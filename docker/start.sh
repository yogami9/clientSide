#!/bin/bash

# Clean up any existing X lock files
rm -f /tmp/.X*-lock
rm -f /tmp/.X11-unix/X*

# Start Xvfb with better error handling
Xvfb :99 -screen 0 1280x720x24 &
XVFB_PID=$!

# Wait for Xvfb to initialize
sleep 2

# Check if Xvfb is running
if ! ps -p $XVFB_PID > /dev/null; then
    echo "Failed to start Xvfb"
    exit 1
fi

# Set DISPLAY environment variable
export DISPLAY=:99

# Ensure JavaFX modules are correctly loaded
JAVA_OPTS="${JAVA_OPTS} --module-path /opt/javafx-sdk-11/lib --add-modules javafx.controls,javafx.fxml"

# Run the application
echo "Starting Java application with DISPLAY=$DISPLAY"
java $JAVA_OPTS -jar app.jar
