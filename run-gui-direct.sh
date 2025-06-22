#!/bin/bash
# Direct GUI test script

echo "Testing Greta GUI with X11 forwarding..."

# Make sure XQuartz is running and configured
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 3
fi

# Enable X11 forwarding
echo "Enabling X11 access..."
xhost +localhost

# Stop any existing container
docker stop greta-dev 2>/dev/null || true

# Test with a simple X11 app first
echo "Testing X11 connection with xeyes..."
docker run --rm \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    --name test-x11 \
    greta-greta-app \
    bash -c 'apt-get update && apt-get install -y x11-apps && xeyes' &

XEYES_PID=$!
sleep 5
kill $XEYES_PID 2>/dev/null || true

echo "Now testing Greta GUI..."
# Run Greta with GUI
docker run -it --rm \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    --name greta-gui-test \
    greta-greta-app \
    bash -c 'cd /app && java -Djava.awt.headless=false -jar greta-application-modular-1.0.0-SNAPSHOT.jar'

# Cleanup
xhost -localhost 2>/dev/null || true