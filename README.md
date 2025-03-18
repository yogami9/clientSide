# Banking Client

Modern client application for the three-tier banking system.

## Features

- Connect to the application layer using REST API or RMI
- View account information and transaction history
- Perform banking operations (deposit, withdraw, transfer)
- Create new accounts
- Modern JavaFX user interface

## Project Structure

```
banking-client/
├── pom.xml                                  # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── banking/
│   │   │           └── client/
│   │   │               ├── BankingClientApplication.java    # Main application class
│   │   │               ├── gui/                             # GUI controllers
│   │   │               │   ├── BankingClientGUI.java        # JavaFX application
│   │   │               │   ├── LoginController.java         # Login view controller
│   │   │               │   ├── MainController.java          # Main view controller
│   │   │               │   └── CreateAccountController.java # Create account controller
│   │   │               ├── model/                           # Data models
│   │   │               │   ├── Account.java                 # Account model
│   │   │               │   └── Transaction.java             # Transaction model
│   │   │               └── service/                         # Services
│   │   │                   ├── BankingService.java          # Service interface
│   │   │                   ├── BankingServiceFactory.java   # Service factory
│   │   │                   ├── RestBankingService.java      # REST implementation
│   │   │                   └── RmiBankingService.java       # RMI implementation
│   │   └── resources/
│   │       ├── fxml/                                        # JavaFX FXML layouts
│   │       │   ├── LoginView.fxml                           # Login view
│   │       │   ├── MainView.fxml                            # Main view
│   │       │   └── CreateAccountView.fxml                   # Create account view
│   │       ├── log4j2.xml                                   # Logging configuration
│   │       └── application.properties                       # Application configuration
│   └── test/                                                # Test directory
```

## Building the Application

```bash
mvn clean package
```

## Running the Application

```bash
java -jar target/banking-client-1.0-SNAPSHOT.jar
```

## Configuration

Edit `src/main/resources/application.properties` to configure the connection to the application layer:

- `connection.type`: Set to `REST` or `RMI` to choose the communication method
- `rest.api.url`: The URL of the REST API endpoint
- `rmi.host`: The hostname of the RMI registry
- `rmi.port`: The port of the RMI registry
