# 🤖 Greta Virtual Agent Setup with AI Features

## Overview
Greta is a complete Embodied Conversational Agent (ECA) platform that can display a 3D avatar, speak with natural voices, understand speech, and engage in AI-powered conversations.

## 🎯 Quick Start: See Your Virtual Agent Speaking

### Step 1: Start Greta with Basic AI Configuration
```bash
# Build the latest image with all fixes
docker-compose build

# Start Greta in GUI mode
./run-greta-gui-fixed.sh
```

### Step 2: Load an AI-Enabled Configuration
1. In the Greta GUI, go to **File** → **Load Configuration**
2. Choose one of these AI configurations:
   - `Greta - ChatGPT - CereProc.xml` (ChatGPT + Text-to-Speech)
   - `Greta - Basic configuration - CereProc.xml` (Basic avatar + TTS)
   - `Greta - Mistral incremental - DeepGram(ASR) - CereProc.xml` (Full AI pipeline)

### Step 3: Set Up Your Avatar
1. Add **Character Manager** module (from Add menu)
2. Add **Ogre Player** module (for 3D avatar display)
3. Connect Character Manager → Ogre Player
4. Configure the avatar (default avatars are available)

### Step 4: Configure Text-to-Speech
1. Add **CereProc** or **AzureTTS** module
2. Connect it to the behavior pipeline
3. For AzureTTS: Set your Azure subscription key in the module settings

### Step 5: Add AI Brain (Choose One)

#### Option A: ChatGPT Integration
1. Add **ChatGPT** module
2. Set your OpenAI API key in module settings
3. Connect: ChatGPT → Behavior Planner → TTS → Avatar

#### Option B: Advanced LLM with Speech Recognition
1. Add **Mistral LLM** module
2. Add **DeepGram ASR** module (for speech input)
3. Add **Turn Management** for natural conversation flow
4. Connect the full pipeline

## 🎭 Available AI Features

### 1. **Avatar Rendering**
- **3D Characters**: Realistic avatars with facial expressions
- **Animation Systems**: Natural gestures and lip synchronization
- **Configurable Appearance**: Multiple avatar models available

### 2. **Speech Synthesis (TTS)**
- **CereProc**: High-quality commercial voices
- **Azure TTS**: Microsoft's neural voices
- **MaryTTS**: Open-source alternative
- **Voice Selection**: Multiple languages and voice types

### 3. **Speech Recognition (ASR)**
- **DeepGram**: Modern neural ASR with real-time processing
- **Continuous Mode**: For natural conversation flow
- **Language Support**: Multiple language models

### 4. **AI Conversation Engines**
- **ChatGPT Integration**: GPT-3.5/4 powered conversations
- **Mistral LLM**: Alternative large language model
- **MI Counselor**: Specialized therapeutic conversation system
- **Custom Personalities**: Configurable agent personalities

### 5. **Advanced AI Features**
- **Emotion Recognition**: Detect user emotions from speech/video
- **Neural Gesture Generation**: AI-generated natural gestures
- **Multimodal Understanding**: Process text, speech, and vision
- **Adaptive Personality**: Agent personality changes based on interaction

## 🔧 Detailed Configuration

### Basic Avatar + TTS Setup
```xml
<!-- Load this configuration for basic speaking avatar -->
<configuration name="Basic Speaking Avatar">
    <modules>
        <module id="character" class="CharacterManager"/>
        <module id="player" class="OgrePlayer"/>
        <module id="tts" class="CereProc"/>
        <module id="behavior" class="BehaviorRealizer"/>
    </modules>
    <connections>
        <connection from="tts" to="behavior"/>
        <connection from="behavior" to="player"/>
    </connections>
</configuration>
```

