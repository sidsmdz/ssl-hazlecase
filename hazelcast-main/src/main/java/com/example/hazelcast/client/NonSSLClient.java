package com.example.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.Scanner;

/**
 * Simple Hazelcast client that connects without SSL/TLS on port 5701
 */
public class NonSSLClient {
    
    public static void main(String[] args) {
        // Create a basic client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("localhost:5701");  // Connect to non-SSL port
        
        // Set any SSL properties to null/false to ensure non-SSL connection
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
        System.setProperty("hazelcast.client.ssl.enabled", "false");
        
        System.out.println("Connecting to Hazelcast server without SSL via port 5701...");
        
        // Create client instance
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println("Connected to Hazelcast cluster without SSL/TLS");
        
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
                    
                    client.getMap("test-map").put(key, value);
                    System.out.println("Data stored: " + key + " -> " + value);
                    break;
                
                case "2":
                    System.out.print("Enter key: ");
                    String getKey = scanner.nextLine().trim();
                    
                    Object storedValue = client.getMap("test-map").get(getKey);
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