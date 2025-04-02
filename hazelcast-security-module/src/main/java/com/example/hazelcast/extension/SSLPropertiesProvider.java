package com.example.hazelcast.extension;

import com.example.hazelcast.config.StaticInjectorHolder;
import com.google.inject.Injector;
import java.util.Properties;

/**
 * Static provider for SSL properties used by NodeExtension
 */
public class SSLPropertiesProvider {
    
    /**
     * Sets the Guice injector to use
     */
    public static void setInjector(Injector injector) {
        StaticInjectorHolder.setInjector(injector);
    }
    
    /**
     * Gets SSL Properties from Guice
     */
    public static Properties getSSLProperties() {
        return StaticInjectorHolder.getInjector().getInstance(Properties.class);
    }
}