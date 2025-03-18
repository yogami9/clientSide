# Banking Client

Modern client application for the three-tier banking system.

## Features

- Connect to the application layer using REST API or RMI
- View account information and transaction history
- Perform banking operations (deposit, withdraw, transfer)
- Create new accounts
- Modern JavaFX user interface
- Docker support for containerized deployment

## Project Structure

```
banking-client/
├── pom.xml                                  # Maven configuration
├── Dockerfile                               # Docker configuration
├── docker-compose.yml                       # Docker Compose configuration
├── docker/                                  # Docker support files
│   ├── start.sh                             # Startup script for Docker
│   ├── Dockerfile.vnc                       # Alternative VNC-based Dockerfile
│   └── vnc-startup.sh                       # VNC startup script
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

### Using Maven

```bash
mvn clean package
```

### Using Docker

```bash
docker build -t banking-client .
```

## Running the Application

### Using Java

```bash
java -jar target/banking-client-1.0-SNAPSHOT.jar
```

### Using Docker Compose

To run the entire three-tier system including the client:

```bash
docker-compose up
```

To run only the client:

```bash
docker-compose up client-x11
```

### Connecting with VNC

If using the VNC approach, connect to the VNC server at `localhost:5901` using a VNC client.

## Configuration

Edit `src/main/resources/application.properties` or set environment variables to configure the connection to the application layer:

- `connection.type`: Set to `REST` or `RMI` to choose the communication method
- `rest.api.url`: The URL of the REST API endpoint
- `rmi.host`: The hostname of the RMI registry
- `rmi.port`: The port of the RMI registry

When running with Docker, use environment variables:

```
REST_API_URL=http://app-tier:8080
CONNECTION_TYPE=REST
RMI_HOST=app-tier
RMI_PORT=1099
```

## X11 Display Forwarding (Linux)

If you want to run the Docker container with direct X11 forwarding on Linux:

```bash
xhost +local:docker
docker run -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix banking-client
```

## VNC Approach (Cross-platform)

Build and run the VNC version:

```bash
docker build -f docker/Dockerfile.vnc -t banking-client-vnc .
docker run -p 5901:5901 banking-client-vnc
```

Then connect to `localhost:5901` with a VNC client.
