package com.banking.client.gui;

import com.banking.client.model.Account;
import com.banking.client.service.BankingService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Controller for the login view.
 */
public class LoginController {
    
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    
    @FXML private TextField accountNumberField;
    @FXML private Button loginButton;
    @FXML private Button createAccountButton;
    @FXML private Label statusLabel;
    
    private BankingService bankingService;
    private Stage primaryStage;
    
    public void setBankingService(BankingService bankingService) {
        this.bankingService = bankingService;
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String accountNumber = accountNumberField.getText();
        
        if (accountNumber.isEmpty()) {
            showAlert("Error", "Please enter an account number.");
            return;
        }
        
        try {
            Account account = bankingService.getAccount(accountNumber);
            
            if (account != null) {
                logger.info("Logged in to account: {}", accountNumber);
                openMainView(account);
            }
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            showAlert("Login Error", "Could not log in: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateAccountView.fxml"));
            Parent root = loader.load();
            
            CreateAccountController controller = loader.getController();
            controller.setBankingService(bankingService);
            controller.setLoginController(this);
            
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Error opening create account view: {}", e.getMessage(), e);
            showAlert("Error", "Could not open create account view: " + e.getMessage());
        }
    }
    
    private void openMainView(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.setBankingService(bankingService);
            controller.setAccount(account);
            controller.initialize();
            
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            
        } catch (IOException e) {
            logger.error("Error opening main view: {}", e.getMessage(), e);
            showAlert("Error", "Could not open main view: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
