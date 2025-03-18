package com.banking.client.gui;

import com.banking.client.service.BankingService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the create account view.
 */
public class CreateAccountController {
    
    private static final Logger logger = LogManager.getLogger(CreateAccountController.class);
    
    @FXML private TextField accountNumberField;
    @FXML private TextField accountHolderField;
    @FXML private TextField initialBalanceField;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    
    private BankingService bankingService;
    private LoginController loginController;
    
    public void setBankingService(BankingService bankingService) {
        this.bankingService = bankingService;
    }
    
    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }
    
    @FXML
    private void handleCreate(ActionEvent event) {
        String accountNumber = accountNumberField.getText();
        String accountHolder = accountHolderField.getText();
        String initialBalanceText = initialBalanceField.getText();
        
        if (accountNumber.isEmpty() || accountHolder.isEmpty() || initialBalanceText.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }
        
        try {
            double initialBalance = Double.parseDouble(initialBalanceText);
            
            if (initialBalance < 0) {
                showAlert("Error", "Initial balance cannot be negative.");
                return;
            }
            
            bankingService.createAccount(accountNumber, accountHolder, initialBalance);
            
            showAlert("Success", "Account created successfully!");
            
            // Return to login view
            loginController.setPrimaryStage(cancelButton.getScene().getWindow());
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for initial balance.");
        } catch (Exception e) {
            logger.error("Account creation error: {}", e.getMessage(), e);
            showAlert("Error", "Failed to create account: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        // Return to login view
        loginController.setPrimaryStage(cancelButton.getScene().getWindow());
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
