package com.example.hazelcast.config;

import com.example.hazelcast.extension.CustomSSLContextFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.EndpointConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ssl.SSLContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HazelcastConfigModule extends AbstractModule {

    private static final int DEFAULT_PORT = 5701;
    private static final int SSL_PORT = 5702;

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
        
        Config config = new Config();
        
        // For Hazelcast 5.3.6, we'll use the standard network configuration
        // and use the socket-level properties for SSL
        NetworkConfig networkConfig = config.getNetworkConfig();
        
        // Set SSL system properties for SSL socket
        System.setProperty("hazelcast.socket.server.ssl", "true");
        System.setProperty("hazelcast.socket.client.ssl", "true");
        
        // Configure port
        networkConfig.setPort(DEFAULT_PORT);
        networkConfig.setPortAutoIncrement(true);
        
        // Configure discovery
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().addMember("127.0.0.1");
        
        // Create a second member with SSL port explicitly configured
        Config sslConfig = new Config();
        sslConfig.setClusterName(config.getClusterName());
        sslConfig.getNetworkConfig().setPort(SSL_PORT);
        sslConfig.getNetworkConfig().setPortAutoIncrement(true);
        
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