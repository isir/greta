#!/usr/bin/env python3
"""
Greta Platform Voice Conversation Example

This example demonstrates how to interact with the modernized Greta platform
using voice input (microphone) and output (speaker) for natural conversation.

Features:
- Real-time speech recognition
- Text-to-speech output through Greta
- Conversation management with context
- Behavior planning for natural responses
- WebSocket connection for real-time interaction

Requirements:
- Python 3.8+
- Greta platform running on localhost:8080
- Microphone and speakers
"""

import asyncio
import json
import logging
import threading
import time
from typing import Dict, List, Optional, Callable
import queue

# Audio processing
import pyaudio
import speech_recognition as sr
import pygame

# HTTP and WebSocket clients
import requests
import websockets
from requests.auth import HTTPBasicAuth

# Audio format conversion
import io
import wave

class GretaVoiceConversation:
    """
    Main class for voice conversation with Greta platform
    """
    
    def __init__(self, 
                 greta_host: str = "localhost",
                 greta_port: int = 8080,
                 username: str = "user",
                 password: str = "password"):
        
        self.base_url = f"http://{greta_host}:{greta_port}"
        self.ws_url = f"ws://{greta_host}:{greta_port}/ws/conversation"
        self.username = username
        self.password = password
        
        # Authentication
        self.session = requests.Session()
        self.auth_token = None
        self.conversation_id = None
        
        # Audio components
        self.recognizer = sr.Recognizer()
        self.microphone = sr.Microphone()
        self.audio_queue = queue.Queue()
        
        # Conversation state
        self.is_listening = False
        self.is_speaking = False
        self.conversation_context = []
        
        # WebSocket connection
        self.websocket = None
        
        # Callbacks
        self.on_speech_recognized: Optional[Callable[[str], None]] = None
        self.on_greta_response: Optional[Callable[[str, Dict], None]] = None
        self.on_error: Optional[Callable[[Exception], None]] = None
        
        # Setup logging
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
        
        # Initialize audio
        pygame.mixer.init()
        
    async def initialize(self) -> bool:
        """
        Initialize the conversation system
        """
        try:
            # Authenticate with Greta platform
            if not await self._authenticate():
                return False
            
            # Start conversation session
            if not await self._start_conversation():
                return False
            
            # Connect WebSocket
            if not await self._connect_websocket():
                return False
            
            # Calibrate microphone
            await self._calibrate_microphone()
            
            self.logger.info("✅ Greta voice conversation initialized successfully")
            return True
            
        except Exception as e:
            self.logger.error(f"❌ Initialization failed: {e}")
            if self.on_error:
                self.on_error(e)
            return False
    
    async def _authenticate(self) -> bool:
        """
        Authenticate with Greta platform
        """
        try:
            auth_data = {
                "username": self.username,
                "password": self.password
            }
            
            response = requests.post(
                f"{self.base_url}/api/auth/login",
                json=auth_data,
                timeout=10
            )
            
            if response.status_code == 200:
                auth_result = response.json()
                self.auth_token = auth_result.get("token")
                self.session.headers.update({
                    "Authorization": f"Bearer {self.auth_token}"
                })
                self.logger.info("🔐 Authentication successful")
                return True
            else:
                self.logger.error(f"❌ Authentication failed: {response.status_code}")
                return False
                
        except Exception as e:
            self.logger.error(f"❌ Authentication error: {e}")
            return False
    
    async def _start_conversation(self) -> bool:
        """
        Start a new conversation session with Greta
        """
        try:
            conversation_data = {
                "type": "voice_conversation",
                "mode": "interactive",
                "context": {
                    "user_preferences": {
                        "interaction_style": "friendly",
                        "response_speed": "normal",
                        "emotion": "neutral"
                    }
                }
            }
            
            response = requests.post(
                f"{self.base_url}/api/conversation/start",
                json=conversation_data,
                headers=self.session.headers,
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                self.conversation_id = result.get("conversationId")
                self.logger.info(f"💬 Conversation started: {self.conversation_id}")
                return True
            else:
                self.logger.error(f"❌ Failed to start conversation: {response.status_code}")
                return False
                
        except Exception as e:
            self.logger.error(f"❌ Conversation start error: {e}")
            return False
    
    async def _connect_websocket(self) -> bool:
        """
        Connect to Greta's WebSocket for real-time interaction
        """
        try:
            headers = {
                "Authorization": f"Bearer {self.auth_token}",
                "Conversation-ID": self.conversation_id
            }
            
            self.websocket = await websockets.connect(
                self.ws_url,
                extra_headers=headers
            )
            
            # Start WebSocket message handler
            asyncio.create_task(self._handle_websocket_messages())
            
            self.logger.info("🔌 WebSocket connected")
            return True
            
        except Exception as e:
            self.logger.error(f"❌ WebSocket connection failed: {e}")
            return False
    
    async def _calibrate_microphone(self):
        """
        Calibrate microphone for ambient noise
        """
        self.logger.info("🎤 Calibrating microphone for ambient noise...")
        with self.microphone as source:
            self.recognizer.adjust_for_ambient_noise(source, duration=2)
        self.logger.info("✅ Microphone calibrated")
    
    async def start_conversation(self):
        """
        Start the main conversation loop
        """
        self.logger.info("🗣️ Starting voice conversation with Greta")
        self.logger.info("Say 'Hello Greta' to begin, or 'goodbye' to exit")
        
        # Start listening in background
        self.is_listening = True
        listening_thread = threading.Thread(target=self._continuous_listening)
        listening_thread.daemon = True
        listening_thread.start()
        
        # Send initial greeting
        await self._send_greta_message("Hello! I'm ready to talk with you.")
        
        try:
            # Main conversation loop
            while self.is_listening:
                await asyncio.sleep(0.1)  # Small delay to prevent busy waiting
                
        except KeyboardInterrupt:
            self.logger.info("🛑 Conversation interrupted by user")
        finally:
            await self.stop_conversation()
    
    def _continuous_listening(self):
        """
        Continuously listen for speech input (runs in separate thread)
        """
        while self.is_listening:
            try:
                # Listen for speech
                with self.microphone as source:
                    # Adjust for ambient noise periodically
                    if time.time() % 30 < 1:  # Every 30 seconds
                        self.recognizer.adjust_for_ambient_noise(source, duration=0.5)
                    
                    self.logger.debug("🎤 Listening...")
                    audio = self.recognizer.listen(source, timeout=1, phrase_time_limit=5)
                
                # Recognize speech
                try:
                    text = self.recognizer.recognize_google(audio)
                    if text.strip():
                        self.logger.info(f"👤 User said: {text}")
                        
                        # Handle the recognized speech
                        asyncio.run_coroutine_threadsafe(
                            self._handle_user_speech(text),
                            asyncio.get_event_loop()
                        )
                        
                        if self.on_speech_recognized:
                            self.on_speech_recognized(text)
                            
                except sr.UnknownValueError:
                    # Could not understand audio
                    continue
                except sr.RequestError as e:
                    self.logger.error(f"❌ Speech recognition error: {e}")
                    
            except sr.WaitTimeoutError:
                # Timeout is normal, continue listening
                continue
            except Exception as e:
                self.logger.error(f"❌ Listening error: {e}")
                time.sleep(1)  # Wait before retrying
    
    async def _handle_user_speech(self, text: str):
        """
        Handle recognized user speech
        """
        # Check for exit commands
        if any(word in text.lower() for word in ['goodbye', 'bye', 'exit', 'quit']):
            self.logger.info("👋 User said goodbye")
            await self._send_greta_message("Goodbye! It was nice talking with you.")
            await asyncio.sleep(3)  # Wait for response to complete
            self.is_listening = False
            return
        
        # Add to conversation context
        self.conversation_context.append({
            "role": "user",
            "content": text,
            "timestamp": time.time()
        })
        
        # Send to Greta for processing
        await self._process_with_greta(text)
    
    async def _process_with_greta(self, user_text: str):
        """
        Process user input with Greta's behavior planning and generate response
        """
        try:
            # Plan behavior based on user input
            behavior_request = {
                "intention": self._classify_intention(user_text),
                "content": user_text,
                "context": {
                    "conversation_history": self.conversation_context[-5:],  # Last 5 exchanges
                    "emotion": self._detect_emotion(user_text),
                    "formality": "casual",
                    "response_style": "conversational"
                }
            }
            
            response = requests.post(
                f"{self.base_url}/api/behavior/plan",
                json=behavior_request,
                headers=self.session.headers,
                timeout=15
            )
            
            if response.status_code == 200:
                behavior_plan = response.json()
                await self._execute_behavior_plan(behavior_plan)
            else:
                self.logger.error(f"❌ Behavior planning failed: {response.status_code}")
                await self._fallback_response(user_text)
                
        except Exception as e:
            self.logger.error(f"❌ Error processing with Greta: {e}")
            await self._fallback_response(user_text)
    
    async def _execute_behavior_plan(self, behavior_plan: Dict):
        """
        Execute Greta's behavior plan (speech + gestures + expressions)
        """
        try:
            # Extract speech content
            speech_content = behavior_plan.get("speech", {}).get("content", "")
            
            if not speech_content:
                # Generate speech if not provided
                speech_content = await self._generate_speech_response(behavior_plan)
            
            # Execute the complete behavior (including animations)
            execution_request = {
                "planId": behavior_plan.get("planId"),
                "conversationId": self.conversation_id,
                "includeAudio": True,
                "audioFormat": "wav"
            }
            
            response = requests.post(
                f"{self.base_url}/api/behavior/execute",
                json=execution_request,
                headers=self.session.headers,
                timeout=20
            )
            
            if response.status_code == 200:
                execution_result = response.json()
                
                # Play speech through speakers
                if speech_content:
                    await self._speak_text(speech_content)
                
                # Log the response
                self.logger.info(f"🤖 Greta: {speech_content}")
                
                # Add to conversation context
                self.conversation_context.append({
                    "role": "greta",
                    "content": speech_content,
                    "timestamp": time.time(),
                    "behavior_plan": behavior_plan
                })
                
                if self.on_greta_response:
                    self.on_greta_response(speech_content, behavior_plan)
                    
            else:
                self.logger.error(f"❌ Behavior execution failed: {response.status_code}")
                await self._fallback_response()
                
        except Exception as e:
            self.logger.error(f"❌ Error executing behavior: {e}")
            await self._fallback_response()
    
    async def _speak_text(self, text: str):
        """
        Convert text to speech and play through speakers
        """
        try:
            # Request TTS from Greta platform
            tts_request = {
                "text": text,
                "voice": "default",
                "speed": 1.0,
                "emotion": "neutral"
            }
            
            response = requests.post(
                f"{self.base_url}/api/tts/synthesize",
                json=tts_request,
                headers=self.session.headers,
                timeout=10
            )
            
            if response.status_code == 200:
                # Get audio data
                audio_data = response.content
                
                # Play audio using pygame
                audio_file = io.BytesIO(audio_data)
                pygame.mixer.music.load(audio_file)
                pygame.mixer.music.play()
                
                # Wait for playback to complete
                while pygame.mixer.music.get_busy():
                    await asyncio.sleep(0.1)
                    
            else:
                self.logger.warning("⚠️ TTS failed, using fallback")
                # Fallback: could use local TTS or just print
                print(f"🤖 Greta: {text}")
                
        except Exception as e:
            self.logger.error(f"❌ TTS error: {e}")
            print(f"🤖 Greta: {text}")  # Fallback to text output
    
    async def _generate_speech_response(self, behavior_plan: Dict) -> str:
        """
        Generate speech response if not provided in behavior plan
        """
        try:
            generation_request = {
                "behaviorPlan": behavior_plan,
                "conversationContext": self.conversation_context[-3:],
                "responseStyle": "natural"
            }
            
            response = requests.post(
                f"{self.base_url}/api/nlg/generate",
                json=generation_request,
                headers=self.session.headers,
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                return result.get("generatedText", "I understand.")
            else:
                return "I understand."
                
        except Exception as e:
            self.logger.error(f"❌ Speech generation error: {e}")
            return "I see."
    
    def _classify_intention(self, text: str) -> str:
        """
        Classify user's communicative intention
        """
        text_lower = text.lower()
        
        if any(word in text_lower for word in ['hello', 'hi', 'hey', 'good morning']):
            return "greeting"
        elif any(word in text_lower for word in ['?', 'what', 'how', 'why', 'when', 'where']):
            return "question"
        elif any(word in text_lower for word in ['thank', 'thanks', 'appreciate']):
            return "gratitude"
        elif any(word in text_lower for word in ['sorry', 'apologize', 'excuse me']):
            return "apology"
        elif any(word in text_lower for word in ['goodbye', 'bye', 'see you']):
            return "farewell"
        else:
            return "statement"
    
    def _detect_emotion(self, text: str) -> str:
        """
        Simple emotion detection from text
        """
        text_lower = text.lower()
        
        if any(word in text_lower for word in ['happy', 'great', 'wonderful', 'amazing', '!']):
            return "happy"
        elif any(word in text_lower for word in ['sad', 'unhappy', 'disappointed']):
            return "sad"
        elif any(word in text_lower for word in ['angry', 'mad', 'frustrated']):
            return "angry"
        elif any(word in text_lower for word in ['worried', 'concerned', 'anxious']):
            return "worried"
        else:
            return "neutral"
    
    async def _fallback_response(self, user_text: str = ""):
        """
        Provide fallback response when Greta processing fails
        """
        fallback_responses = [
            "I'm sorry, could you repeat that?",
            "That's interesting. Could you tell me more?",
            "I understand. Please continue.",
            "Let me think about that.",
            "Could you explain that differently?"
        ]
        
        import random
        response = random.choice(fallback_responses)
        await self._speak_text(response)
        
        self.logger.info(f"🤖 Greta (fallback): {response}")
    
    async def _send_greta_message(self, message: str):
        """
        Send a message from Greta (for system-initiated communication)
        """
        await self._speak_text(message)
        self.logger.info(f"🤖 Greta: {message}")
        
        self.conversation_context.append({
            "role": "greta",
            "content": message,
            "timestamp": time.time()
        })
    
    async def _handle_websocket_messages(self):
        """
        Handle incoming WebSocket messages from Greta
        """
        try:
            async for message in self.websocket:
                data = json.loads(message)
                message_type = data.get("type")
                
                if message_type == "behavior_update":
                    # Handle real-time behavior updates
                    self.logger.debug(f"📡 Behavior update: {data}")
                elif message_type == "animation_complete":
                    # Handle animation completion
                    self.logger.debug(f"🎭 Animation completed: {data}")
                elif message_type == "error":
                    # Handle errors
                    self.logger.error(f"❌ WebSocket error: {data}")
                    
        except websockets.exceptions.ConnectionClosed:
            self.logger.warning("🔌 WebSocket connection closed")
        except Exception as e:
            self.logger.error(f"❌ WebSocket error: {e}")
    
    async def stop_conversation(self):
        """
        Stop the conversation and cleanup resources
        """
        self.logger.info("🛑 Stopping conversation...")
        
        self.is_listening = False
        
        # Close WebSocket
        if self.websocket:
            await self.websocket.close()
        
        # End conversation session
        if self.conversation_id:
            try:
                requests.post(
                    f"{self.base_url}/api/conversation/{self.conversation_id}/end",
                    headers=self.session.headers,
                    timeout=5
                )
            except Exception as e:
                self.logger.error(f"❌ Error ending conversation: {e}")
        
        # Cleanup audio
        pygame.mixer.quit()
        
        self.logger.info("✅ Conversation stopped")


# Example usage functions
async def basic_conversation_example():
    """
    Basic conversation example
    """
    print("🚀 Starting basic conversation with Greta...")
    
    # Create conversation instance
    greta = GretaVoiceConversation(
        greta_host="localhost",
        greta_port=8080,
        username="demo_user",
        password="demo_pass"
    )
    
    # Set up callbacks
    def on_speech(text):
        print(f"📝 Recognized: {text}")
    
    def on_response(text, behavior):
        print(f"🎭 Greta responded with behavior: {behavior.get('intention', 'unknown')}")
    
    def on_error(error):
        print(f"⚠️ Error: {error}")
    
    greta.on_speech_recognized = on_speech
    greta.on_greta_response = on_response
    greta.on_error = on_error
    
    # Initialize and start conversation
    if await greta.initialize():
        await greta.start_conversation()
    else:
        print("❌ Failed to initialize conversation")


async def advanced_conversation_example():
    """
    Advanced conversation example with custom behavior
    """
    print("🚀 Starting advanced conversation with Greta...")
    
    class CustomGretaConversation(GretaVoiceConversation):
        def _classify_intention(self, text: str) -> str:
            # Custom intention classification
            text_lower = text.lower()
            
            if 'weather' in text_lower:
                return "weather_inquiry"
            elif 'time' in text_lower:
                return "time_inquiry"
            elif any(word in text_lower for word in ['teach', 'learn', 'explain']):
                return "educational_request"
            else:
                return super()._classify_intention(text)
        
        def _detect_emotion(self, text: str) -> str:
            # Enhanced emotion detection
            # You could integrate with sentiment analysis libraries here
            return super()._detect_emotion(text)
    
    # Create custom conversation
    greta = CustomGretaConversation()
    
    if await greta.initialize():
        await greta.start_conversation()


if __name__ == "__main__":
    """
    Main entry point
    """
    print("🎤 Greta Voice Conversation Example")
    print("=====================================")
    print()
    print("Prerequisites:")
    print("- Greta platform running on localhost:8080")
    print("- Microphone and speakers connected")
    print("- Required Python packages installed")
    print()
    
    # Choose example to run
    example_choice = input("Choose example (1=basic, 2=advanced): ").strip()
    
    if example_choice == "2":
        asyncio.run(advanced_conversation_example())
    else:
        asyncio.run(basic_conversation_example())