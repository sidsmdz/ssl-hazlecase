package com.example.hazelcast.extension;

import com.hazelcast.nio.ssl.SSLContextFactory;

import javax.inject.Inject;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Properties;

/**
 * Implementation of SSLContextFactory for SSL/TLS support in Hazelcast
 */
public class CustomSSLContextFactory implements SSLContextFactory {

    private Properties properties;

    @Inject
    public CustomSSLContextFactory(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public void init(Properties properties) {
        // If properties are provided here, use them, otherwise keep the injected ones
        if (properties != null && !properties.isEmpty()) {
            this.properties = properties;
        }
    }

    @Override
    public SSLContext getSSLContext() {
        try {
            String protocol = properties.getProperty("protocol", "TLS");
            String keyManagerAlgorithm = properties.getProperty("keyManagerAlgorithm", KeyManagerFactory.getDefaultAlgorithm());
            String trustManagerAlgorithm = properties.getProperty("trustManagerAlgorithm", TrustManagerFactory.getDefaultAlgorithm());
            
            KeyManager[] keyManagers = getKeyManagers(keyManagerAlgorithm);
            TrustManager[] trustManagers = getTrustManagers(trustManagerAlgorithm);
            
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, null);
            
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Error creating SSL context", e);
        }
    }

    private KeyManager[] getKeyManagers(String algorithm) throws Exception {
        // Get keystore properties from JVM system properties
        String keystorePath = System.getProperty("javax.net.ssl.keyStore");
        String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        String keystoreType = properties.getProperty("keyStoreType", "JKS");
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        KeyStore ks = KeyStore.getInstance(keystoreType);
        
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, keystorePassword.toCharArray());
        }
        
        kmf.init(ks, keystorePassword.toCharArray());
        return kmf.getKeyManagers();
    }

    private TrustManager[] getTrustManagers(String algorithm) throws Exception {
        // Get truststore properties from JVM system properties
        String truststorePath = System.getProperty("javax.net.ssl.trustStore");
        String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        String truststoreType = properties.getProperty("trustStoreType", "JKS");
        
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        KeyStore ts = KeyStore.getInstance(truststoreType);
        
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            ts.load(fis, truststorePassword.toCharArray());
        }
        
        tmf.init(ts);
        return tmf.getTrustManagers();
    }
}