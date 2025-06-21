# Greta AI Python Control Guide

## 🐍 Complete Python Interface for Greta AI

This guide shows you how to control all the advanced AI capabilities of the modernized Greta platform using Python.

## 📋 Quick Navigation

1. [Setup and Installation](#setup-and-installation)
2. [Basic Usage](#basic-usage)
3. [AI Capabilities](#ai-capabilities)
4. [Complete Examples](#complete-examples)
5. [Advanced Integration](#advanced-integration)

## 🚀 Setup and Installation

### Prerequisites

```bash
# Install required Python packages
pip install requests websockets opencv-python numpy asyncio aiohttp

# Ensure Greta platform is running with AI enhancements
docker-compose up -d  # or your preferred startup method
```

### Basic Configuration

```python
from greta_ai_controller import GretaAIController, GretaConfig

# Configure connection to Greta platform
config = GretaConfig(
    host="localhost",
    port=8080,
    username="demo_user",
    password="demo_pass",
    api_key="your-openai-api-key"  # For LLM providers
)

controller = GretaAIController(config)
```

## 🎯 Basic Usage

### Initialize and Connect

```python
import asyncio

async def main():
    # Initialize connection
    if await controller.initialize():
        print("✅ Connected to Greta AI platform")
        
        # Your AI interactions here
        
        # Cleanup
        await controller.shutdown()
    else:
        print("❌ Failed to connect")

asyncio.run(main())
```

### Simple Chat Example

```python
# Basic conversation with AI
response = await controller.chat_with_llm(
    message="Hello! How are you today?",
    personality="assistant"
)

print(f"Greta: {response['text']}")
print(f"Confidence: {response['confidence']:.2f}")
```

## 🧠 AI Capabilities Reference

### 1. LLM Integration (GPT-4, Claude)

```python
# Chat with different personalities
personalities = ["teacher", "therapist", "researcher", "assistant"]

for personality in personalities:
    response = await controller.chat_with_llm(
        "Tell me about quantum computing",
        personality=personality
    )
    print(f"{personality}: {response['text']}")

# Custom personality traits
response = await controller.generate_personality_response(
    message="I'm feeling stressed about my exams",
    personality_traits={
        "empathy": 0.9,
        "supportiveness": 0.8,
        "optimism": 0.7
    }
)
```

### 2. Speech-to-Speech Synthesis

```python
# Load audio file
with open("input_speech.wav", "rb") as f:
    audio_data = f.read()

# Convert speech with emotion
result = await controller.convert_speech_with_emotion(
    audio_data=audio_data,
    target_emotion="enthusiastic",
    target_voice="teacher_voice"
)

# Save converted audio
with open("output_speech.wav", "wb") as f:
    f.write(result['audio_data'])

# Clone a voice from samples
reference_files = []
for i in range(3):  # Use 3 reference files
    with open(f"reference_{i}.wav", "rb") as f:
        reference_files.append(f.read())

voice_profile = await controller.clone_voice(
    reference_audio_files=reference_files,
    speaker_name="CustomVoice"
)
```

### 3. Real-time Emotion Recognition

```python
import cv2

# Analyze emotion from webcam
cap = cv2.VideoCapture(0)

while True:
    ret, frame = cap.read()
    if not ret:
        break
    
    # Analyze emotion
    emotion = await controller.analyze_emotion_multimodal(
        video_frame=frame,
        text="I'm feeling great today!"  # Optional text context
    )
    
    print(f"Emotion: {emotion['primary_emotion']} "
          f"(arousal: {emotion['arousal']:.2f}, "
          f"valence: {emotion['valence']:.2f})")
    
    # Display with emotion overlay
    cv2.putText(frame, f"Emotion: {emotion['primary_emotion']}", 
               (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
    cv2.imshow('Emotion Detection', frame)
    
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
```

### 4. Multimodal Scene Understanding

```python
# Analyze an image with natural language query
image = cv2.imread("classroom_scene.jpg")

scene_analysis = await controller.analyze_scene_multimodal(
    video_frame=image,
    query="How many students are in this classroom and what are they doing?"
)

print(f"Scene: {scene_analysis['scene_description']}")
print(f"Objects: {scene_analysis['detected_objects']}")
print(f"People: {scene_analysis['detected_people']}")

# Understand gestures in context
gesture_understanding = await controller.understand_gesture_in_context(
    video_frame=image,
    speech_text="Please raise your hand if you have a question"
)

print(f"Gesture: {gesture_understanding['gesture_type']}")
print(f"Meaning: {gesture_understanding['contextual_meaning']}")
```

### 5. Neural Gesture Generation

```python
# Generate gestures from text
gesture = await controller.generate_gesture_from_text(
    text="Welcome everyone to today's presentation!",
    emotion="enthusiastic",
    style="formal"
)

print(f"Generated gesture: {gesture['gesture_type']}")
print(f"Duration: {gesture['duration_ms']}ms")
print(f"Quality: {gesture['quality_score']:.2f}")

# Generate co-speech gestures
with open("presentation_audio.wav", "rb") as f:
    speech_audio = f.read()

co_speech = await controller.generate_co_speech_gestures(
    speech_audio=speech_audio,
    speech_text="In this graph, you can see the upward trend"
)

print(f"Synchronized gestures: {len(co_speech['synchronized_gestures'])}")
```

## 📚 Complete Examples

### Educational Virtual Tutor

```python
async def create_virtual_tutor():
    """Create an AI-powered educational assistant"""
    
    # Configure tutor personality
    tutor_config = {
        "name": "MathTutor",
        "personality": {
            "patience": 0.9,
            "enthusiasm": 0.8,
            "clarity": 0.9,
            "empathy": 0.8
        },
        "voice": "friendly_teacher",
        "gestures": "educational",
        "emotion_sensitivity": 0.8
    }
    
    # Create the persona
    persona = await controller.create_ai_persona(tutor_config)
    
    # Start tutoring session
    print("🎓 Virtual Math Tutor ready!")
    
    while True:
        student_question = input("Student: ")
        if student_question.lower() in ['quit', 'exit', 'bye']:
            break
        
        # Analyze student's emotional state (if video available)
        # emotion = await controller.analyze_emotion_multimodal(video_frame=frame)
        
        # Generate appropriate response
        response = await controller.chat_with_llm(
            message=student_question,
            personality="teacher"
        )
        
        # Generate accompanying gesture
        gesture = await controller.generate_gesture_from_text(
            text=response['text'],
            emotion="encouraging",
            style="educational"
        )
        
        print(f"📚 Tutor: {response['text']}")
        print(f"🤲 *{gesture['gesture_type']} gesture*")
```

### Therapeutic Conversation Agent

```python
async def therapeutic_session():
    """Create an empathetic therapeutic agent"""
    
    # Configure therapeutic personality
    therapist_traits = {
        "empathy": 0.95,
        "patience": 0.9,
        "supportiveness": 0.9,
        "non_judgmental": 0.95
    }
    
    print("🌟 Therapeutic AI session starting...")
    print("How are you feeling today?")
    
    while True:
        user_input = input("You: ")
        if user_input.lower() in ['end session', 'goodbye']:
            break
        
        # Generate empathetic response
        response = await controller.generate_personality_response(
            message=user_input,
            personality_traits=therapist_traits
        )
        
        # Use calm, supportive speech
        # speech_result = await controller.convert_speech_with_emotion(
        #     audio_data=text_to_speech(response['text']),
        #     target_emotion="calm",
        #     target_voice="therapeutic"
        # )
        
        print(f"🌸 Therapist: {response['text']}")
```

### Real-time Interactive Demo

```python
async def realtime_interaction_demo():
    """Demonstrate real-time multimodal interaction"""
    
    cap = cv2.VideoCapture(0)
    
    def get_video_frame():
        ret, frame = cap.read()
        return frame if ret else None
    
    def get_audio_chunk():
        # Implement audio capture
        return None  # Placeholder
    
    def handle_response(response):
        print(f"🤖 Greta response: {response}")
        # Handle multimodal response (speech, gestures, etc.)
    
    print("🎥 Starting real-time interaction...")
    print("Look at the camera and speak naturally")
    
    # Start real-time processing
    await controller.start_realtime_conversation(
        video_callback=get_video_frame,
        audio_callback=get_audio_chunk,
        response_callback=handle_response
    )
    
    cap.release()
```

### Cross-Cultural Research Platform

```python
async def cross_cultural_study():
    """Demonstrate cultural adaptation capabilities"""
    
    cultural_contexts = ["japanese", "american", "german", "brazilian"]
    
    test_scenario = "Please introduce yourself and explain the task"
    
    for culture in cultural_contexts:
        print(f"\n🌍 Testing {culture} cultural context:")
        
        # Configure for specific culture
        cultural_persona = {
            "name": f"Guide_{culture}",
            "cultural_context": culture,
            "formality": 0.8 if culture == "japanese" else 0.5,
            "directness": 0.9 if culture == "german" else 0.6
        }
        
        persona = await controller.create_ai_persona(cultural_persona)
        
        # Generate culturally appropriate response
        response = await controller.chat_with_llm(
            message=test_scenario,
            personality="assistant"
        )
        
        # Generate culturally appropriate gesture
        gesture = await controller.generate_gesture_from_text(
            text=response['text'],
            emotion="polite",
            style=culture
        )
        
        print(f"Response: {response['text']}")
        print(f"Gesture: {gesture['gesture_type']}")
```

## 🔧 Advanced Integration

### Custom AI Pipeline

```python
class CustomGretaPipeline:
    def __init__(self, controller):
        self.controller = controller
        self.emotion_history = []
        self.context_memory = []
    
    async def process_interaction(self, video_frame, audio_data, text_input):
        """Process a complete multimodal interaction"""
        
        # 1. Analyze current state
        emotion = await self.controller.analyze_emotion_multimodal(
            video_frame=video_frame,
            audio_data=audio_data,
            text=text_input
        )
        
        # 2. Update context
        self.emotion_history.append(emotion)
        self.context_memory.append(text_input)
        
        # 3. Generate contextual response
        context_summary = self._summarize_context()
        
        response = await self.controller.chat_with_llm(
            message=f"Context: {context_summary}\nUser: {text_input}",
            personality=self._adapt_personality(emotion)
        )
        
        # 4. Generate synchronized outputs
        gesture = await self.controller.generate_gesture_from_text(
            text=response['text'],
            emotion=emotion['primary_emotion']
        )
        
        # 5. Convert to speech with appropriate emotion
        speech = await self.controller.convert_speech_with_emotion(
            audio_data=self._text_to_speech(response['text']),
            target_emotion=emotion['primary_emotion']
        )
        
        return {
            'text': response['text'],
            'gesture': gesture,
            'speech': speech,
            'emotion': emotion
        }
    
    def _adapt_personality(self, emotion):
        """Adapt personality based on detected emotion"""
        if emotion['primary_emotion'] == 'sad':
            return "therapist"
        elif emotion['primary_emotion'] == 'confused':
            return "teacher"
        else:
            return "assistant"
    
    def _summarize_context(self):
        """Summarize recent interaction context"""
        recent_emotions = self.emotion_history[-3:]
        recent_messages = self.context_memory[-3:]
        
        return f"Recent emotions: {[e['primary_emotion'] for e in recent_emotions]}, " \
               f"Recent topics: {recent_messages}"
```

### Performance Monitoring

```python
async def monitor_ai_performance():
    """Monitor AI system performance"""
    
    while True:
        metrics = await controller.get_performance_metrics()
        
        print(f"🔍 AI Performance Metrics:")
        print(f"  LLM Response Time: {metrics.get('llm_response_time', 0)}ms")
        print(f"  Emotion Recognition: {metrics.get('emotion_accuracy', 0):.2f}")
        print(f"  Gesture Quality: {metrics.get('gesture_quality', 0):.2f}")
        print(f"  Memory Usage: {metrics.get('memory_usage_mb', 0)}MB")
        print(f"  Active Streams: {metrics.get('active_streams', 0)}")
        
        await asyncio.sleep(10)  # Update every 10 seconds
```

## 📊 Configuration Templates

### Research Configuration

```yaml
# research_config.yaml
greta:
  ai:
    llm:
      provider: "claude"  # More deterministic for research
      temperature: 0.3
      max_tokens: 200
    
    emotion:
      confidence_threshold: 0.8
      cultural_adaptation: true
      
    multimodal:
      fusion_strategy: "hierarchical"
      real_time: false  # Higher accuracy for research
```

### Educational Configuration

```yaml
# education_config.yaml
greta:
  ai:
    llm:
      provider: "openai"
      personality: "teacher"
      enthusiasm: 0.8
    
    gesture:
      style: "educational"
      intensity: "medium"
      
    speech:
      voice: "friendly_teacher"
      emotion_range: ["encouraging", "patient", "enthusiastic"]
```

### Therapeutic Configuration

```yaml
# therapy_config.yaml
greta:
  ai:
    llm:
      provider: "claude"
      personality: "therapist"
      empathy: 0.95
    
    emotion:
      sensitivity: 0.9
      cultural_context: "therapeutic"
      
    speech:
      voice: "calm_supportive"
      emotion_control: "gentle"
```

## 🚀 Running the Examples

### Quick Start

```bash
# 1. Start Greta platform
docker-compose up -d

# 2. Run basic example
cd examples/python
python greta_ai_controller.py

# 3. Select desired example from menu
```

### Individual Examples

```bash
# Voice conversation (from previous example)
python voice_conversation.py

# Complete AI controller
python greta_ai_controller.py

# Custom integration
python your_custom_script.py
```

## 📋 Troubleshooting

### Common Issues

1. **Connection Failed**
   ```python
   # Check Greta platform status
   curl http://localhost:8080/health
   
   # Verify AI services
   curl http://localhost:8080/api/ai/status
   ```

2. **Authentication Error**
   ```python
   # Update credentials in config
   config.username = "your_username"
   config.password = "your_password"
   ```

3. **AI Service Unavailable**
   ```python
   # Check which services are running
   capabilities = await controller.get_ai_capabilities()
   print(capabilities)
   ```

### Performance Tips

- Use `asyncio` for concurrent operations
- Cache frequently used models
- Adjust confidence thresholds based on use case
- Monitor memory usage for long-running sessions

---

**🎉 You now have complete Python control over all Greta AI capabilities!**

This interface provides access to cutting-edge AI features including GPT-4/Claude integration, multimodal understanding, emotion recognition, neural gesture synthesis, and real-time interaction - all through simple Python commands.