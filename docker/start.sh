#!/bin/bash

# Start Xvfb
Xvfb :99 -screen 0 1280x720x24 &
sleep 1

# Run the application
java -jar app.jar
