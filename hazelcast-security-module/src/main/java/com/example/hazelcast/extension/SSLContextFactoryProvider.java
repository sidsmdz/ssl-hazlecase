package com.example.hazelcast.extension;

import com.example.hazelcast.config.StaticInjectorHolder;
import com.google.inject.Injector;

/**
 * Static provider for SSLContextFactory used by NodeExtension
 */
public class SSLContextFactoryProvider {
    
    /**
     * Sets the Guice injector to use
     */
    public static void setInjector(Injector injector) {
        StaticInjectorHolder.setInjector(injector);
    }
    
    /**
     * Gets SSLContextFactory from Guice
     */
    public static CustomSSLContextFactory getSSLContextFactory() {
        return StaticInjectorHolder.getInjector().getInstance(CustomSSLContextFactory.class);
    }
}