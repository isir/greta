#!/bin/bash
# Setup working TTS (AzureTTS) for Greta ECA

echo "🗣️  Setting up working TTS for Greta ECA System"
echo "=============================================="

# Create Azure TTS configuration
echo "📝 Setting up AzureTTS configuration..."

# Check if user has Azure credentials
read -p "Do you have an Azure Speech Services API key? (y/n): " has_azure
if [ "$has_azure" = "y" ]; then
    read -p "Enter your Azure Speech API key: " AZURE_KEY
    read -p "Enter your Azure region (e.g., eastus): " AZURE_REGION
    
    # Create credential files for Docker
    echo "$AZURE_KEY" > azure_speech_key.txt
    echo "$AZURE_REGION" > azure_speech_region.txt
    
    echo "✅ Azure credentials configured"
else
    echo "🆓 Setting up free TTS alternatives..."
    
    # Create demo credentials file for testing
    echo "demo_key" > azure_speech_key.txt
    echo "eastus" > azure_speech_region.txt
    
    echo "⚠️  Demo mode: For production, get Azure Speech API key at:"
    echo "   https://azure.microsoft.com/en-us/services/cognitive-services/speech-services/"
fi

# Update Docker compose to include TTS
cat > docker-compose-tts.yml << 'EOF'
version: '3.8'

services:
  greta-eca:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: greta-eca-full
    restart: unless-stopped
    
    environment:
      - GRETA_ENV=production
      - DISPLAY=host.docker.internal:0
      # TTS Configuration
      - AZURE_SPEECH_KEY_FILE=/app/credentials/azure_speech_key.txt
      - AZURE_SPEECH_REGION_FILE=/app/credentials/azure_speech_region.txt
      - TTS_ENGINE=AzureTTS
      
    volumes:
      # GUI support
      - /tmp/.X11-unix:/tmp/.X11-unix:rw
      # TTS credentials
      - ./azure_speech_key.txt:/app/credentials/azure_speech_key.txt:ro
      - ./azure_speech_region.txt:/app/credentials/azure_speech_region.txt:ro
      # Data persistence
      - greta-data:/app/data
      - greta-logs:/app/logs
    
    ports:
      - "8080:8080"      # Web interface
      - "8081:8081"      # Avatar rendering
      - "61616:61616"    # ActiveMQ
    
    networks:
      - greta-net

volumes:
  greta-data:
  greta-logs:

networks:
  greta-net:
    driver: bridge
EOF

echo "✅ Docker Compose configuration created: docker-compose-tts.yml"

# Create TTS test configuration
cat > test-tts-config.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<modular>
    <elements>
        <!-- Core Components -->
        <element id="charactermanager" class="CharacterManager"/>
        <element id="behaviorplanner" class="BehaviorPlanner"/>
        <element id="behaviorrealizer" class="BehaviorRealizer"/>
        
        <!-- TTS Engine -->
        <element id="azuretts" class="AzureTTS"/>
        
        <!-- Avatar Display -->
        <element id="webavatar" class="WebAvatarPlayer"/>
        
        <!-- Test Input -->
        <element id="textinput" class="TextInputFrame"/>
    </elements>
    
    <connections>
        <!-- Text Input → Behavior Planning → TTS → Avatar -->
        <connection from="textinput" to="behaviorplanner"/>
        <connection from="behaviorplanner" to="behaviorrealizer"/>
        <connection from="behaviorrealizer" to="azuretts"/>
        <connection from="azuretts" to="webavatar"/>
    </connections>
</modular>
EOF

echo "✅ TTS test configuration created: test-tts-config.xml"

echo ""
echo "🚀 Ready to test TTS!"
echo "Next steps:"
echo "1. Build: docker-compose -f docker-compose-tts.yml build"
echo "2. Run: docker-compose -f docker-compose-tts.yml up"
echo "3. Open Greta and load test-tts-config.xml"
echo "4. Type text to hear the avatar speak!"