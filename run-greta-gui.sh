#!/bin/bash
# Script to run Greta with GUI support on macOS with Docker

echo "Starting Greta GUI with X11 forwarding..."

# Check if XQuartz is running
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 2
fi

# Allow X11 connections
echo "Configuring X11 access..."
xhost +localhost 2>/dev/null || true

# Set display
export DISPLAY=:0

# Stop any existing container
docker-compose down 2>/dev/null || true

# Build and run with GUI support
echo "Building GUI-enabled container..."
docker build -f Dockerfile.gui -t greta-gui .

echo "Starting Greta GUI..."
docker run -it --rm \
    --name greta-gui \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    -v "$PWD/data:/app/data" \
    --network host \
    greta-gui

# Cleanup
xhost -localhost 2>/dev/null || true