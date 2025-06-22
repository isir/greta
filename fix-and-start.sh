#!/bin/bash
# Fix the unstable GUI and web interface issues

echo "🔧 Fixing Greta ECA Issues..."
echo "=============================="

# Kill any existing processes
echo "1. Stopping existing processes..."
pkill -f "greta" 2>/dev/null || true
pkill -f "java.*WebServer" 2>/dev/null || true
docker stop $(docker ps -q) 2>/dev/null || true

# Fix X11 for stable GUI
echo "2. Fixing X11 configuration..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS XQuartz setup
    if ! pgrep -x "XQuartz" > /dev/null; then
        echo "Starting XQuartz..."
        open -a XQuartz
        sleep 5
    fi
    
    # Configure XQuartz for stability
    echo "Configuring XQuartz for stable GUI..."
    xhost +localhost 2>/dev/null || true
    
    # Set more stable display settings
    export DISPLAY=:0
    
    # Configure Java for better X11 compatibility
    export _JAVA_AWT_WM_NONREPARENTING=1
    export AWT_TOOLKIT=XToolkit
fi

# Compile and start web server
echo "3. Starting web server..."
javac -cp "lib/*" WebServer.java 2>/dev/null || javac WebServer.java

# Start web server in background
echo "Starting Greta web interface on http://localhost:8080..."
java WebServer &
WEB_PID=$!
sleep 2

# Check if web server started
if curl -s http://localhost:8080/health > /dev/null; then
    echo "✅ Web server started successfully"
else
    echo "❌ Web server failed to start"
fi

# Start Greta with more stable settings
echo "4. Starting Greta with stable GUI settings..."

# Build if needed
if [ ! -f "application/Modular/target/greta-application-modular-1.0.0-SNAPSHOT.jar" ]; then
    echo "Building Greta..."
    ./mvnw clean package -DskipTests -q
fi

# Start Greta with stable X11 settings
echo "5. Launching Greta ECA..."
java -Dfile.encoding=UTF-8 \
     -Djava.awt.headless=false \
     -Djava.security.egd=file:/dev/./urandom \
     -D_JAVA_AWT_WM_NONREPARENTING=1 \
     -Dapple.awt.application.name="Greta ECA" \
     -Xmx2g \
     -cp "application/Modular/target/*:core/*/target/*:auxiliary/*/target/*" \
     greta.application.modular.Modular &

GRETA_PID=$!
sleep 3

echo ""
echo "🎉 Greta ECA System Status:"
echo "=========================="
echo "🌐 Web Interface: http://localhost:8080"
echo "🎭 3D Avatar: http://localhost:8080/avatar.html"
echo "🖥️  GUI Application: Should appear on screen"
echo ""

if ps -p $WEB_PID > /dev/null; then
    echo "✅ Web Server: Running (PID: $WEB_PID)"
else
    echo "❌ Web Server: Not running"
fi

if ps -p $GRETA_PID > /dev/null; then
    echo "✅ Greta GUI: Running (PID: $GRETA_PID)"
else
    echo "❌ Greta GUI: Not running"
fi

echo ""
echo "📋 How to use:"
echo "1. Open http://localhost:8080 in your browser"
echo "2. Click 'Launch 3D Avatar'"
echo "3. In Greta GUI window, add modules and connect them"
echo "4. Use Text Input module to make avatar speak"
echo ""
echo "🛑 To stop: press Ctrl+C or run: pkill -f greta"

# Wait for user to stop
wait