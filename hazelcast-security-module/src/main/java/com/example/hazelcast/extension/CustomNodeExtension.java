package com.example.hazelcast.extension;

import com.google.inject.Inject;
import com.hazelcast.instance.impl.DefaultNodeExtension;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.nio.ssl.SSLContextFactory;

import java.util.Properties;

/**
 * Custom NodeExtension that provides SSL configuration.
 */
public class CustomNodeExtension extends DefaultNodeExtension {
    
    private final Properties sslProperties;
    
    public CustomNodeExtension(Node node) {
        super(node);
        // Since we can't use Guice injection directly in NodeExtension constructor
        // We'll use a static injector access pattern through a provider class
        this.sslProperties = SSLPropertiesProvider.getSSLProperties();
        getSSLContextFactory();
    }
    
    // This method is what Hazelcast 5.3.6 actually calls
    // @Override
    public SSLContextFactory getSSLContextFactory() {
        // Get SSLContextFactory from Guice via provider
        CustomSSLContextFactory sslContextFactory = SSLContextFactoryProvider.getSSLContextFactory();
        sslContextFactory.init(sslProperties);
        return sslContextFactory;
    }
}