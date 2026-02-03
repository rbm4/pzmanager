package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import com.apocalipsebr.zomboid.server.manager.domain.entity.ServerCommand;
import com.apocalipsebr.zomboid.server.manager.domain.exception.ServerCommandException;
import com.apocalipsebr.zomboid.server.manager.domain.port.ServerCommandExecutor;

import org.glavo.rcon.AuthenticationException;
import org.glavo.rcon.MalformedPacketException;
import org.glavo.rcon.Rcon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Component
public class BashCommandExecutor implements ServerCommandExecutor {
    private static final Logger logger = Logger.getLogger(BashCommandExecutor.class.getName());

    @Value("${zomboid.control.file:/opt/pzserver/zomboid.control}")
    private String controlFilePath;
    @Override
    public void execute(ServerCommand command){
        Rcon rcon = null;
        try {
            rcon = new Rcon("72.62.137.60", 27015, "PzRconPaswd44@key");
            rcon.command(command.getCommand());

            logger.info("Command: " + command.getCommand());

            rcon.close();
        } catch (IOException | AuthenticationException e) {
            throw new ServerCommandException("Failed: " + e.getMessage(), e);
        } 
    }
    @Override
    public String executeResponse(ServerCommand command) {
        Rcon rcon = null;
        try {
            rcon = new Rcon("72.62.137.60", 27015, "PzRconPaswd44@key");

            rcon.command(command.getCommand());

            // Wait for response to fully arrive
            Thread.sleep(command.getResponseWaitTime());
            String resultee = read(rcon.getSocket().getInputStream());
            logger.info("Command: " + command.getCommand());
            logger.info("Response: [" + resultee + "]");

            return resultee;
        } catch (IOException | AuthenticationException | InterruptedException e) {
            throw new ServerCommandException("Failed: " + e.getMessage(), e);
        } finally {
            if (rcon != null) {
                try {
                    rcon.close();
                } catch (IOException e) {
                    logger.warning("Error closing: " + e.getMessage());
                }
            }
        }
    }

    private static String read(InputStream in) throws IOException {
        // Header is 3 4-bytes ints
        byte[] header = new byte[4 * 3];

        // Read the 3 ints
        //noinspection ResultOfMethodCallIgnored
        in.read(header);

        try {
            // Use a bytebuffer in little endian to read the first 3 ints
            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            int length = buffer.getInt();
            int requestId = buffer.getInt();
            int type = buffer.getInt();

            // Payload size can be computed now that we have its length
            byte[] payload = new byte[length - 4 - 4 - 2];

            DataInputStream dis = new DataInputStream(in);

            // Read the full payload
            dis.readFully(payload);

            // Read the null bytes
            //noinspection ResultOfMethodCallIgnored
            dis.read(new byte[2]);

            return new String(payload, StandardCharsets.UTF_8);
        } catch (BufferUnderflowException | EOFException e) {
            throw new MalformedPacketException("Cannot read the whole packet");
        }
    }
}
