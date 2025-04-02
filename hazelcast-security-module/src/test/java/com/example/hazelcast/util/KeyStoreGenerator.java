package com.example.hazelcast.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Utility to generate JKS keystore files for testing
 */
public class KeyStoreGenerator {
    
    public static void main(String[] args) throws Exception {
        // Find the script relative to the project root
        String projectDir = System.getProperty("user.dir");
        String scriptPath = projectDir + "/scripts/generate-certs.sh";
        File script = new File(scriptPath);
        
        if (!script.exists()) {
            System.err.println("Script not found at " + script.getAbsolutePath());
            System.err.println("Please create the script first or run it directly.");
            return;
        }
        
        System.out.println("Executing certificate generation script: " + scriptPath);
        
        ProcessBuilder pb = new ProcessBuilder("bash", scriptPath);
        pb.directory(new File(projectDir)); // Set working directory
        pb.inheritIO(); // Redirect output to console
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            System.err.println("Certificate generation failed with exit code " + exitCode);
        } else {
            System.out.println("Certificates generated successfully");
        }
    }
}