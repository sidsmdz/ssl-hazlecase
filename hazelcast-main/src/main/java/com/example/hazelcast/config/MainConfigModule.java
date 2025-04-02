package com.example.hazelcast.config;

import com.example.hazelcast.extension.CustomSSLContextFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ssl.SSLContextFactory;

import java.util.Properties;

/**
 * Main configuration module that provides Hazelcast settings
 */
public class MainConfigModule extends AbstractModule {
    
    private final Properties sslProperties;
    
    public MainConfigModule(Properties sslProperties) {
        this.sslProperties = sslProperties;
    }

    @Override
    protected void configure() {
        // Bind SSL classes from security module
        bind(SSLContextFactory.class).to(CustomSSLContextFactory.class).in(Singleton.class);
    }

    /**
     * Provides SSL properties to be used by the security module
     */
    @Provides
    @Singleton
    public Properties provideSSLProperties() {
        return sslProperties;
    }

    /**
     * Provides Hazelcast configuration that works with OSGI properties
     */
    @Provides
    @Singleton
    public Config provideHazelcastConfig() {
        Config config = new Config();
        
        // Configure network settings - OSGI properties will override these
        NetworkConfig networkConfig = config.getNetworkConfig();
        // Use automatic port detection to allow OSGI properties to control
        networkConfig.setPortAutoIncrement(true);
        
        // Configure clustering
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().addMember("127.0.0.1");
        
        return config;
    }

    /**
     * Provides a singleton HazelcastInstance
     */
    @Provides
    @Singleton
    public HazelcastInstance provideHazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}