package com.example.hazelcast.extension;

import com.google.inject.Injector;

import java.util.Properties;

/**
 * Provider for SSL Properties that can be accessed from the security module
 */
public class SSLPropertiesProvider {
    private static Injector injector;
    
    /**
     * Sets the injector provided by the main module
     */
    public static void setInjector(Injector injectorInstance) {
        injector = injectorInstance;
    }
    
    /**
     * Gets the SSL properties from the injector
     */
    public static Properties getSSLProperties() {
        if (injector == null) {
            throw new IllegalStateException("Injector not initialized. Call setInjector first.");
        }
        return injector.getInstance(Properties.class);
    }
}