#!/bin/bash
# Complete ECA System Deployment with TTS and 3D Avatar Rendering

echo "🤖 Deploying Complete Greta ECA System"
echo "====================================="
echo "Features: TTS (AzureTTS) + 3D Avatar (WebGL) + AI Brain"
echo ""

# Make scripts executable
chmod +x setup-working-tts.sh

# Step 1: Setup TTS
echo "🗣️  Step 1: Setting up TTS (Text-to-Speech)"
echo "-------------------------------------------"

# Check for Azure credentials
if [ ! -f "azure_speech_key.txt" ]; then
    echo "Setting up TTS credentials..."
    read -p "Do you have Azure Speech Services API key? (y/n): " has_azure
    
    if [ "$has_azure" = "y" ]; then
        read -p "Enter your Azure Speech API key: " AZURE_KEY
        read -p "Enter your Azure region (e.g., eastus): " AZURE_REGION
        echo "$AZURE_KEY" > azure_speech_key.txt
        echo "$AZURE_REGION" > azure_speech_region.txt
        echo "✅ Azure TTS configured"
    else
        echo "demo_key" > azure_speech_key.txt
        echo "eastus" > azure_speech_region.txt
        echo "⚠️  Demo mode enabled (limited functionality)"
        echo "   Get free Azure key: https://azure.microsoft.com/en-us/services/cognitive-services/speech-services/"
    fi
else
    echo "✅ TTS credentials already configured"
fi

# Step 2: Build with all components
echo ""
echo "🔨 Step 2: Building ECA System with All Components"
echo "------------------------------------------------"

# Create complete docker-compose configuration
cat > docker-compose-eca.yml << 'EOF'
version: '3.8'

services:
  greta-eca:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        MAVEN_OPTS: "-Dmaven.repo.local=/root/.m2/repository -Xmx2048m"
    
    container_name: greta-eca-full
    restart: unless-stopped
    
    environment:
      - GRETA_ENV=production
      - DISPLAY=host.docker.internal:0
      
      # TTS Configuration
      - AZURE_SPEECH_KEY_FILE=/app/credentials/azure_speech_key.txt
      - AZURE_SPEECH_REGION_FILE=/app/credentials/azure_speech_region.txt
      - TTS_ENGINE=AzureTTS
      
      # Avatar Configuration
      - AVATAR_ENGINE=WebAvatar
      - AVATAR_PORT=8081
      
      # JVM Options
      - JAVA_OPTS=-Xmx3g -Xms1g -XX:+UseG1GC -Dfile.encoding=UTF-8
      
    volumes:
      # GUI support (if using X11)
      - /tmp/.X11-unix:/tmp/.X11-unix:rw
      
      # TTS credentials
      - ./azure_speech_key.txt:/app/credentials/azure_speech_key.txt:ro
      - ./azure_speech_region.txt:/app/credentials/azure_speech_region.txt:ro
      
      # Data persistence
      - greta-eca-data:/app/data
      - greta-eca-logs:/app/logs
      - greta-eca-cache:/app/cache
      
      # Configuration
      - ./configurations:/app/configurations:ro
    
    ports:
      - "8080:8080"      # Main application / Web avatar
      - "8081:8081"      # Avatar WebSocket
      - "8082:8082"      # AI Services
      - "61616:61616"    # ActiveMQ
    
    networks:
      - greta-eca
    
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health", "||", "exit", "1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 90s

volumes:
  greta-eca-data:
  greta-eca-logs:
  greta-eca-cache:

networks:
  greta-eca:
    driver: bridge
EOF

echo "✅ ECA Docker configuration created"

# Step 3: Create ECA configuration
echo ""
echo "⚙️  Step 3: Creating ECA Configuration"
echo "------------------------------------"

mkdir -p configurations

