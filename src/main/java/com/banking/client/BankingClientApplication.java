package com.banking.client;

import com.banking.client.gui.BankingClientGUI;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Application;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main class for the Banking Client application.
 * This launches the JavaFX GUI and a simple HTTP server for cloud deployments.
 */
public class BankingClientApplication {
    
    private static final Logger logger = LogManager.getLogger(BankingClientApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Banking Client application");
        
        // Start HTTP server in a background thread
        startHttpServer();
        
        // Launch the JavaFX application
        Application.launch(BankingClientGUI.class, args);
    }
    
    /**
     * Starts a simple HTTP server to satisfy Render's port binding requirement
     */
    private static void startHttpServer() {
        new Thread(() -> {
            try {
                // Get port from environment variable or use default
                int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "10000"));
                
                // Try multiple ports if the default is in use
                HttpServer server = null;
                for (int attempts = 0; attempts < 10; attempts++) {
                    try {
                        server = HttpServer.create(new InetSocketAddress(port + attempts), 0);
                        port += attempts;
                        break;
                    } catch (IOException e) {
                        if (attempts == 9) {
                            logger.error("Failed to find an available port after 10 attempts");
                            return;
                        }
                    }
                }
                
                if (server == null) {
                    logger.error("Could not create HTTP server");
                    return;
                }
                
                server.createContext("/", (exchange) -> {
                    String response = "Banking Client is running";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                });
                
                // Add health check endpoint
                server.createContext("/health", (exchange) -> {
                    String response = "{\"status\":\"UP\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                });
                
                server.setExecutor(Executors.newFixedThreadPool(10));
                server.start();
                
                logger.info("HTTP server started on port {}", port);
                
                // Register shutdown hook to stop the server when the application exits
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Stopping HTTP server");
                    server.stop(0);
                }));
                
            } catch (Exception e) {
                logger.error("Failed to start HTTP server: {}", e.getMessage(), e);
            }
        }).start();
    }
}