### Full AI Pipeline Setup
```xml
<!-- Complete AI conversation system -->
<configuration name="AI Conversation System">
    <modules>
        <module id="asr" class="DeepGram"/>
        <module id="llm" class="ChatGPT"/>
        <module id="tts" class="AzureTTS"/>
        <module id="avatar" class="OgrePlayer"/>
        <module id="behavior" class="BehaviorPlanner"/>
        <module id="turn" class="TurnManagement"/>
    </modules>
    <connections>
        <connection from="asr" to="llm"/>
        <connection from="llm" to="behavior"/>
        <connection from="behavior" to="tts"/>
        <connection from="tts" to="avatar"/>
        <connection from="turn" to="asr"/>
    </connections>
</configuration>
```

## 🔑 API Keys and Configuration

### Required API Keys
1. **OpenAI API Key** (for ChatGPT):
   ```
   Set in ChatGPT module settings
   Environment variable: OPENAI_API_KEY
   ```

2. **Azure Speech Services** (for Azure TTS/ASR):
   ```
   Subscription Key: Set in AzureTTS module
   Region: Set service region
   ```

3. **DeepGram API Key** (for ASR):
   ```
   Set in DeepGram module settings
   Environment variable: DEEPGRAM_API_KEY
   ```

### Module Configuration Files
- TTS settings: `/app/data/TTS/`
- ASR models: `/app/data/ASR/`
- Avatar models: `/app/data/Characters/`
- AI personalities: `/app/data/Personalities/`

## 🎪 Demo Scenarios

### Scenario 1: Basic Talking Avatar
1. Load "Basic configuration - CereProc.xml"
2. Type text in the Text Input module
3. Watch avatar speak with lip sync and gestures

### Scenario 2: Voice Conversation
1. Load "Mistral incremental - DeepGram - CereProc.xml"
2. Speak to your microphone
3. Avatar processes speech → generates response → speaks back

### Scenario 3: ChatGPT Powered Agent
1. Load "ChatGPT - CereProc.xml"
2. Type questions or have voice conversations
3. Avatar gives ChatGPT-powered responses with natural gestures

### Scenario 4: Therapeutic Counselor
1. Load "MI Counselor Incremental.xml"
2. Engage in counseling-style conversation
3. Avatar provides empathetic responses using specialized AI

## 🚀 Advanced Features

### Real-time Emotion Recognition
```bash
# Add emotion recognition to your pipeline
1. Add "Emotion Recognition" module
2. Connect webcam input
3. Avatar responds to detected emotions
```

### Neural Gesture Generation
```bash
# Enable AI-generated gestures
1. Add "Neural Gesture Synthesis" module
2. Connect to behavior planner
3. Avatar generates contextual gestures automatically
```

### Multimodal AI Integration
```bash
# Process text, speech, and vision together
1. Add "Multimodal AI Integration" module
2. Connect multiple input sources
3. Avatar understands context from all modalities
```

## 🐛 Troubleshooting

### Avatar Not Visible
- Ensure X11 forwarding is working
- Check that Ogre Player module is loaded
- Verify 3D acceleration is available

### No Speech Output
- Check TTS module configuration
- Verify API keys are set correctly
- Ensure audio output is configured

### AI Not Responding
- Verify internet connection for cloud APIs
- Check API key validity
- Review module connection pipeline

### Performance Issues
- Reduce avatar rendering quality
- Use local TTS instead of cloud
- Limit concurrent AI processing

## 📚 Additional Resources

- **Character Models**: Located in `/app/data/Characters/`
- **Voice Samples**: Available in TTS module settings
- **Configuration Examples**: `/app/bin/Common/Data/Configurations/`
- **Module Documentation**: Each module has built-in help

## 🎉 Quick Demo Command

For immediate results, try this:
```bash
# Start with pre-configured ChatGPT demo
docker run -it --rm \
    -e DISPLAY=host.docker.internal:0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
    -e OPENAI_API_KEY=your_key_here \
    greta-greta-app \
    java -jar greta-application-modular-1.0.0-SNAPSHOT.jar \
    --config "Greta - ChatGPT - CereProc.xml"
```

This will start Greta with a speaking avatar powered by ChatGPT!