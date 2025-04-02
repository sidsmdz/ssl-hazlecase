package com.example.hazelcast.client;

import com.example.hazelcast.extension.CustomSSLContextFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Client {
    
    private final Injector injector;
    
    public Client(Module... modules) {
        this.injector = Guice.createInjector(modules);
    }
    
    public void run() {
        // Get client configuration
        ClientConfig clientConfig = createClientConfig();
        
        // Create client instance
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println("Connected to Hazelcast cluster with SSL/TLS");
        
        try {
            // Test map operations
            client.getMap("test-map").put("key1", "value1");
            System.out.println("Value: " + client.getMap("test-map").get("key1"));
        } finally {
            // Shutdown client
            client.shutdown();
        }
    }
    
    private ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("localhost:5701");
        
        // Configure SSL
        Properties sslProperties = loadClientSSLProperties();
        clientConfig.getNetworkConfig().getSSLConfig()
                .setEnabled(true)
                .setFactoryClassName("com.example.hazelcast.extension.CustomSSLContextFactory")
                .setProperties(sslProperties);
                
        return clientConfig;
    }
    
    private Properties loadClientSSLProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("client-ssl.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException("Could not find client-ssl.properties in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading client SSL properties", e);
        }
        return properties;
    }
    
    public static void main(String[] args) {
        // Create client module for dependency injection
        Module clientModule = new ClientModule();
        
        // Create and run client
        new Client(clientModule).run();
    }
}
