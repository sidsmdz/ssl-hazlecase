package com.example.hazelcast.config;

import com.example.hazelcast.extension.CustomSSLContextFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ssl.SSLContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HazelcastConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind SSLContextFactory to our implementation
        bind(SSLContextFactory.class).to(CustomSSLContextFactory.class).in(Singleton.class);
        bind(CustomSSLContextFactory.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public Properties provideSSLProperties() {
        return loadSSLProperties();
    }

    @Provides
    @Singleton
    public Config provideHazelcastConfig() {
        // Set the NodeExtension class using system property
        System.setProperty("hazelcast.node.extension.class", "com.example.hazelcast.extension.CustomNodeExtension");
        
        // Enable SSL at the socket level (for OSS)
        System.setProperty("hazelcast.socket.server.ssl", "true");
        System.setProperty("hazelcast.socket.client.ssl", "true");
        
        Config config = new Config();
        
        // Configure network settings
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().addMember("127.0.0.1");
        
        return config;
    }

    @Provides
    @Singleton
    public HazelcastInstance provideHazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
    
    private Properties loadSSLProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ssl.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new IOException("Could not find ssl.properties in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading SSL properties", e);
        }
        return properties;
    }
}