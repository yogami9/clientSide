FROM maven:3.8-openjdk-11 as build

# Set working directory
WORKDIR /app

# Copy POM file
COPY pom.xml .

# Download dependencies (this layer can be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Runtime image with JavaFX support
FROM openjdk:11-jre-slim

# Install X11 and JavaFX dependencies
# Install X11 and JavaFX dependencies
# Add these packages to your Dockerfile
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libx11-6 \
    libxxf86vm1 \
    libxtst6 \
    libxt6 \
    libxrender1 \
    xvfb \
    procps \
    dbus-x11 \
    libgtk-3-0 \
    libgtk2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/banking-client-1.0-SNAPSHOT.jar ./app.jar

# Copy startup script
COPY docker/start.sh ./start.sh
RUN chmod +x ./start.sh

# Environment variables for configuration
ENV REST_API_URL=http://localhost:8080
ENV CONNECTION_TYPE=REST
ENV RMI_HOST=localhost
ENV RMI_PORT=1099
ENV DISPLAY=:99

# Expose port for VNC (if using VNC approach)
EXPOSE 5901

# Run the application
ENTRYPOINT ["./start.sh"]
