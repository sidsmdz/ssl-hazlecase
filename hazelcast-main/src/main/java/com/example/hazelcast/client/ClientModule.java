package com.example.hazelcast.client;

import com.example.hazelcast.extension.CustomSSLContextFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CustomSSLContextFactory.class).in(Singleton.class);
    }
    
    @Provides
    @Singleton
    public Properties provideClientSSLProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("client-ssl.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException("Could not find client-ssl.properties in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading client SSL properties", e);
        }
        return properties;
    }
}