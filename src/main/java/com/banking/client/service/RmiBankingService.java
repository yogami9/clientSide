package com.banking.client.service;

import com.banking.client.model.Account;
import com.banking.client.model.InsufficientFundsException;
import com.banking.client.model.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * RMI implementation of the BankingService.
 * This communicates with the application tier using RMI.
 */
public class RmiBankingService implements BankingService {
    
    private static final Logger logger = LogManager.getLogger(RmiBankingService.class);
    private final String host;
    private final int port;
    private Registry registry;
    
    // We'll need references to these remote interfaces - this is a simplified representation
    // In a real implementation, you would need to have the actual RMI interfaces available
    private interface RemoteAccount {
        String getAccountNumber() throws RemoteException;
        String getAccountHolderName() throws RemoteException;
        double getBalance() throws RemoteException;
        double deposit(double amount) throws RemoteException;
        double withdraw(double amount) throws RemoteException, com.banking.client.model.InsufficientFundsException;
        boolean transfer(RemoteAccount destinationAccount, double amount) throws RemoteException, com.banking.client.model.InsufficientFundsException;
        List<Transaction> getTransactionHistory() throws RemoteException;
    }
    
    private interface AccountRegistry {
        RemoteAccount getAccount(String accountNumber) throws RemoteException;
        RemoteAccount createAccount(String accountNumber, String accountHolderName, double initialBalance) throws RemoteException;
    }
    
    public RmiBankingService(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            this.registry = LocateRegistry.getRegistry(host, port);
            logger.info("Connected to RMI registry at {}:{}", host, port);
        } catch (RemoteException e) {
            logger.error("Failed to connect to RMI registry: {}", e.getMessage(), e);
        }
    }
    
    private AccountRegistry getAccountRegistry() throws Exception {
        try {
            return (AccountRegistry) registry.lookup("AccountRegistry");
        } catch (Exception e) {
            logger.error("Failed to locate AccountRegistry: {}", e.getMessage(), e);
            throw new Exception("Failed to connect to banking service: " + e.getMessage());
        }
    }
    
    @Override
    public Account createAccount(String accountNumber, String accountHolderName, double initialBalance) throws Exception {
        logger.info("Creating account via RMI: {}", accountNumber);
        
        try {
            RemoteAccount remoteAccount = getAccountRegistry().createAccount(accountNumber, accountHolderName, initialBalance);
            
            if (remoteAccount != null) {
                Account account = new Account();
                account.setAccountNumber(remoteAccount.getAccountNumber());
                account.setAccountHolderName(remoteAccount.getAccountHolderName());
                account.setBalance(remoteAccount.getBalance());
                return account;
            } else {
                throw new Exception("Failed to create account");
            }
        } catch (RemoteException e) {
            logger.error("RMI error during account creation: {}", e.getMessage(), e);
            throw new Exception("Failed to create account: " + e.getMessage());
        }
    }
    
    @Override
    public Account getAccount(String accountNumber) throws Exception {
        logger.info("Getting account via RMI: {}", accountNumber);
        
        try {
            RemoteAccount remoteAccount = getAccountRegistry().getAccount(accountNumber);
            
            if (remoteAccount != null) {
                Account account = new Account();
                account.setAccountNumber(remoteAccount.getAccountNumber());
                account.setAccountHolderName(remoteAccount.getAccountHolderName());
                account.setBalance(remoteAccount.getBalance());
                return account;
            } else {
                throw new Exception("Account not found: " + accountNumber);
            }
        } catch (RemoteException e) {
            logger.error("RMI error while getting account: {}", e.getMessage(), e);
            throw new Exception("Failed to get account: " + e.getMessage());
        }
    }
    
    @Override
    public List<Account> getAllAccounts() throws Exception {
        // This operation would typically require extending the RMI interface
        // For now, we'll throw an exception
        throw new UnsupportedOperationException("Getting all accounts is not supported via RMI");
    }
    
    @Override
    public Account deposit(String accountNumber, double amount) throws Exception {
        logger.info("Depositing {} to account {} via RMI", amount, accountNumber);
        
        try {
            RemoteAccount remoteAccount = getAccountRegistry().getAccount(accountNumber);
            
            if (remoteAccount != null) {
                double newBalance = remoteAccount.deposit(amount);
                
                Account account = new Account();
                account.setAccountNumber(remoteAccount.getAccountNumber());
                account.setAccountHolderName(remoteAccount.getAccountHolderName());
                account.setBalance(newBalance);
                return account;
            } else {
                throw new Exception("Account not found: " + accountNumber);
            }
        } catch (RemoteException e) {
            logger.error("RMI error during deposit: {}", e.getMessage(), e);
            throw new Exception("Failed to deposit: " + e.getMessage());
        }
    }
    
    @Override
    public Account withdraw(String accountNumber, double amount) throws Exception, InsufficientFundsException {
        logger.info("Withdrawing {} from account {} via RMI", amount, accountNumber);
        
        try {
            RemoteAccount remoteAccount = getAccountRegistry().getAccount(accountNumber);
            
            if (remoteAccount != null) {
                double newBalance = remoteAccount.withdraw(amount);
                
                Account account = new Account();
                account.setAccountNumber(remoteAccount.getAccountNumber());
                account.setAccountHolderName(remoteAccount.getAccountHolderName());
                account.setBalance(newBalance);
                return account;
            } else {
                throw new Exception("Account not found: " + accountNumber);
            }
        } catch (com.banking.client.model.InsufficientFundsException e) {
            throw new InsufficientFundsException(e.getRequestedAmount(), e.getAvailableBalance());
        } catch (RemoteException e) {
            logger.error("RMI error during withdrawal: {}", e.getMessage(), e);
            throw new Exception("Failed to withdraw: " + e.getMessage());
        }
    }
    
    @Override
    public boolean transfer(String sourceAccountNumber, String destinationAccountNumber, double amount) 
            throws Exception, InsufficientFundsException {
        logger.info("Transferring {} from account {} to account {} via RMI", 
                amount, sourceAccountNumber, destinationAccountNumber);
        
        try {
            RemoteAccount sourceAccount = getAccountRegistry().getAccount(sourceAccountNumber);
            RemoteAccount destinationAccount = getAccountRegistry().getAccount(destinationAccountNumber);
            
            if (sourceAccount == null) {
                throw new Exception("Source account not found: " + sourceAccountNumber);
            }
            
            if (destinationAccount == null) {
                throw new Exception("Destination account not found: " + destinationAccountNumber);
            }
            
            return sourceAccount.transfer(destinationAccount, amount);
        } catch (com.banking.client.model.InsufficientFundsException e) {
            throw new InsufficientFundsException(e.getRequestedAmount(), e.getAvailableBalance());
        } catch (RemoteException e) {
            logger.error("RMI error during transfer: {}", e.getMessage(), e);
            throw new Exception("Failed to transfer: " + e.getMessage());
        }
    }
    
    @Override
    public List<Transaction> getTransactionHistory(String accountNumber) throws Exception {
        logger.info("Getting transaction history for account {} via RMI", accountNumber);
        
        try {
            RemoteAccount remoteAccount = getAccountRegistry().getAccount(accountNumber);
            
            if (remoteAccount != null) {
                return remoteAccount.getTransactionHistory();
            } else {
                throw new Exception("Account not found: " + accountNumber);
            }
        } catch (RemoteException e) {
            logger.error("RMI error while getting transaction history: {}", e.getMessage(), e);
            throw new Exception("Failed to get transaction history: " + e.getMessage());
        }
    }
}
