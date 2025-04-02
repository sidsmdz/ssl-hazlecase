package com.example.hazelcast.extension;

import com.google.inject.Injector;

/**
 * Provider for the CustomSSLContextFactory that can be accessed from the security module
 */
public class SSLContextFactoryProvider {
    private static Injector injector;
    
    /**
     * Sets the injector provided by the main module
     */
    public static void setInjector(Injector injectorInstance) {
        injector = injectorInstance;
    }
    
    /**
     * Gets the SSL context factory from the injector
     */
    public static CustomSSLContextFactory getSSLContextFactory() {
        if (injector == null) {
            throw new IllegalStateException("Injector not initialized. Call setInjector first.");
        }
        return injector.getInstance(CustomSSLContextFactory.class);
    }
}