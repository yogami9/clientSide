package com.banking.client.gui;

import com.banking.client.model.Account;
import com.banking.client.model.InsufficientFundsException;
import com.banking.client.model.Transaction;
import com.banking.client.service.BankingService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main view.
 */
public class MainController {
    
    private static final Logger logger = LogManager.getLogger(MainController.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @FXML private Label accountNumberLabel;
    @FXML private Label accountHolderLabel;
    @FXML private Label balanceLabel;
    
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    
    private BankingService bankingService;
    private Account account;
    
    public void setBankingService(BankingService bankingService) {
        this.bankingService = bankingService;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public void initialize() {
        // Set up account information
        accountNumberLabel.setText(account.getAccountNumber());
        accountHolderLabel.setText(account.getAccountHolderName());
        updateBalanceLabel();
        
        // Set up transactions table
        dateColumn.setCellValueFactory(cellData -> {
            String formattedDate = DATE_FORMAT.format(cellData.getValue().getTimestamp());
            return javafx.beans.binding.Bindings.createStringBinding(() -> formattedDate);
        });
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Load transactions
        loadTransactions();
    }
    
    @FXML
    private void handleDeposit(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Enter Deposit Amount");
        dialog.setContentText("Amount:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double amount = Double.parseDouble(result.get());
                
                if (amount <= 0) {
                    showAlert("Error", "Deposit amount must be positive.");
                    return;
                }
                
                account = bankingService.deposit(account.getAccountNumber(), amount);
                updateBalanceLabel();
                loadTransactions();
                
                showAlert("Success", "Deposit successful!");
                
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid number.");
            } catch (Exception e) {
                logger.error("Deposit error: {}", e.getMessage(), e);
                showAlert("Error", "Deposit failed: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleWithdraw(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Enter Withdrawal Amount");
        dialog.setContentText("Amount:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double amount = Double.parseDouble(result.get());
                
                if (amount <= 0) {
                    showAlert("Error", "Withdrawal amount must be positive.");
                    return;
                }
                
                account = bankingService.withdraw(account.getAccountNumber(), amount);
                updateBalanceLabel();
                loadTransactions();
                
                showAlert("Success", "Withdrawal successful!");
                
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid number.");
            } catch (InsufficientFundsException e) {
                showAlert("Error", "Insufficient funds! Requested: $" + e.getRequestedAmount() + 
                        " Available: $" + e.getAvailableBalance());
            } catch (Exception e) {
                logger.error("Withdrawal error: {}", e.getMessage(), e);
                showAlert("Error", "Withdrawal failed: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleTransfer(ActionEvent event) {
        Dialog<TransferData> dialog = new Dialog<>();
        dialog.setTitle("Transfer");
        dialog.setHeaderText("Transfer Money");
        
        ButtonType transferButtonType = new ButtonType("Transfer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(transferButtonType, ButtonType.CANCEL);
        
        TextField accountField = new TextField();
        accountField.setPromptText("Destination Account Number");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        
        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(10, 
                new Label("Destination Account:"), accountField, 
                new Label("Amount:"), amountField));
        
        Platform.runLater(accountField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == transferButtonType) {
                return new TransferData(accountField.getText(), amountField.getText());
            }
            return null;
        });
        
        Optional<TransferData> result = dialog.showAndWait();
        
        result.ifPresent(transferData -> {
            try {
                String destinationAccount = transferData.accountNumber;
                double amount = Double.parseDouble(transferData.amount);
                
                if (destinationAccount.isEmpty()) {
                    showAlert("Error", "Please enter a destination account number.");
                    return;
                }
                
                if (amount <= 0) {
                    showAlert("Error", "Transfer amount must be positive.");
                    return;
                }
                
                boolean success = bankingService.transfer(
                        account.getAccountNumber(), destinationAccount, amount);
                
                if (success) {
                    // Update account after transfer
                    account = bankingService.getAccount(account.getAccountNumber());
                    updateBalanceLabel();
                    loadTransactions();
                    
                    showAlert("Success", "Transfer successful!");
                } else {
                    showAlert("Error", "Transfer failed.");
                }
                
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid amount.");
            } catch (InsufficientFundsException e) {
                showAlert("Error", "Insufficient funds! Requested: $" + e.getRequestedAmount() + 
                        " Available: $" + e.getAvailableBalance());
            } catch (Exception e) {
                logger.error("Transfer error: {}", e.getMessage(), e);
                showAlert("Error", "Transfer failed: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            account = bankingService.getAccount(account.getAccountNumber());
            updateBalanceLabel();
            loadTransactions();
        } catch (Exception e) {
            logger.error("Refresh error: {}", e.getMessage(), e);
            showAlert("Error", "Failed to refresh data: " + e.getMessage());
        }
    }
    
    private void updateBalanceLabel() {
        balanceLabel.setText(String.format("$%.2f", account.getBalance()));
    }
    
    private void loadTransactions() {
        try {
            List<Transaction> transactions = bankingService.getTransactionHistory(account.getAccountNumber());
            transactionsTable.setItems(FXCollections.observableArrayList(transactions));
        } catch (Exception e) {
            logger.error("Error loading transactions: {}", e.getMessage(), e);
            showAlert("Error", "Failed to load transactions: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static class TransferData {
        private final String accountNumber;
        private final String amount;
        
        public TransferData(String accountNumber, String amount) {
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
    }
}