cat > configurations/eca-complete.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<modular>
    <elements>
        <!-- Core Character Management -->
        <element id="charactermanager" class="CharacterManager"/>
        <element id="inimanager" class="IniManager"/>
        
        <!-- Input Processing -->
        <element id="textinput" class="TextInputFrame"/>
        
        <!-- AI Brain (can be replaced with ChatGPT, Mistral, etc.) -->
        <element id="behaviorplanner" class="BehaviorPlanner"/>
        <element id="behaviorrealizer" class="BehaviorRealizer"/>
        
        <!-- Text-to-Speech -->
        <element id="azuretts" class="AzureTTS"/>
        
        <!-- 3D Avatar Rendering -->
        <element id="webavatar" class="WebAvatarPlayer"/>
        
        <!-- Animation Filters -->
        <element id="faceblender" class="FaceBlender"/>
        <element id="lipblender" class="LipBlender"/>
        <element id="bodyblender" class="BodyBlender"/>
        
        <!-- Feedback System -->
        <element id="feedbacks" class="Feedbacks"/>
        <element id="logs" class="Logs"/>
    </elements>
    
    <connections>
        <!-- Main pipeline: Text → AI → TTS → Avatar -->
        <connection from="textinput" to="behaviorplanner"/>
        <connection from="behaviorplanner" to="behaviorrealizer"/>
        <connection from="behaviorrealizer" to="azuretts"/>
        <connection from="behaviorrealizer" to="faceblender"/>
        <connection from="behaviorrealizer" to="bodyblender"/>
        
        <!-- TTS to Animation -->
        <connection from="azuretts" to="lipblender"/>
        
        <!-- Animation to Avatar -->
        <connection from="faceblender" to="webavatar"/>
        <connection from="lipblender" to="webavatar"/>
        <connection from="bodyblender" to="webavatar"/>
        
        <!-- Feedback -->
        <connection from="webavatar" to="feedbacks"/>
        <connection from="feedbacks" to="logs"/>
    </connections>
</modular>
EOF

echo "✅ Complete ECA configuration created"

# Step 4: Build the system
echo ""
echo "🏗️  Step 4: Building Complete ECA System"
echo "--------------------------------------"

echo "Building Docker image with TTS and Avatar support..."
docker-compose -f docker-compose-eca.yml build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed. Check the error messages above."
    exit 1
fi

# Step 5: Create startup scripts
echo ""
echo "🚀 Step 5: Creating Startup Scripts"
echo "----------------------------------"

# GUI startup script
cat > start-eca-gui.sh << 'EOF'
#!/bin/bash
# Start Greta ECA with GUI support

echo "🤖 Starting Greta ECA System (GUI Mode)"

# Check XQuartz on macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    if ! pgrep -x "XQuartz" > /dev/null; then
        echo "Starting XQuartz..."
        open -a XQuartz
        sleep 3
    fi
    xhost +localhost 2>/dev/null || true
fi

# Start the ECA system
docker-compose -f docker-compose-eca.yml up

# Cleanup
if [[ "$OSTYPE" == "darwin"* ]]; then
    xhost -localhost 2>/dev/null || true
fi
EOF

# Headless startup script
cat > start-eca-web.sh << 'EOF'
#!/bin/bash
# Start Greta ECA in web-only mode (no X11 required)

echo "🌐 Starting Greta ECA System (Web Mode)"
echo "Avatar will be available at: http://localhost:8080/avatar.html"

# Start without X11
docker-compose -f docker-compose-eca.yml up -d

echo ""
echo "🎭 ECA System Status:"
echo "• Main Interface: http://localhost:8080"
echo "• 3D Avatar: http://localhost:8080/avatar.html"
echo "• WebSocket: ws://localhost:8081"
echo ""
echo "To stop: docker-compose -f docker-compose-eca.yml down"
EOF

chmod +x start-eca-gui.sh start-eca-web.sh

echo "✅ Startup scripts created"

# Step 6: Final instructions
echo ""
echo "🎉 Complete ECA System Ready!"
echo "============================"
echo ""
echo "📋 Available Features:"
echo "• ✅ Text-to-Speech (AzureTTS)"
echo "• ✅ 3D Avatar Rendering (WebGL)"
echo "• ✅ Facial Expressions & Lip Sync"
echo "• ✅ Gesture Animation"
echo "• ✅ Real-time Communication"
echo "• ✅ Web-based Interface"
echo ""
echo "🚀 How to Start:"
echo ""
echo "Option 1 - GUI Mode (with X11):"
echo "  ./start-eca-gui.sh"
echo ""
echo "Option 2 - Web Mode (headless):"
echo "  ./start-eca-web.sh"
echo "  Then open: http://localhost:8080/avatar.html"
echo ""
echo "🎯 Quick Test:"
echo "1. Start the system"
echo "2. Open the avatar URL in your browser"
echo "3. Type text in Greta GUI"
echo "4. Watch your avatar speak and move!"
echo ""
echo "📁 Configuration Files:"
echo "• configurations/eca-complete.xml - Main ECA setup"
echo "• docker-compose-eca.yml - Docker configuration"
echo "• azure_speech_key.txt - TTS credentials"
echo ""

if [ "$has_azure" != "y" ]; then
    echo "⚠️  Note: Using demo TTS credentials."
    echo "   For full TTS functionality, get Azure Speech Services key:"
    echo "   https://azure.microsoft.com/en-us/services/cognitive-services/speech-services/"
    echo ""
fi

echo "🎊 Your Embodied Conversational Agent is ready to go!"