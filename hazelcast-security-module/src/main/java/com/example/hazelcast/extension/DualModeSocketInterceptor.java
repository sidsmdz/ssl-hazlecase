package com.example.hazelcast.extension;

import com.hazelcast.nio.MemberSocketInterceptor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * Socket interceptor that can handle both SSL and non-SSL connections
 * on the same port based on connection properties.
 */
public class DualModeSocketInterceptor implements MemberSocketInterceptor {
    private SSLSocketFactory sslSocketFactory;
    private boolean initialized = false;
    private Properties properties;

    public DualModeSocketInterceptor() {
        // Empty constructor required
    }

    @Override
    public void init(Properties properties) {
        this.properties = properties;
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null); // Use default keystores from system properties
            sslSocketFactory = context.getSocketFactory();
            initialized = true;
            System.out.println("DualModeSocketInterceptor initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing SSL socket factory: " + e.getMessage());
        }
    }

    @Override
    public void onAccept(Socket acceptedSocket) {
        if (acceptedSocket instanceof SSLSocket) {
            System.out.println("Accepted SSL connection from " + acceptedSocket.getRemoteSocketAddress());
        } else {
            System.out.println("Accepted plain connection from " + acceptedSocket.getRemoteSocketAddress());
        }
    }

    @Override
    public void onConnect(Socket connectedSocket) {
        String clientType = System.getProperty("hazelcast.client.ssl.enabled", "false");
        if (Boolean.parseBoolean(clientType) && sslSocketFactory != null) {
            try {
                // Wrap the socket with SSL if the client is SSL-enabled
                SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                        connectedSocket, 
                        connectedSocket.getInetAddress().getHostAddress(), 
                        connectedSocket.getPort(),
                        true); // autoClose=true
                sslSocket.startHandshake();
                System.out.println("Upgraded connection to SSL for " + connectedSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.err.println("Failed to wrap socket with SSL: " + e.getMessage());
            }
        } else {
            System.out.println("Connected with plain socket to " + connectedSocket.getRemoteSocketAddress());
        }
    }
}