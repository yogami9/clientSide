package com.banking.client.service;

import com.banking.client.model.Account;
import com.banking.client.model.InsufficientFundsException;
import com.banking.client.model.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST implementation of the BankingService.
 * This communicates with the application tier using HTTP/REST.
 */
public class RestBankingService implements BankingService {
    
    private static final Logger logger = LogManager.getLogger(RestBankingService.class);
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    
    public RestBankingService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public Account createAccount(String accountNumber, String accountHolderName, double initialBalance) throws Exception {
        logger.info("Creating account: {}", accountNumber);
        
        Account account = new Account(accountNumber, accountHolderName, initialBalance);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/api/accounts");
            
            String json = objectMapper.writeValueAsString(account);
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                
                if (statusCode == 201) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(responseBody, Account.class);
                } else {
                    throw new Exception("Failed to create account: " + statusCode);
                }
            }
        }
    }
    
    @Override
    public Account getAccount(String accountNumber) throws Exception {
        logger.info("Getting account: {}", accountNumber);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + "/api/accounts/" + accountNumber);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(responseBody, Account.class);
                } else if (statusCode == 404) {
                    throw new Exception("Account not found: " + accountNumber);
                } else {
                    throw new Exception("Failed to get account: " + statusCode);
                }
            }
        }
    }
    
    @Override
    public List<Account> getAllAccounts() throws Exception {
        logger.info("Getting all accounts");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + "/api/accounts");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(responseBody, new TypeReference<List<Account>>() {});
                } else {
                    throw new Exception("Failed to get accounts: " + statusCode);
                }
            }
        }
    }
    
    @Override
    public Account deposit(String accountNumber, double amount) throws Exception {
        logger.info("Depositing {} to account {}", amount, accountNumber);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/api/accounts/" + accountNumber + "/deposit");
            
            Map<String, Double> depositData = new HashMap<>();
            depositData.put("amount", amount);
            
            String json = objectMapper.writeValueAsString(depositData);
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(responseBody, Account.class);
                } else if (statusCode == 404) {
                    throw new Exception("Account not found: " + accountNumber);
                } else {
                    throw new Exception("Failed to deposit: " + statusCode);
                }
            }
        }
    }
    
    @Override
    public Account withdraw(String accountNumber, double amount) throws Exception, InsufficientFundsException {
        logger.info("Withdrawing {} from account {}", amount, accountNumber);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/api/accounts/" + accountNumber + "/withdraw");
            
            Map<String, Double> withdrawData = new HashMap<>();
            withdrawData.put("amount", amount);
            
            String json = objectMapper.writeValueAsString(withdrawData);
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 200) {
                    return objectMapper.readValue(responseBody, Account.class);
                } else if (statusCode == 400) {
                    // Check if it's an insufficient funds error
                    Map<String, Object> errorResponse = objectMapper.readValue(responseBody, 
                            new TypeReference<Map<String, Object>>() {});
                    
                    if (errorResponse.containsKey("error") && 
                            "Insufficient funds".equals(errorResponse.get("error"))) {
                        double requestedAmount = ((Number) errorResponse.get("requestedAmount")).doubleValue();
                        double availableBalance = ((Number) errorResponse.get("availableBalance")).doubleValue();
                        throw new InsufficientFundsException(requestedAmount, availableBalance);
                    }
                    throw new Exception("Bad request: " + responseBody);
                } else if (statusCode == 404) {
                    throw new Exception("Account not found: " + accountNumber);
                } else {
                    throw new Exception("Failed to withdraw: " + statusCode);
                }
            }
        }
    }
    
    @Override
    public boolean transfer(String sourceAccountNumber, String destinationAccountNumber, double amount) 
            throws Exception, InsufficientFundsException {
        logger.info("Transferring {} from account {} to account {}", 
                amount, sourceAccountNumber, destinationAccountNumber);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/api/accounts/" + sourceAccountNumber + "/transfer");
            
            Map<String, Object> transferData = new HashMap<>();
            transferData.put("destinationAccountNumber", destinationAccountNumber);
            transferData.put("amount", amount);
            
            String json = objectMapper.writeValueAsString(transferData);
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 200) {
                    Map<String, Object> successResponse = objectMapper.readValue(responseBody, 
                            new TypeReference<Map<String, Object>>() {});
                    return (boolean) successResponse.get("success");
                } else if (statusCode == 400) {
                    // Check if it's an insufficient funds error
                    Map<String, Object> errorResponse = objectMapper.readValue(responseBody, 
                            new TypeReference<Map<String, Object>>() {});
                    
                    if (errorResponse.containsKey("error") && 
                            "Insufficient funds".equals(errorResponse.get("error"))) {
                        double requestedAmount = ((Number) errorResponse.get("requestedAmount")).doubleValue();
                        double availableBalance = ((Number) errorResponse.get("availableBalance")).doubleValue();
                        throw new InsufficientFundsException(requestedAmount, availableBalance);
                    }
                    throw new Exception("Bad request: " + responseBody);
                } else if (statusCode == 404) {
                    throw new Exception("Account not found");
                } else {
                    throw new Exception("Failed to transfer: " + statusCode);
                }
            }
        }
    }
    
    @Override
    public List<Transaction> getTransactionHistory(String accountNumber) throws Exception {
        logger.info("Getting transaction history for account {}", accountNumber);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + "/api/accounts/" + accountNumber + "/transactions");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    return objectMapper.readValue(responseBody, new TypeReference<List<Transaction>>() {});
                } else if (statusCode == 404) {
                    throw new Exception("Account not found: " + accountNumber);
                } else {
                    throw new Exception("Failed to get transaction history: " + statusCode);
                }
            }
        }
    }
}
