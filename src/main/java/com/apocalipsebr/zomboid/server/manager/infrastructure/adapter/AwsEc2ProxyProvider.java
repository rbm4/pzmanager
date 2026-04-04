package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import com.apocalipsebr.zomboid.server.manager.infrastructure.config.AwsProxyConfig.ProxyProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class AwsEc2ProxyProvider implements ProxyProvider {

    private static final Logger logger = Logger.getLogger(AwsEc2ProxyProvider.class.getName());
    public static final String PROVIDER_TYPE = "AWS_EC2";

    private final ProxyProperties proxyProperties;
    private final ConcurrentHashMap<String, Ec2Client> clientsByRegion = new ConcurrentHashMap<>();

    public AwsEc2ProxyProvider(ProxyProperties proxyProperties) {
        this.proxyProperties = proxyProperties;
    }

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public void startInstance(String instanceId, String region) {
        Ec2Client client = getClient(region);
        if (client == null) {
            logger.warning("EC2 client not configured — skipping startInstance for " + instanceId);
            return;
        }
        logger.info("Starting EC2 instance: " + instanceId + " in region " + region);
        client.startInstances(r -> r.instanceIds(instanceId));
    }

    @Override
    public void stopInstance(String instanceId, String region) {
        Ec2Client client = getClient(region);
        if (client == null) {
            logger.warning("EC2 client not configured — skipping stopInstance for " + instanceId);
            return;
        }
        logger.info("Stopping EC2 instance: " + instanceId + " in region " + region);
        client.stopInstances(r -> r.instanceIds(instanceId));
    }

    @Override
    public ProxyInstanceState getInstanceState(String instanceId, String region) {
        Ec2Client client = getClient(region);
        if (client == null) {
            logger.warning("EC2 client not configured — returning 'stopped' for " + instanceId);
            return new ProxyInstanceState("stopped", null);
        }
        try {
            DescribeInstancesResponse response = client.describeInstances(
                    r -> r.instanceIds(instanceId));
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    String state = instance.state().nameAsString();
                    String publicIp = instance.publicIpAddress();
                    logger.info("EC2 instance " + instanceId + " state: " + state + ", publicIp: " + publicIp);
                    return new ProxyInstanceState(state, publicIp);
                }
            }
            logger.warning("No instance found for ID: " + instanceId);
            return new ProxyInstanceState("unknown", null);
        } catch (Ec2Exception e) {
            logger.warning("Failed to describe EC2 instance " + instanceId + ": " + e.getMessage());
            return new ProxyInstanceState("unknown", null);
        }
    }

    @Override
    public boolean isConfigured() {
        return proxyProperties.getAccessKey() != null && !proxyProperties.getAccessKey().isBlank()
                && proxyProperties.getSecretKey() != null && !proxyProperties.getSecretKey().isBlank();
    }

    private Ec2Client getClient(String region) {
        if (!isConfigured()) {
            return null;
        }
        String effectiveRegion = (region != null && !region.isBlank()) ? region : proxyProperties.getRegion();
        return clientsByRegion.computeIfAbsent(effectiveRegion, r ->
                Ec2Client.builder()
                        .region(Region.of(r))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(proxyProperties.getAccessKey(), proxyProperties.getSecretKey())))
                        .build()
        );
    }
}
