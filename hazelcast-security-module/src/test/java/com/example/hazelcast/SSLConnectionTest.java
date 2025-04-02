package com.example.hazelcast;

import com.example.hazelcast.config.HazelcastConfigModule;
import com.example.hazelcast.config.StaticInjectorHolder;
import com.example.hazelcast.extension.SSLContextFactoryProvider;
import com.example.hazelcast.extension.SSLPropertiesProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SSLConnectionTest {

    private Injector injector;
    private HazelcastInstance server;
    private HazelcastInstance client;

    @Before
    public void setUp() throws Exception {
        // Check if certificates exist
        URL clientKeystore = getClass().getClassLoader().getResource("client.jks");
        URL serverKeystore = getClass().getClassLoader().getResource("server.jks");
        
        if (clientKeystore == null || serverKeystore == null) {
            System.err.println("Keystore files not found. Generating keystores automatically...");
            
            // Try to run the script directly
            String projectDir = System.getProperty("user.dir");
            String scriptPath = projectDir + "/scripts/generate-certs.sh";
            File script = new File(scriptPath);
            
            if (script.exists()) {
                System.out.println("Found script at: " + scriptPath);
                ProcessBuilder pb = new ProcessBuilder("bash", scriptPath);
                pb.directory(new File(projectDir));
                pb.inheritIO();
                
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    throw new RuntimeException("Failed to generate keystores, exit code: " + exitCode);
                }
                
                System.out.println("Certificates generated successfully!");
                
                // Reload the resources
                clientKeystore = getClass().getClassLoader().getResource("client.jks");
                serverKeystore = getClass().getClassLoader().getResource("server.jks");
                
                // If still null, fail
                if (clientKeystore == null || serverKeystore == null) {
                    throw new RuntimeException("Keystores still not found after generation attempt. Check script paths.");
                }
            } else {
                throw new RuntimeException("Keystore files not found and script not found at " + scriptPath);
            }
        }
        
        // Now we should have valid keystore paths
        String clientKeystorePath = new File(clientKeystore.toURI()).getAbsolutePath();
        String serverKeystorePath = new File(serverKeystore.toURI()).getAbsolutePath();
        
        System.out.println("Using client keystore: " + clientKeystorePath);
        System.out.println("Using server keystore: " + serverKeystorePath);
        
        // Set SSL system properties
        System.setProperty("javax.net.ssl.keyStore", serverKeystorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", serverKeystorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        
        // Create Guice injector
        injector = Guice.createInjector(new HazelcastConfigModule());
        
        // Set the injector in provider classes
        StaticInjectorHolder.setInjector(injector);
        SSLPropertiesProvider.setInjector(injector);
        SSLContextFactoryProvider.setInjector(injector);
        
        // Configure NodeExtension for SSL
        System.setProperty("hazelcast.node.extension.class", "com.example.hazelcast.extension.CustomNodeExtension");
        
        // Get server config from injector and start server
        Config config = injector.getInstance(Config.class);
        server = Hazelcast.newHazelcastInstance(config);
        
        // Configure client with SSL
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("localhost:5701");
        
        // Set client SSL properties
        System.setProperty("javax.net.ssl.keyStore", clientKeystorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", clientKeystorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("hazelcast.client.ssl.enabled", "true");
        
        // Create client
        client = HazelcastClient.newHazelcastClient(clientConfig);
    }
    
    @Test
    public void testSSLConnection() {
        // Test that client can communicate with server over TLS
        String testKey = "testKey";
        String testValue = "testValue";
        
        client.getMap("testMap").put(testKey, testValue);
        
        Object retrievedValue = client.getMap("testMap").get(testKey);
        assertNotNull("Retrieved value should not be null", retrievedValue);
        assertEquals("Client should be able to retrieve the value it put", testValue, retrievedValue);
    }
    
    @After
    public void tearDown() {
        // Clean up
        if (client != null) {
            client.shutdown();
        }
        if (server != null) {
            server.shutdown();
        }
    }
}
