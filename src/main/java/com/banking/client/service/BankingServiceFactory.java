package com.banking.client.service;

/**
 * Factory class for creating BankingService instances.
 */
public class BankingServiceFactory {
    
    /**
     * Create a REST-based BankingService.
     * 
     * @param baseUrl Base URL of the application tier
     * @return BankingService instance
     */
    public static BankingService createRestService(String baseUrl) {
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
        return new RmiBankingService(host, port);
    }
}
