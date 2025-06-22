#!/bin/bash
# Fix GUI startup issues

echo "Fixing Greta GUI startup..."

# Ensure XQuartz is running
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "Starting XQuartz..."
    open -a XQuartz
    sleep 3
fi

# Enable X11 access
xhost +localhost

# Stop current container
docker-compose down

# Create a custom startup script that handles the LanguageMenu issue
docker run --rm -v "$PWD:/host" greta-greta-app bash -c "
cat > /tmp/greta-gui-fixed.sh << 'EOF'
#!/bin/bash
cd /app

# Set GUI-friendly environment
export GRETA_HOME=/app
export GRETA_DATA=/app/data
export JAVA_TOOL_OPTIONS=\"-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=false\"

# Try to start GUI with fallback for LanguageMenu issues
echo 'Starting Greta GUI with error handling...'
java -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8 \\
     -Djava.awt.headless=false \\
     -Dapple.awt.application.name=\"Greta Platform\" \\
     -jar greta-application-modular-1.0.0-SNAPSHOT.jar 2>&1 | tee /tmp/greta.log

# If GUI fails, show the error
if [ \${PIPESTATUS[0]} -ne 0 ]; then
    echo \"GUI startup failed. Error log:\"
    tail -20 /tmp/greta.log
    echo \"Keeping container alive for debugging...\"
    tail -f /dev/null
fi
EOF
chmod +x /tmp/greta-gui-fixed.sh
cp /tmp/greta-gui-fixed.sh /host/
"

# Make it executable
chmod +x greta-gui-fixed.sh

# Run with GUI
echo "Starting Greta with GUI..."
docker run -it --rm \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    -v "$PWD:/host" \
    -p 8080:8080 \
    --name greta-gui \
    greta-greta-app \
    /host/greta-gui-fixed.sh

# Cleanup
xhost -localhost 2>/dev/null || true