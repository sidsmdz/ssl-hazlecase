package com.example.hazelcast.extension;

import com.hazelcast.instance.impl.DefaultNodeExtension;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.nio.MemberSocketInterceptor;
import com.hazelcast.nio.ssl.SSLContextFactory;

/**
 * Custom node extension that provides SSL/TLS capabilities to Hazelcast
 * using a dual-mode socket interceptor.
 */
public class CustomNodeExtension extends DefaultNodeExtension {
    
    public CustomNodeExtension(Node node) {
        super(node);
        System.out.println("CustomNodeExtension initialized with dual socket support");
    }

    // @Override
    public SSLContextFactory getSSLContextFactory() {
        return SecurityConfigProvider.getSSLContextFactory();
    }

    // @Override
    public MemberSocketInterceptor getMemberSocketInterceptor() {
        return new DualModeSocketInterceptor();
    }
}