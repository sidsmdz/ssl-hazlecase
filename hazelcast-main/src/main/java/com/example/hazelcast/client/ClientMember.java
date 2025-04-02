package com.example.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Example Hazelcast client that can connect with SSL
 */
public class ClientMember {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Connect with SSL? (y/n): ");
        
        String choice = scanner.nextLine().trim().toLowerCase();
        boolean useSSL = choice.equals("y") || choice.equals("yes");
        
        HazelcastInstance client = null;
        
        // Configure client
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("localhost:5701");
        
        if (useSSL) {
            String clientKeystorePath = Paths.get("src/main/resources/client.jks").toAbsolutePath().toString();
            
            System.setProperty("javax.net.ssl.keyStore", clientKeystorePath);
            System.setProperty("javax.net.ssl.keyStorePassword", "password");
            System.setProperty("javax.net.ssl.trustStore", clientKeystorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", "password");
            System.setProperty("hazelcast.client.ssl.enabled", "true");
            
            System.out.println("Connecting with SSL enabled");
        } else {
            System.out.println("Connecting with plain connection");
        }
        
        client = HazelcastClient.newHazelcastClient(config);
        System.out.println("Connected to Hazelcast server");
        
        // Basic operations with the map
        while (true) {
            System.out.println("\nChoose operation:");
            System.out.println("1. Put data");
            System.out.println("2. Get data");
            System.out.println("3. Exit");
            System.out.print("Enter choice (1-3): ");
            
            int op = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (op == 1) {
                System.out.print("Enter key: ");
                String key = scanner.nextLine();
                System.out.print("Enter value: ");
                String value = scanner.nextLine();
                
                client.getMap("dataMap").put(key, value);
                System.out.println("Data stored successfully!");
            } else if (op == 2) {
                System.out.print("Enter key: ");
                String key = scanner.nextLine();
                
                Object value = client.getMap("dataMap").get(key);
                if (value != null) {
                    System.out.println("Retrieved value: " + value);
                } else {
                    System.out.println("Key not found!");
                }
            } else if (op == 3) {
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
        
        if (client != null) {
            client.shutdown();
        }
        scanner.close();
        System.out.println("Client shut down");
    }
}