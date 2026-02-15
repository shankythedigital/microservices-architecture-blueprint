
package com.example.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.asset", "com.example.common"})
@EntityScan(basePackages = {"com.example.asset.entity", "com.example.common.entity"})
@EnableJpaRepositories(basePackages = {"com.example.asset.repository", "com.example.common.repository"})
@EnableFeignClients(basePackages = {"com.example.common.client", "com.example.asset.client"})
public class AssetServiceApplication {
    public static void main(String[] args) {
        // Set native library path for Tesseract before Spring Boot starts
        setupNativeLibraryPath();
        
        SpringApplication.run(AssetServiceApplication.class, args);
    }
    
    /**
     * Configure native library path for Tesseract OCR
     * This ensures the JVM can find the native libraries (libtesseract.dylib, libleptonica.dylib)
     */
    private static void setupNativeLibraryPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String existingLibraryPath = System.getProperty("java.library.path");
        
        if (osName.contains("mac")) {
            // macOS - add MacPorts and Homebrew library paths
            String[] macLibraryPaths = {
                "/opt/local/lib",        // MacPorts
                "/opt/homebrew/lib",     // Homebrew (Apple Silicon)
                "/usr/local/lib"         // Homebrew (Intel)
            };
            
            StringBuilder newLibraryPath = new StringBuilder();
            if (existingLibraryPath != null && !existingLibraryPath.isEmpty()) {
                newLibraryPath.append(existingLibraryPath);
            }
            
            for (String path : macLibraryPaths) {
                java.io.File libDir = new java.io.File(path);
                if (libDir.exists() && libDir.isDirectory()) {
                    if (newLibraryPath.length() > 0) {
                        newLibraryPath.append(":");
                    }
                    newLibraryPath.append(path);
                    System.out.println("✅ Added to library path: " + path);
                }
            }
            
            if (newLibraryPath.length() > 0 && !newLibraryPath.toString().equals(existingLibraryPath)) {
                System.setProperty("java.library.path", newLibraryPath.toString());
                System.out.println("📚 Native library path configured: " + newLibraryPath);
            }
        } else if (osName.contains("linux")) {
            // Linux - add common library paths
            String[] linuxLibraryPaths = {
                "/usr/lib",
                "/usr/local/lib",
                "/usr/lib/x86_64-linux-gnu"
            };
            
            StringBuilder newLibraryPath = new StringBuilder();
            if (existingLibraryPath != null && !existingLibraryPath.isEmpty()) {
                newLibraryPath.append(existingLibraryPath);
            }
            
            for (String path : linuxLibraryPaths) {
                java.io.File libDir = new java.io.File(path);
                if (libDir.exists() && libDir.isDirectory()) {
                    if (newLibraryPath.length() > 0) {
                        newLibraryPath.append(":");
                    }
                    newLibraryPath.append(path);
                }
            }
            
            if (newLibraryPath.length() > 0 && !newLibraryPath.toString().equals(existingLibraryPath)) {
                System.setProperty("java.library.path", newLibraryPath.toString());
            }
        }
        
        // Also set TESSDATA_PREFIX if not already set
        if (System.getenv("TESSDATA_PREFIX") == null || System.getenv("TESSDATA_PREFIX").isEmpty()) {
            if (osName.contains("mac")) {
                // Try MacPorts first, then Homebrew
                String[] tessDataPaths = {
                    "/opt/local/share/tessdata",
                    "/opt/homebrew/share/tessdata",
                    "/usr/local/share/tessdata"
                };
                
                for (String path : tessDataPaths) {
                    java.io.File tessDataDir = new java.io.File(path);
                    if (tessDataDir.exists() && tessDataDir.isDirectory()) {
                        System.setProperty("TESSDATA_PREFIX", path);
                        System.out.println("📁 TESSDATA_PREFIX set to: " + path);
                        break;
                    }
                }
            }
        }
    }
}

