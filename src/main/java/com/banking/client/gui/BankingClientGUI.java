package com.banking.client.gui;

import com.banking.client.service.BankingService;
import com.banking.client.service.BankingServiceFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BankingClientGUI extends Application {
    
    private static final Logger logger = LogManager.getLogger(BankingClientGUI.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Banking Client GUI");
        
        // Create the banking service using the factory
        BankingService bankingService = BankingServiceFactory.createService();
        
        // Load the FXML and set the controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        // Get the controller and set the banking service
        LoginController controller = loader.getController();
        controller.setBankingService(bankingService);
        controller.setPrimaryStage(primaryStage);
        
        // Set up the stage
        primaryStage.setTitle("Banking Client");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        
        logger.info("Banking Client GUI started");
    }
}
