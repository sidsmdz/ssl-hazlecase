package com.example.hazelcast.server;

import com.example.hazelcast.config.MainConfigModule;
import com.example.hazelcast.extension.SecurityConfigProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Hazelcast server member with both SSL/TLS and plain socket configuration using OSGI properties.
 */
public class ServerMember {

    private static final int PLAIN_PORT = 5701;
    private static final int SSL_PORT = 5702;

    public static void main(String[] args) {
        // Step 1: Load and set up SSL properties from the main application
        Properties sslProperties = loadSSLProperties();
        
        // Step 2: Configure paths for SSL certificates
        setupSSLCertificatePaths();
        
        // Step 3: Configure OSGI properties for dual ports
        configureDualPorts();
        
        // Step 4: Configure node extension - this activates our security module
        System.setProperty("hazelcast.node.extension.class", "com.example.hazelcast.extension.CustomNodeExtension");
        
        // Step 5: Create Guice injector with our main configuration module
        Injector injector = Guice.createInjector(new MainConfigModule(sslProperties));
        
        // Step 6: Provide the injector to the security module
        SecurityConfigProvider.setInjector(injector);
        
        // Step 7: Get the HazelcastInstance from the injector
        HazelcastInstance hazelcastInstance = injector.getInstance(HazelcastInstance.class);
        
        System.out.println("Hazelcast server started with dual ports:");
        System.out.println("Plain socket: localhost:" + PLAIN_PORT);
        System.out.println("SSL socket: localhost:" + SSL_PORT);
        System.out.println("Member address: " + hazelcastInstance.getCluster().getLocalMember().getAddress());
        System.out.println("Press Ctrl+C to exit");
    }
    
    /**
     * Configure OSGI properties to expose multiple ports
     */
    private static void configureDualPorts() {
        // Configure dual ports
        System.setProperty("hazelcast.socket.server.port", PLAIN_PORT + "," + SSL_PORT);
        System.setProperty("hazelcast.socket.server.ssl.port", String.valueOf(SSL_PORT));
        
        // Enable SSL but make it optional
        System.setProperty("hazelcast.socket.server.ssl", "true");
        System.setProperty("hazelcast.socket.client.ssl.enabled", "true");
        System.setProperty("hazelcast.socket.ssl.optional", "true"); // Makes SSL optional
    }
    
    /**
     * Load SSL properties from main application resources
     */
    private static Properties loadSSLProperties() {
        Properties properties = new Properties();
        try {
            Path sslPropsPath = Paths.get("hazelcast-main/src/main/resources/ssl.properties");
            if (Files.exists(sslPropsPath)) {
                properties.load(Files.newInputStream(sslPropsPath));
            } else {
                System.out.println("SSL properties file not found, using defaults");
                properties.setProperty("protocol", "TLS");
                properties.setProperty("keyManagerAlgorithm", "SunX509");
                properties.setProperty("trustManagerAlgorithm", "SunX509");
                properties.setProperty("keyStoreType", "JKS");
                properties.setProperty("trustStoreType", "JKS");
            }
        } catch (Exception e) {
            System.err.println("Error loading SSL properties: " + e.getMessage());
        }
        return properties;
    }
    
    /**
     * Set up SSL certificate paths
     */
    private static void setupSSLCertificatePaths() {
        Path keystorePath = Paths.get("hazelcast-main/src/main/resources/server.jks");
        
        // Ensure keystore exists
        if (!Files.exists(keystorePath)) {
            System.err.println("Server keystore not found at: " + keystorePath.toAbsolutePath());
            System.err.println("Please run the generate-certs.sh script first");
            System.exit(1);
        }
        
        // Configure SSL properties
        System.setProperty("javax.net.ssl.keyStore", keystorePath.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", keystorePath.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }
}
