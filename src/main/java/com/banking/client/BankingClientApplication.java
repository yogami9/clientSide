package com.banking.client;

import com.banking.client.gui.BankingClientGUI;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class for the Banking Client application.
 * This launches the JavaFX GUI.
 */
public class BankingClientApplication {
    
    private static final Logger logger = LogManager.getLogger(BankingClientApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Banking Client application");
        Application.launch(BankingClientGUI.class, args);
    }
}
