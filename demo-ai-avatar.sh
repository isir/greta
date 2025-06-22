#!/bin/bash
# Demo script to run Greta with AI-powered speaking avatar

echo "🤖 Starting Greta AI Virtual Agent Demo"
echo "========================================"

# Check if XQuartz is running
if ! pgrep -x "XQuartz" > /dev/null; then
    echo "📱 Starting XQuartz for GUI..."
    open -a XQuartz
    sleep 5
fi

# Enable X11 access
echo "🔧 Configuring X11 access..."
xhost +localhost 2>/dev/null || true

# Stop any existing container
docker rm -f greta-ai-demo 2>/dev/null || true

echo ""
echo "🎭 Available AI Demo Configurations:"
echo "1. Basic Avatar + Text-to-Speech (CereProc)"
echo "2. ChatGPT Powered Avatar (requires OpenAI API key)"
echo "3. Advanced LLM with Speech Recognition"
echo "4. MI Counselor Demo (Therapeutic conversation)"
echo ""

# Default to basic demo
CONFIG_FILE="Greta - Basic configuration - CereProc.xml"
DEMO_TYPE="Basic Avatar Demo"

read -p "Enter your choice (1-4) or press Enter for basic demo: " choice

case $choice in
    1)
        CONFIG_FILE="Greta - Basic configuration - CereProc.xml"
        DEMO_TYPE="Basic Avatar + TTS Demo"
        ;;
    2)
        CONFIG_FILE="Greta - ChatGPT - CereProc.xml"
        DEMO_TYPE="ChatGPT Powered Avatar"
        read -p "Enter your OpenAI API key (or press Enter to skip): " OPENAI_KEY
        ;;
    3)
        CONFIG_FILE="Greta - Mistral incremental - DeepGramContineous(ASR) - CereProc - TurnManagementContineous.xml"
        DEMO_TYPE="Advanced LLM with Speech Recognition"
        ;;
    4)
        CONFIG_FILE="Greta - MI Counselor demo RL.xml"
        DEMO_TYPE="MI Counselor (Therapeutic AI)"
        ;;
    *)
        echo "Using default basic configuration..."
        ;;
esac

echo ""
echo "🚀 Starting: $DEMO_TYPE"
echo "Configuration: $CONFIG_FILE"
echo ""

# Set up environment variables
ENV_VARS="-e DISPLAY=host.docker.internal:0"
if [ ! -z "$OPENAI_KEY" ]; then
    ENV_VARS="$ENV_VARS -e OPENAI_API_KEY=$OPENAI_KEY"
fi

# Run the demo
echo "🎬 Launching Greta Virtual Agent..."
echo "💡 Instructions will appear in the Greta GUI window"
echo ""

docker run --rm -it \
    --name greta-ai-demo \
    $ENV_VARS \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    -v "$PWD/bin:/app/bin:ro" \
    -p 8080:8080 \
    greta-greta-app \
    bash -c "
# Set up environment
cd /app
export GRETA_HOME=/app
export GRETA_DATA=/app/data

# Copy essential files
cp /app/bin/Modular.xml /app/ 2>/dev/null || true
cp /app/bin/Modular.xsd /app/ 2>/dev/null || true

echo '🎭 Starting Greta AI Virtual Agent...'
echo 'Configuration: $CONFIG_FILE'
echo ''
echo '📋 How to use:'
echo '1. Wait for the Greta GUI to appear'
echo '2. The avatar will be loaded automatically'
echo '3. Try typing text or speaking (depending on configuration)'
echo '4. Watch the virtual agent respond with speech and gestures!'
echo ''
echo '🎯 Available features in this demo:'
case '$choice' in
    1) echo '  • 3D Avatar with facial expressions'
       echo '  • Text-to-Speech with lip synchronization'
       echo '  • Natural gesture generation'
       echo '  • Type text in the Text Input module to make avatar speak'
       ;;
    2) echo '  • ChatGPT-powered conversations'
       echo '  • Intelligent responses with context awareness'
       echo '  • Natural language understanding'
       echo '  • Type questions and get AI-powered responses'
       ;;
    3) echo '  • Full speech-to-speech pipeline'
       echo '  • Continuous speech recognition'
       echo '  • LLM processing and response generation'
       echo '  • Natural conversation with turn-taking'
       ;;
    4) echo '  • Therapeutic conversation AI'
       echo '  • Empathetic responses'
       echo '  • Counseling dialogue patterns'
       echo '  • Specialized for mental health support'
       ;;
esac
echo ''
echo '🔧 Troubleshooting:'
echo '  • If no GUI appears, check XQuartz is running'
echo '  • If no speech, check your audio settings'
echo '  • For voice input, allow microphone access'
echo ''

# Start Greta with the selected configuration
java -Duser.language=en \\
     -Duser.country=GB \\
     -Dfile.encoding=UTF-8 \\
     -Djava.awt.headless=false \\
     -Xmx2g \\
     -jar greta-application-modular-1.0.0-SNAPSHOT.jar
"

# Cleanup
xhost -localhost 2>/dev/null || true
echo ""
echo "🎬 Demo session ended. Thanks for trying Greta AI!"