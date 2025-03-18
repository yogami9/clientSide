package com.banking.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Factory class for creating BankingService instances.
 */
public class BankingServiceFactory {
    
    private static final Logger logger = LogManager.getLogger(BankingServiceFactory.class);
    
    /**
     * Create a BankingService based on configuration.
     * 
     * @return BankingService instance
     */
    public static BankingService createService() {
        try {
            Properties properties = loadProperties();
            
            String connectionType = properties.getProperty("connection.type", "REST");
            
            if ("RMI".equalsIgnoreCase(connectionType)) {
                String host = properties.getProperty("rmi.host", "localhost");
                int port = Integer.parseInt(properties.getProperty("rmi.port", "1099"));
                return createRmiService(host, port);
            } else {
                String baseUrl = properties.getProperty("rest.api.url", "http://localhost:8080");
                return createRestService(baseUrl);
            }
        } catch (IOException e) {
            logger.error("Failed to load properties: {}", e.getMessage(), e);
            // Default to REST with localhost
            return createRestService("http://localhost:8080");
        }
    }
    
    /**
     * Create a REST-based BankingService.
     * 
     * @param baseUrl Base URL of the application tier
     * @return BankingService instance
     */
    public static BankingService createRestService(String baseUrl) {
        logger.info("Creating REST banking service with URL: {}", baseUrl);
        return new RestBankingService(baseUrl);
    }
    
    /**
     * Create an RMI-based BankingService.
     * 
     * @param host Host of the RMI registry
     * @param port Port of the RMI registry
     * @return BankingService instance
     */
    public static BankingService createRmiService(String host, int port) {
        logger.info("Creating RMI banking service with host: {}, port: {}", host, port);
        return new RmiBankingService(host, port);
    }
    
    /**
     * Load properties from configuration file.
     * 
     * @return Properties
     * @throws IOException If properties cannot be loaded
     */
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        
        try (InputStream input = BankingServiceFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            
            properties.load(input);
        }
        
        // Check for environment variables that should override properties
        String restApiUrl = System.getenv("REST_API_URL");
        if (restApiUrl != null && !restApiUrl.isEmpty()) {
            properties.setProperty("rest.api.url", restApiUrl);
        }
        
        String connectionType = System.getenv("CONNECTION_TYPE");
        if (connectionType != null && !connectionType.isEmpty()) {
            properties.setProperty("connection.type", connectionType);
        }
        
        String rmiHost = System.getenv("RMI_HOST");
        if (rmiHost != null && !rmiHost.isEmpty()) {
            properties.setProperty("rmi.host", rmiHost);
        }
        
        String rmiPort = System.getenv("RMI_PORT");
        if (rmiPort != null && !rmiPort.isEmpty()) {
            properties.setProperty("rmi.port", rmiPort);
        }
        
        return properties;
    }
}
