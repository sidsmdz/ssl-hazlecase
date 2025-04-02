package com.example.hazelcast.extension;

import com.google.inject.Injector;
import com.hazelcast.nio.ssl.SSLContextFactory;

import java.util.Properties;

/**
 * Centralized provider for security-related components that need to be accessed
 * by the security module from the main application's injector.
 * <p>
 * This eliminates the need for separate provider classes and simplifies
 * the configuration flow between the main module and security module.
 */
public class SecurityConfigProvider {
    private static Injector injector;
    
    /**
     * Sets the injector provided by the main module
     */
    public static void setInjector(Injector injectorInstance) {
        injector = injectorInstance;
    }
    
    /**
     * Gets the Guice injector
     */
    public static Injector getInjector() {
        if (injector == null) {
            throw new IllegalStateException("Injector not initialized. Call setInjector first.");
        }
        return injector;
    }
    
    /**
     * Gets the SSL properties from the injector
     */
    public static Properties getSSLProperties() {
        return getInjector().getInstance(Properties.class);
    }
    
    /**
     * Gets the SSL context factory from the injector
     */
    public static SSLContextFactory getSSLContextFactory() {
        return getInjector().getInstance(CustomSSLContextFactory.class);
    }
    
    /**
     * Gets any instance from the injector by class type
     */
    public static <T> T getInstance(Class<T> type) {
        return getInjector().getInstance(type);
    }
}