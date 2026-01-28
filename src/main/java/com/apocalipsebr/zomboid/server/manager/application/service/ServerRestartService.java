package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class ServerRestartService {
    private static final Logger logger = Logger.getLogger(ServerRestartService.class.getName());

    private final ServerCommandService commandService;
    private final DiscordNotificationService discordNotificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean restartInProgress = false;

    @Value("${server.restart.password:adminrbz0mb01d2$3}")
    private String restartPassword;

    public ServerRestartService(ServerCommandService commandService,
            DiscordNotificationService discordNotificationService) {
        this.commandService = commandService;
        this.discordNotificationService = discordNotificationService;
    }

    public boolean validatePassword(String password) {
        return restartPassword.equals(password);
    }

    public void bootSequence(){
        scheduler.schedule(() -> {
            discordNotificationService.sendServerBooting();
        }, 0, TimeUnit.SECONDS);
    }

    public synchronized void initiateRestart() {
        if (restartInProgress) {
            throw new IllegalStateException("A restart is already in progress");
        }

        restartInProgress = true;
        logger.info("Server restart initiated");

        // Send Discord notification that restart was initiated
        discordNotificationService.sendRestartInitiated();

        // 10 minute warning
        scheduler.schedule(() -> {
            warningBlock("10 minutos");
        }, 0, TimeUnit.SECONDS);

        // 5 minute warning
        scheduler.schedule(() -> {
            warningBlock("5 minutos");
        }, 5, TimeUnit.MINUTES);

        // 1 minute warning
        scheduler.schedule(() -> {
            warningBlock("1 minuto");
        }, 9, TimeUnit.MINUTES);

        // 15 second warning
        scheduler.schedule(() -> {
            warningBlock("15 segundos");
        }, 9 * 60 + 45, TimeUnit.SECONDS);

        // Save, quit and restart
        scheduler.schedule(() -> {
            scheduler.schedule(() -> {
                sendWarning("Voltamos rapidinho, apenas 5 minutos!");
            }, 10, TimeUnit.SECONDS);
            executeShutdownSequence();
        }, 10, TimeUnit.MINUTES);
    }

    private void warningBlock(String tempo) {
        sendWarning("Servidor reiniciando em " + tempo);
        scheduler.schedule(() -> {
            String command = "servermsg \" " + "Voltamos rapidinho, apenas 5 minutos!" + "\"";
            logger.info("Sending warning: " + command);
            commandService.sendCommand(command);
        }, 10, TimeUnit.SECONDS);
    }

    private void sendWarning(String time) {
        try {
            String command = "servermsg \" " + time + "\"";
            logger.info("Sending warning: " + command);
            commandService.sendCommand(command);

            // Send Discord notification
            discordNotificationService.sendRestartWarning(time);
        } catch (Exception e) {
            logger.severe("Failed to send warning: " + e.getMessage());
        }
    }

    private void executeShutdownSequence() {
        try {
            // Send Discord notification that server is shutting down
            discordNotificationService.sendServerShuttingDown();

            // Save the server
            logger.info("Executing save command");
            commandService.sendCommand("save");

            // Wait 30 seconds for save to complete
            Thread.sleep(30000);

            // Quit the server
            logger.info("Executing quit command");
            commandService.sendCommand("quit");

            // Wait a bit for server to shut down gracefully
            Thread.sleep(25000);

            // Execute the restart script
            executeRestartScript();

        } catch (Exception e) {
            logger.severe("Error during shutdown sequence: " + e.getMessage());
            restartInProgress = false;
        }
    }

    private void executeRestartScript() {
        try {
            logger.info("Executing VM restart script");

            // Get the script from resources
            InputStream scriptStream = getClass().getClassLoader()
                    .getResourceAsStream("restart-vm.sh");

            if (scriptStream == null) {
                logger.severe("Restart script not found in resources");
                restartInProgress = false;
                return;
            }

            // Copy script to temp location and make it executable
            String tempScript = "/tmp/restart-vm.sh";
            ProcessBuilder copyBuilder = new ProcessBuilder(
                    "bash", "-c",
                    "cat > " + tempScript + " && chmod +x " + tempScript);
            Process copyProcess = copyBuilder.start();

            // Write script content
            scriptStream.transferTo(copyProcess.getOutputStream());
            copyProcess.getOutputStream().close();
            copyProcess.waitFor();

            // Execute the script
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Log output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("Script output: " + line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("Restart script completed with exit code: " + exitCode);

            // Note: Application will be terminated by VM restart

        } catch (IOException | InterruptedException e) {
            logger.severe("Failed to execute restart script: " + e.getMessage());
            restartInProgress = false;
        }
    }

    public boolean isRestartInProgress() {
        return restartInProgress;
    }
}
