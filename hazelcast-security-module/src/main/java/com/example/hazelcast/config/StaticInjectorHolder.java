package com.example.hazelcast.config;

import com.google.inject.Injector;

/**
 * Holds a static reference to the Guice Injector to allow access from components
 * that aren't created by Guice (like NodeExtension instances created by Hazelcast)
 */
public class StaticInjectorHolder {
    private static Injector injector;
    
    public static void setInjector(Injector injectorInstance) {
        injector = injectorInstance;
    }
    
    public static Injector getInjector() {
        if (injector == null) {
            throw new IllegalStateException("Guice Injector has not been initialized");
        }
        return injector;
    }
}