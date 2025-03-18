package com.banking.client.service;

import com.banking.client.model.Account;
import com.banking.client.model.InsufficientFundsException;
import com.banking.client.model.Transaction;

import java.util.List;

/**
 * Service interface for banking operations.
 * This can be implemented using different communication strategies (REST, RMI).
 */
public interface BankingService {
    
    /**
     * Create a new account.
     */
    Account createAccount(String accountNumber, String accountHolderName, double initialBalance) throws Exception;
    
    /**
     * Get an account by account number.
     */
    Account getAccount(String accountNumber) throws Exception;
    
    /**
     * Get all accounts.
     */
    List<Account> getAllAccounts() throws Exception;
    
    /**
     * Deposit money into an account.
     */
    Account deposit(String accountNumber, double amount) throws Exception;
    
    /**
     * Withdraw money from an account.
     */
    Account withdraw(String accountNumber, double amount) throws Exception, InsufficientFundsException;
    
    /**
     * Transfer money between accounts.
     */
    boolean transfer(String sourceAccountNumber, String destinationAccountNumber, double amount) 
            throws Exception, InsufficientFundsException;
    
    /**
     * Get transaction history for an account.
     */
    List<Transaction> getTransactionHistory(String accountNumber) throws Exception;
}
