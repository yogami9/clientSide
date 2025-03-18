#!/bin/bash

# Clean up any existing X11 lock files
rm -f /tmp/.X*-lock
rm -f /tmp/.X11-unix/X*

# Set VNC environment
export DISPLAY=:1

# Determine HTTP port from environment or use default
HTTP_PORT=${PORT:-10000}

# Start VNC server with better error handling
vncserver :1 -geometry 1280x720 -depth 24 -SecurityTypes None || {
  echo "Failed to start VNC server"
  exit 1
}

# Give VNC server a moment to initialize
sleep 3

# Start the window manager in background
fluxbox &
FLUXBOX_PID=$!

# Wait for fluxbox to start
sleep 2

echo "VNC server running on port 5901"
echo "Starting Java application with DISPLAY=$DISPLAY"

# Run the application - direct output to avoid buffer issues
java -jar app.jar