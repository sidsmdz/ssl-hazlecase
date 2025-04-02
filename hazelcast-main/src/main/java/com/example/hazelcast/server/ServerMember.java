package com.example.hazelcast.server;

import com.example.hazelcast.config.HazelcastConfigModule;
import com.example.hazelcast.config.StaticInjectorHolder;
import com.example.hazelcast.extension.SSLContextFactoryProvider;
import com.example.hazelcast.extension.SSLPropertiesProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;

/**
 * Hazelcast server member with SSL/TLS configuration.
 * Uses Guice for dependency injection.
 */
public class ServerMember {

    public static void main(String[] args) {
        // Create Guice injector with our module
        Injector injector = Guice.createInjector(new HazelcastConfigModule());
        
        // Set the injector in our provider classes
        StaticInjectorHolder.setInjector(injector);
        SSLPropertiesProvider.setInjector(injector);
        SSLContextFactoryProvider.setInjector(injector);
        
        // Configure node extension
        System.setProperty("hazelcast.node.extension.class", "com.example.hazelcast.extension.CustomNodeExtension");
        
        // Get the HazelcastInstance from the injector
        HazelcastInstance hazelcastInstance = injector.getInstance(HazelcastInstance.class);
        
        System.out.println("Hazelcast server member started with SSL/TLS configured via CustomNodeExtension");
        System.out.println("Server running at: " + hazelcastInstance.getCluster().getLocalMember().getAddress());
        System.out.println("SSL is enabled for this instance");
        System.out.println("Press Ctrl+C to exit");
    }
}
