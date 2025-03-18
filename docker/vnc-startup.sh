#!/bin/bash

# Start VNC server
vncserver :1 -geometry 1280x720 -depth 24 -SecurityTypes None

# Start the window manager
fluxbox &

# Wait for VNC server to start properly
sleep 2

# Run the application
java -jar app.jar
