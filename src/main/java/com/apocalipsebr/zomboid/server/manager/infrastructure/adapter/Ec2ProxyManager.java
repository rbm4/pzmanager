package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import org.springframework.lang.Nullable;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Ec2ProxyManager {

    private static final Logger logger = Logger.getLogger(Ec2ProxyManager.class.getName());

    private final Ec2Client ec2Client;

    public Ec2ProxyManager(@Nullable Ec2Client ec2Client) {
        this.ec2Client = ec2Client;
    }

    public void startInstance(String instanceId) {
        if (ec2Client == null) {
            logger.warning("EC2 client not configured — skipping startInstance for " + instanceId);
            return;
        }
        logger.info("Starting EC2 instance: " + instanceId);
        ec2Client.startInstances(r -> r.instanceIds(instanceId));
    }

    public void stopInstance(String instanceId) {
        if (ec2Client == null) {
            logger.warning("EC2 client not configured — skipping stopInstance for " + instanceId);
            return;
        }
        logger.info("Stopping EC2 instance: " + instanceId);
        ec2Client.stopInstances(r -> r.instanceIds(instanceId));
    }

    /**
     * Returns the current EC2 instance state name.
     * Possible values: "pending", "running", "shutting-down", "terminated", "stopping", "stopped"
     */
    public String getInstanceState(String instanceId) {
        if (ec2Client == null) {
            logger.warning("EC2 client not configured — returning 'stopped' for " + instanceId);
            return "stopped";
        }
        try {
            DescribeInstancesResponse response = ec2Client.describeInstances(
                    r -> r.instanceIds(instanceId));
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    String state = instance.state().nameAsString();
                    logger.info("EC2 instance " + instanceId + " state: " + state);
                    return state;
                }
            }
            logger.warning("No instance found for ID: " + instanceId);
            return "unknown";
        } catch (Ec2Exception e) {
            logger.warning("Failed to describe EC2 instance " + instanceId + ": " + e.getMessage());
            return "unknown";
        }
    }

    public boolean isConfigured() {
        return ec2Client != null;
    }
}
