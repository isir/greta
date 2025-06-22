#!/bin/bash
# Quick test for GUI fix

echo "Testing Greta GUI fix..."

# Check if XQuartz is running
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 3
fi

# Enable X11 access
xhost +localhost

# Create a temporary container with the locale fix
echo "Creating container with locale data..."
docker run -d --name greta-locale-test greta-greta-app tail -f /dev/null

# Copy locale data to the container
echo "Copying locale data..."
docker exec greta-locale-test mkdir -p /app/bin
docker cp ./bin/Locale greta-locale-test:/app/bin/

# Test GUI startup with timeout
echo "Testing GUI startup..."
docker exec greta-locale-test bash -c "
cd /app
export DISPLAY=host.docker.internal:0
timeout 15 java -Djava.awt.headless=false -Duser.language=en -Duser.country=US -jar greta-application-modular-1.0.0-SNAPSHOT.jar 2>&1 | head -30
" || echo "GUI test completed (timeout or error expected)"

# Cleanup
docker rm -f greta-locale-test
xhost -localhost

echo "Test completed"