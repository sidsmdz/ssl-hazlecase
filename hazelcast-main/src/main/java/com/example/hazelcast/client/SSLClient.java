package com.example.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Hazelcast client that connects with SSL/TLS enabled using JVM system properties
 * Compatible with Hazelcast Open Source Edition
 */
public class SSLClient {
    
    public static void main(String[] args) {
        // Setup SSL system properties
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path clientKeystore = projectRoot.resolve("hazelcast-security-module/src/main/resources/client.jks");
        
        if (!Files.exists(clientKeystore)) {
            System.err.println("Client keystore not found at: " + clientKeystore.toAbsolutePath());
            System.err.println("Please run the generate-certs.sh script first");
            System.exit(1);
        }

        System.out.println("Using keystore at: " + clientKeystore.toAbsolutePath());
        
        // Set system properties for SSL
        System.setProperty("javax.net.ssl.keyStore", clientKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", clientKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        
        // Enable SSL for Hazelcast client (OSS approach)
        System.setProperty("hazelcast.client.ssl.enabled", "true");
        
        // Create a basic client config (without SSLConfig)
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("localhost:5701");
        
        System.out.println("Connecting to Hazelcast server with SSL via JVM system properties...");
        
        // Create client instance
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println("Connected to Hazelcast cluster with SSL/TLS");
        
        // Interactive console for map operations
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        System.out.println("Map Operations Console (enter 'exit' to quit):");
        
        while (running) {
            System.out.println("\nSelect operation:");
            System.out.println("1. Put data");
            System.out.println("2. Get data");
            System.out.println("3. Exit");
            
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    System.out.print("Enter key: ");
                    String key = scanner.nextLine().trim();
                    System.out.print("Enter value: ");
                    String value = scanner.nextLine().trim();
                    
                    client.getMap("secure-map").put(key, value);
                    System.out.println("Data stored securely: " + key + " -> " + value);
                    break;
                
                case "2":
                    System.out.print("Enter key: ");
                    String getKey = scanner.nextLine().trim();
                    
                    Object storedValue = client.getMap("secure-map").get(getKey);
                    if (storedValue != null) {
                        System.out.println("Retrieved value: " + getKey + " -> " + storedValue);
                    } else {
                        System.out.println("No value found for key: " + getKey);
                    }
                    break;
                
                case "3":
                case "exit":
                    running = false;
                    break;
                
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        // Shutdown client
        System.out.println("Shutting down client...");
        client.shutdown();
        scanner.close();
        System.out.println("Client shut down successfully");
    }
}