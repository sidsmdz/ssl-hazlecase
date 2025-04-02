package com.example.hazelcast.server;

import com.example.hazelcast.config.HazelcastConfigModule;
import com.example.hazelcast.config.StaticInjectorHolder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;

/**
 * Hazelcast server member with SSL/TLS configuration.
 */
public class ServerMember {

    public static void main(String[] args) {
        // Create Guice injector with our module
        Injector injector = Guice.createInjector(new HazelcastConfigModule());
        
        // Set the static injector for components that can't use regular Guice DI
        StaticInjectorHolder.setInjector(injector);
        
        // Get the HazelcastInstance from the injector
        HazelcastInstance hazelcastInstance = injector.getInstance(HazelcastInstance.class);
        
        System.out.println("Hazelcast server member started with SSL/TLS configured via CustomNodeExtension (OSS).");
    }
}
