package com.example.hazelcast.extension;
import com.google.inject.Inject;
import com.hazelcast.nio.ssl.SSLContextFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Properties;

public class CustomSSLContextFactory implements SSLContextFactory {

    private Properties sslProperties;
    
    @Inject
    public CustomSSLContextFactory() {
        // Inject-ready constructor for Guice
    }

    @Override
    public void init(Properties properties) {
        this.sslProperties = properties;
    }

    @Override
    public SSLContext getSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance(sslProperties.getProperty("keyStoreType", "JKS"));
            try (FileInputStream fis = new FileInputStream(sslProperties.getProperty("keyStore"))) {
                keyStore.load(fis, sslProperties.getProperty("keyStorePassword").toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    sslProperties.getProperty("keyManagerAlgorithm", "SunX509"));
            keyManagerFactory.init(keyStore, sslProperties.getProperty("keyStorePassword").toCharArray());

            KeyStore trustStore = KeyStore.getInstance(sslProperties.getProperty("trustStoreType", "JKS"));
            try (FileInputStream fis = new FileInputStream(sslProperties.getProperty("trustStore"))) {
                trustStore.load(fis, sslProperties.getProperty("trustStorePassword").toCharArray());
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    sslProperties.getProperty("trustManagerAlgorithm", "SunX509"));
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance(sslProperties.getProperty("protocol", "TLS"));
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }
}