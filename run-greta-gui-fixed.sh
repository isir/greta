#!/bin/bash
# Fixed script to run Greta with GUI support, handling the LanguageMenu issue

echo "Starting Greta GUI with NullPointerException fix..."

# Check if XQuartz is running
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 3
fi

# Enable X11 access
echo "Configuring X11 access..."
xhost +localhost 2>/dev/null || true

# Stop any existing container
docker rm -f greta-gui-fixed 2>/dev/null || true

# Create and run container with locale fix
echo "Starting Greta with GUI and locale fix..."
docker run --rm \
    --name greta-gui-fixed \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    -v "$PWD/bin:/app/bin:ro" \
    -p 8080:8080 \
    greta-greta-app \
    bash -c '
# Set up environment
cd /app
export GRETA_HOME=/app
export GRETA_DATA=/app/data
export JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom"

# Ensure locale directory exists
if [ ! -d "/app/bin/Locale" ]; then
    echo "Creating missing locale directory..."
    mkdir -p /app/bin/Locale
    
    # Create minimal locale files if they do not exist
    if [ ! -f "/app/bin/Locale/en-GB.ini" ]; then
        echo "Creating default English locale..."
        cat > /app/bin/Locale/en-GB.ini << "EOF"
[GLOBAL]
language=English
country=United Kingdom
locale=en-GB
EOF
    fi
fi

echo "Locale directory contents:"
ls -la /app/bin/Locale/ 2>/dev/null || echo "No locale files found"

echo "Starting Greta GUI application..."
java -Duser.language=en \
     -Duser.country=GB \
     -Dfile.encoding=UTF-8 \
     -Djava.awt.headless=false \
     -Dapple.awt.application.name="Greta Platform" \
     -jar greta-application-modular-1.0.0-SNAPSHOT.jar 2>&1
'

# Cleanup
xhost -localhost 2>/dev/null || true
echo "Greta GUI session ended."