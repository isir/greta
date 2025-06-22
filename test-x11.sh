#!/bin/bash
# Test X11 connection for debugging

echo "Testing X11 connection..."

# Start XQuartz if not running
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 5
fi

# Get the current DISPLAY
echo "Current DISPLAY: $DISPLAY"

# Enable local connections
xhost + 2>/dev/null || true

# Test with a simple X11 app in Docker
echo "Testing with xeyes..."
docker run --rm \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    --name x11-test \
    greta-greta-app \
    bash -c 'apt-get update >/dev/null 2>&1 && apt-get install -y x11-apps >/dev/null 2>&1 && echo "Testing X11..." && timeout 5 xeyes || echo "X11 test failed"'

echo "X11 test completed"