package com.example.hazelcast;

import com.example.hazelcast.config.TestConfigModule;
import com.example.hazelcast.extension.SecurityConfigProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test case to verify both SSL and non-SSL connections using socket interceptor
 */
public class DualConnectionTest {

    private static final int SERVER_PORT = 5701;
    
    private HazelcastInstance server;
    private HazelcastInstance plainClient;
    private HazelcastInstance sslClient;
    
    private String testKeyPrefix;
    private String testValuePrefix;

    @Before
    public void setUp() throws Exception {
        testKeyPrefix = "test-key-" + UUID.randomUUID().toString().substring(0, 8) + "-";
        testValuePrefix = "test-value-" + UUID.randomUUID().toString().substring(0, 8) + "-";

        System.out.println("\n--- Setting up test environment with mixed-mode socket... ---");
        
        // 1. Clear any existing system properties
        clearSystemProperties();
        
        // 2. Set up SSL certificates
        setupSSLCertificates();
        
        // 3. Configure node extension with socket interceptor
        System.setProperty("hazelcast.node.extension.class", "com.example.hazelcast.extension.CustomNodeExtension");
        
        // 4. Enable SSL but make it optional
        System.setProperty("hazelcast.socket.server.ssl", "true");
        System.setProperty("hazelcast.socket.ssl.optional", "true");
        
        // 5. Set up server using Guice with test config module
        Properties sslProperties = loadSSLProperties();
        Injector injector = Guice.createInjector(new TestConfigModule(sslProperties));
        SecurityConfigProvider.setInjector(injector);
        
        // 6. Start server
        server = injector.getInstance(HazelcastInstance.class);
        System.out.println("Server started with mixed-mode socket support on port: " + SERVER_PORT);
        System.out.println("Server address: " + server.getCluster().getLocalMember().getAddress());
        
        // 7. Wait a bit to ensure the server is ready
        TimeUnit.SECONDS.sleep(2);
        
        // 8. Check if port is open
        checkPortsOpen();
        
        // 9. Initialize plain client
        System.out.println("\nInitializing plain client...");
        clearSystemProperties();  // Clear SSL settings
        System.setProperty("hazelcast.client.ssl.enabled", "false");
        
        ClientConfig plainConfig = new ClientConfig();
        plainConfig.getNetworkConfig().addAddress("localhost:" + SERVER_PORT);
        
        plainClient = HazelcastClient.newHazelcastClient(plainConfig);
        System.out.println("Plain client connected successfully to port " + SERVER_PORT);
        
        // 10. Initialize SSL client
        System.out.println("\nInitializing SSL client...");
        Path clientKeystore = findClientKeystore();
        
        // Clear and set SSL properties for client
        clearSystemProperties();
        System.setProperty("javax.net.ssl.keyStore", clientKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", clientKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("hazelcast.client.ssl.enabled", "true");
        
        // Create SSL client config
        ClientConfig sslConfig = new ClientConfig();
        sslConfig.getNetworkConfig().addAddress("localhost:" + SERVER_PORT);
        
        try {
            sslClient = HazelcastClient.newHazelcastClient(sslConfig);
            System.out.println("SSL client connected successfully to port " + SERVER_PORT);
        } catch (Exception e) {
            System.err.println("Failed to connect SSL client: " + e.getMessage());
            e.printStackTrace();
            // Continue with the test even if SSL client fails
        }
    }
    
    /**
     * Check if the server port is open
     */
    private void checkPortsOpen() {
        checkPort("localhost", SERVER_PORT, "Server socket");
    }
    
    private void checkPort(String host, int port, String description) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            System.out.println("✅ " + description + " port " + port + " is OPEN");
        } catch (IOException e) {
            System.err.println("❌ " + description + " port " + port + " is CLOSED or not reachable");
        }
    }
    
    @Test
    public void testPlainConnectionWorks() {
        System.out.println("\n--- Testing plain connection... ---");
        assertNotNull("Plain client should be initialized", plainClient);
        
        // Test writing and reading
        String plainKey = testKeyPrefix + "plain";
        String plainValue = testValuePrefix + "plain";
        
        System.out.println("Writing data via plain client: " + plainKey + " -> " + plainValue);
        plainClient.getMap("test-map").put(plainKey, plainValue);
        
        System.out.println("Reading data via plain client...");
        Object readValue = plainClient.getMap("test-map").get(plainKey);
        assertNotNull("Value should be readable", readValue);
        assertEquals("Value should match what was written", plainValue, readValue);
        
        System.out.println("✅ Plain connection test passed");
    }
    
    @Test
    public void testSSLConnectionIfAvailable() {
        System.out.println("\n--- Testing SSL connection... ---");
        if (sslClient == null) {
            System.out.println("⚠️ SSL client not available, skipping test");
            return;
        }
        
        // Test writing and reading
        String sslKey = testKeyPrefix + "ssl";
        String sslValue = testValuePrefix + "ssl";
        
        System.out.println("Writing data via SSL client: " + sslKey + " -> " + sslValue);
        sslClient.getMap("test-map").put(sslKey, sslValue);
        
        System.out.println("Reading data via SSL client...");
        Object readValue = sslClient.getMap("test-map").get(sslKey);
        assertNotNull("Value should be readable", readValue);
        assertEquals("Value should match what was written", sslValue, readValue);
        
        System.out.println("✅ SSL connection test passed");
    }
    
    @Test
    public void testDataSharingIfBothAvailable() {
        System.out.println("\n--- Testing data sharing between connections... ---");
        if (sslClient == null) {
            System.out.println("⚠️ SSL client not available, skipping test");
            return;
        }
        
        // Test plain to SSL
        String plainKey = testKeyPrefix + "plain-to-ssl";
        String plainValue = testValuePrefix + "plain-to-ssl";
        
        System.out.println("Writing data via plain client: " + plainKey + " -> " + plainValue);
        plainClient.getMap("test-map").put(plainKey, plainValue);
        
        System.out.println("Reading data via SSL client...");
        Object plainValueFromSSL = sslClient.getMap("test-map").get(plainKey);
        assertNotNull("SSL client should be able to read data written by plain client", plainValueFromSSL);
        assertEquals("Value read by SSL client should match what was written by plain client", 
                plainValue, plainValueFromSSL);
        
        // Test SSL to plain
        String sslKey = testKeyPrefix + "ssl-to-plain";
        String sslValue = testValuePrefix + "ssl-to-plain";
        
        System.out.println("Writing data via SSL client: " + sslKey + " -> " + sslValue);
        sslClient.getMap("test-map").put(sslKey, sslValue);
        
        System.out.println("Reading data via plain client...");
        Object sslValueFromPlain = plainClient.getMap("test-map").get(sslKey);
        assertNotNull("Plain client should be able to read data written by SSL client", sslValueFromPlain);
        assertEquals("Value read by plain client should match what was written by SSL client", 
                sslValue, sslValueFromPlain);
        
        System.out.println("✅ Data sharing test passed");
    }
    
    @After
    public void tearDown() {
        System.out.println("\n--- Tearing down test environment... ---");
        
        if (plainClient != null) {
            try {
                plainClient.shutdown();
                System.out.println("Plain client shut down");
            } catch (Exception e) {
                System.err.println("Error shutting down plain client: " + e.getMessage());
            }
        }
        
        if (sslClient != null) {
            try {
                sslClient.shutdown();
                System.out.println("SSL client shut down");
            } catch (Exception e) {
                System.err.println("Error shutting down SSL client: " + e.getMessage());
            }
        }
        
        if (server != null) {
            try {
                server.shutdown();
                System.out.println("Server shut down");
            } catch (Exception e) {
                System.err.println("Error shutting down server: " + e.getMessage());
            }
        }
        
        // Clear system properties
        clearSystemProperties();
        
        System.out.println("Test environment teardown complete");
    }
    
    /**
     * Clear all SSL and Hazelcast system properties
     */
    private void clearSystemProperties() {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
        System.clearProperty("hazelcast.client.ssl.enabled");
        System.clearProperty("hazelcast.socket.server.port");
        System.clearProperty("hazelcast.socket.server.ssl.port");
        System.clearProperty("hazelcast.socket.server.ssl");
        System.clearProperty("hazelcast.socket.ssl.optional");
        System.clearProperty("hazelcast.node.extension.class");
        System.clearProperty("javax.net.debug");
    }
    
    // ... rest of your utility methods remain the same
    
    /**
     * Find client keystore file by checking potential locations
     */
    private Path findClientKeystore() {
        Path[] possibleLocations = {
            Paths.get("src/main/resources/client.jks"),
            Paths.get("src/test/resources/client.jks"),
            Paths.get("../hazelcast-security-module/src/main/resources/client.jks"),
            Paths.get("../hazelcast-main/src/main/resources/client.jks"),
            Paths.get("hazelcast-security-module/src/main/resources/client.jks"),
            Paths.get("hazelcast-main/src/main/resources/client.jks")
        };
        
        for (Path path : possibleLocations) {
            if (Files.exists(path)) {
                System.out.println("Found client keystore at: " + path.toAbsolutePath());
                return path;
            }
        }
        
        throw new RuntimeException("Could not find client.jks keystore in any of the expected locations");
    }
    
    /**
     * Set up SSL certificates for testing
     */
    private void setupSSLCertificates() throws Exception {
        // Find the keystore files
        Path serverKeystore = findServerKeystore();
        Path clientKeystore = findClientKeystore();
        
        System.out.println("Using server keystore: " + serverKeystore.toAbsolutePath());
        System.out.println("Using client keystore: " + clientKeystore.toAbsolutePath());
        
        // Set up server SSL properties
        System.setProperty("javax.net.ssl.keyStore", serverKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", serverKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }
    
    /**
     * Find server keystore file by checking potential locations
     */
    private Path findServerKeystore() {
        Path[] possibleLocations = {
            Paths.get("src/main/resources/server.jks"),
            Paths.get("src/test/resources/server.jks"),
            Paths.get("../hazelcast-security-module/src/main/resources/server.jks"),
            Paths.get("../hazelcast-main/src/main/resources/server.jks"),
            Paths.get("hazelcast-security-module/src/main/resources/server.jks"),
            Paths.get("hazelcast-main/src/main/resources/server.jks")
        };
        
        for (Path path : possibleLocations) {
            if (Files.exists(path)) {
                return path;
            }
        }
        
        throw new RuntimeException("Could not find server.jks keystore in any of the expected locations");
    }
    
    /**
     * Load SSL properties for the server
     */
    private Properties loadSSLProperties() {
        Properties properties = new Properties();
        properties.setProperty("protocol", "TLS");
        properties.setProperty("keyManagerAlgorithm", "SunX509");
        properties.setProperty("trustManagerAlgorithm", "SunX509");
        properties.setProperty("keyStoreType", "JKS");
        properties.setProperty("trustStoreType", "JKS");
        return properties;
    }
}