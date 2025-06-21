#!/usr/bin/env python3
"""
Greta AI Controller - Comprehensive Python Interface

This module provides a complete Python interface to control all the advanced AI capabilities
of the modernized Greta platform, including LLM integration, multimodal AI, emotion recognition,
neural gesture synthesis, and real-time interaction.

Usage:
    python greta_ai_controller.py

Requirements:
    - Greta platform running with AI enhancements
    - Python 3.8+
    - Required packages: requests, websockets, opencv-python, numpy, asyncio
"""

import asyncio
import json
import logging
import time
from dataclasses import dataclass
from typing import Dict, List, Optional, Callable, Any
import base64
import io

# Standard libraries
import requests
import websockets
import cv2
import numpy as np
from requests.auth import HTTPBasicAuth

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class GretaConfig:
    """Configuration for Greta AI platform connection"""
    host: str = "localhost"
    port: int = 8080
    username: str = "demo_user"
    password: str = "demo_pass"
    api_key: str = ""  # For LLM providers
    
class GretaAIController:
    """
    Main controller for all Greta AI capabilities
    Provides Python interface to:
    - LLM Integration (GPT-4, Claude)
    - Speech-to-Speech synthesis
    - Emotion Recognition
    - Multimodal AI
    - Neural Gesture Synthesis
    - Real-time interaction
    """
    
    def __init__(self, config: GretaConfig):
        self.config = config
        self.base_url = f"http://{config.host}:{config.port}"
        self.ws_url = f"ws://{config.host}:{config.port}/ws"
        self.session = requests.Session()
        self.auth_token = None
        self.websocket = None
        
        # AI component status
        self.llm_enabled = False
        self.emotion_enabled = False
        self.multimodal_enabled = False
        self.gesture_enabled = False
        self.speech2speech_enabled = False
        
    async def initialize(self) -> bool:
        """Initialize connection to Greta platform"""
        try:
            logger.info("🚀 Initializing Greta AI Controller...")
            
            # Authenticate
            if not await self._authenticate():
                return False
            
            # Check AI capabilities
            await self._check_ai_capabilities()
            
            # Connect WebSocket
            await self._connect_websocket()
            
            logger.info("✅ Greta AI Controller initialized successfully")
            logger.info(f"📊 Available capabilities: LLM={self.llm_enabled}, "
                       f"Emotion={self.emotion_enabled}, Multimodal={self.multimodal_enabled}, "
                       f"Gesture={self.gesture_enabled}, Speech2Speech={self.speech2speech_enabled}")
            return True
            
        except Exception as e:
            logger.error(f"❌ Initialization failed: {e}")
            return False
    
    # =============================================================================
    # 🧠 LLM Integration Methods
    # =============================================================================
    
    async def chat_with_llm(self, message: str, personality: str = "assistant") -> Dict[str, Any]:
        """
        Chat with advanced LLM integration
        
        Args:
            message: User message
            personality: Personality profile (teacher, therapist, researcher, assistant)
            
        Returns:
            Dictionary with response text, confidence, and metadata
        """
        try:
            request_data = {
                "message": message,
                "personality": personality,
                "provider": "openai",  # or "claude"
                "model": "gpt-4",
                "temperature": 0.7,
                "max_tokens": 150
            }
            
            response = await self._post_async("/api/ai/llm/chat", request_data)
            
            return {
                "text": response.get("response_text", ""),
                "confidence": response.get("confidence", 0.0),
                "personality_used": response.get("personality_profile", {}),
                "processing_time": response.get("processing_time_ms", 0),
                "provider": response.get("provider", ""),
                "usage": response.get("usage", {})
            }
            
        except Exception as e:
            logger.error(f"❌ LLM chat error: {e}")
            return {"text": "I apologize, but I'm having trouble processing your request.", "confidence": 0.0}
    
    async def generate_personality_response(self, message: str, personality_traits: Dict[str, float]) -> Dict[str, Any]:
        """
        Generate response with custom personality traits
        
        Args:
            message: User message
            personality_traits: Dict with traits like {"openness": 0.8, "empathy": 0.9}
            
        Returns:
            Personalized response
        """
        try:
            request_data = {
                "message": message,
                "personality_traits": personality_traits,
                "provider": "openai"
            }
            
            response = await self._post_async("/api/ai/llm/personality", request_data)
            return response
            
        except Exception as e:
            logger.error(f"❌ Personality response error: {e}")
            return {"text": "Error generating personality response", "confidence": 0.0}
    
    # =============================================================================
    # 🎤 Speech-to-Speech Methods
    # =============================================================================
    
    async def convert_speech_with_emotion(self, audio_data: bytes, target_emotion: str, 
                                        target_voice: str = "default") -> Dict[str, Any]:
        """
        Convert speech with emotion and voice control
        
        Args:
            audio_data: Input audio bytes
            target_emotion: Target emotion (happy, sad, angry, calm, excited)
            target_voice: Target voice profile
            
        Returns:
            Converted audio and metadata
        """
        try:
            # Encode audio to base64 for JSON transmission
            audio_b64 = base64.b64encode(audio_data).decode('utf-8')
            
            request_data = {
                "audio_data": audio_b64,
                "target_emotion": target_emotion,
                "target_voice": target_voice,
                "provider": "bark",
                "quality": "high"
            }
            
            response = await self._post_async("/api/ai/speech2speech/convert", request_data)
            
            # Decode returned audio
            output_audio = base64.b64decode(response.get("output_audio", ""))
            
            return {
                "audio_data": output_audio,
                "emotion": response.get("detected_emotion", ""),
                "voice_profile": response.get("voice_profile", ""),
                "quality_score": response.get("quality_score", 0.0),
                "processing_time": response.get("processing_time_ms", 0)
            }
            
        except Exception as e:
            logger.error(f"❌ Speech conversion error: {e}")
            return {"audio_data": b"", "emotion": "neutral"}
    
    async def clone_voice(self, reference_audio_files: List[bytes], speaker_name: str) -> Dict[str, Any]:
        """
        Clone a voice from reference audio samples
        
        Args:
            reference_audio_files: List of reference audio samples
            speaker_name: Name for the cloned voice
            
        Returns:
            Voice profile information
        """
        try:
            # Encode all reference files
            reference_b64 = [base64.b64encode(audio).decode('utf-8') for audio in reference_audio_files]
            
            request_data = {
                "reference_audio": reference_b64,
                "speaker_name": speaker_name,
                "provider": "bark"
            }
            
            response = await self._post_async("/api/ai/speech2speech/clone-voice", request_data)
            return response
            
        except Exception as e:
            logger.error(f"❌ Voice cloning error: {e}")
            return {"success": False, "error": str(e)}
    
    # =============================================================================
    # 😊 Emotion Recognition Methods
    # =============================================================================
    
    async def analyze_emotion_multimodal(self, video_frame: np.ndarray = None, 
                                       audio_data: bytes = None, text: str = None) -> Dict[str, Any]:
        """
        Analyze emotion from multiple modalities
        
        Args:
            video_frame: Video frame as numpy array (for facial emotion)
            audio_data: Audio data bytes (for voice emotion)
            text: Text content (for sentiment analysis)
            
        Returns:
            Comprehensive emotion analysis
        """
        try:
            request_data = {"modalities": []}
            
            # Add video data if provided
            if video_frame is not None:
                # Encode video frame
                _, buffer = cv2.imencode('.jpg', video_frame)
                frame_b64 = base64.b64encode(buffer).decode('utf-8')
                request_data["modalities"].append({
                    "type": "facial",
                    "data": frame_b64
                })
            
            # Add audio data if provided
            if audio_data is not None:
                audio_b64 = base64.b64encode(audio_data).decode('utf-8')
                request_data["modalities"].append({
                    "type": "voice",
                    "data": audio_b64
                })
            
            # Add text if provided
            if text is not None:
                request_data["modalities"].append({
                    "type": "text",
                    "data": text
                })
            
            response = await self._post_async("/api/ai/emotion/analyze", request_data)
            
            return {
                "primary_emotion": response.get("primary_emotion", "neutral"),
                "confidence": response.get("confidence", 0.0),
                "arousal": response.get("arousal", 0.0),
                "valence": response.get("valence", 0.0),
                "dominance": response.get("dominance", 0.0),
                "emotion_probabilities": response.get("emotion_probabilities", {}),
                "modalities_used": response.get("modalities_detected", []),
                "description": response.get("description", "")
            }
            
        except Exception as e:
            logger.error(f"❌ Emotion analysis error: {e}")
            return {"primary_emotion": "neutral", "confidence": 0.0}
    
    async def start_emotion_monitoring(self, callback: Callable[[Dict], None], 
                                     interval_ms: int = 1000) -> None:
        """
        Start real-time emotion monitoring
        
        Args:
            callback: Function to call with emotion updates
            interval_ms: Update interval in milliseconds
        """
        try:
            config_data = {
                "interval_ms": interval_ms,
                "voice_enabled": True,
                "facial_enabled": True,
                "multimodal_fusion": True
            }
            
            await self._post_async("/api/ai/emotion/start-monitoring", config_data)
            
            # Listen for emotion updates via WebSocket
            if self.websocket:
                async for message in self.websocket:
                    data = json.loads(message)
                    if data.get("type") == "emotion_update":
                        callback(data.get("emotion_state", {}))
            
        except Exception as e:
            logger.error(f"❌ Emotion monitoring error: {e}")
    
    # =============================================================================
    # 👁️ Multimodal AI Methods
    # =============================================================================
    
    async def analyze_scene_multimodal(self, video_frame: np.ndarray, audio_data: bytes = None, 
                                     query: str = None) -> Dict[str, Any]:
        """
        Comprehensive multimodal scene analysis
        
        Args:
            video_frame: Video frame for visual analysis
            audio_data: Optional audio data
            query: Optional natural language query about the scene
            
        Returns:
            Comprehensive scene understanding
        """
        try:
            # Encode video frame
            _, buffer = cv2.imencode('.jpg', video_frame)
            frame_b64 = base64.b64encode(buffer).decode('utf-8')
            
            request_data = {
                "vision_input": frame_b64,
                "query": query or "What is happening in this scene?",
                "include_objects": True,
                "include_emotions": True,
                "include_gestures": True
            }
            
            # Add audio if provided
            if audio_data:
                audio_b64 = base64.b64encode(audio_data).decode('utf-8')
                request_data["audio_input"] = audio_b64
            
            response = await self._post_async("/api/ai/multimodal/analyze-scene", request_data)
            
            return {
                "scene_description": response.get("scene_description", ""),
                "detected_objects": response.get("detected_objects", []),
                "detected_people": response.get("detected_people", []),
                "detected_gestures": response.get("detected_gestures", []),
                "emotional_analysis": response.get("emotional_analysis", {}),
                "spatial_relationships": response.get("spatial_relationships", []),
                "confidence": response.get("confidence", 0.0),
                "processing_time": response.get("processing_time_ms", 0)
            }
            
        except Exception as e:
            logger.error(f"❌ Multimodal analysis error: {e}")
            return {"scene_description": "Unable to analyze scene", "confidence": 0.0}
    
    async def understand_gesture_in_context(self, video_frame: np.ndarray, 
                                          speech_text: str = None) -> Dict[str, Any]:
        """
        Understand gestures in conversational context
        
        Args:
            video_frame: Video frame containing gesture
            speech_text: Accompanying speech text for context
            
        Returns:
            Gesture understanding with contextual meaning
        """
        try:
            _, buffer = cv2.imencode('.jpg', video_frame)
            frame_b64 = base64.b64encode(buffer).decode('utf-8')
            
            request_data = {
                "gesture_input": frame_b64,
                "speech_context": speech_text,
                "cultural_context": "neutral",
                "interaction_mode": "conversational"
            }
            
            response = await self._post_async("/api/ai/multimodal/understand-gesture", request_data)
            
            return {
                "gesture_type": response.get("gesture_type", "unknown"),
                "contextual_meaning": response.get("contextual_meaning", ""),
                "intended_action": response.get("intended_action", ""),
                "confidence": response.get("confidence", 0.0),
                "cultural_appropriateness": response.get("cultural_appropriateness", 0.0)
            }
            
        except Exception as e:
            logger.error(f"❌ Gesture understanding error: {e}")
            return {"gesture_type": "unknown", "confidence": 0.0}
    
    # =============================================================================
    # 🤲 Neural Gesture Synthesis Methods
    # =============================================================================
    
    async def generate_gesture_from_text(self, text: str, emotion: str = "neutral", 
                                       style: str = "casual") -> Dict[str, Any]:
        """
        Generate natural gestures from text description
        
        Args:
            text: Text describing the intended gesture or speech content
            emotion: Emotional context for gesture generation
            style: Gesture style (casual, formal, energetic, calm)
            
        Returns:
            Generated gesture sequence data
        """
        try:
            request_data = {
                "text_input": text,
                "emotion": emotion,
                "style": style,
                "cultural_context": "neutral",
                "character_rig": "greta_default",
                "duration_preference": "auto"
            }
            
            response = await self._post_async("/api/ai/gesture/generate-from-text", request_data)
            
            return {
                "gesture_sequence": response.get("gesture_sequence", {}),
                "duration_ms": response.get("duration_ms", 0),
                "quality_score": response.get("quality_score", 0.0),
                "gesture_type": response.get("gesture_type", ""),
                "keyframes": response.get("keyframes", []),
                "animation_data": response.get("animation_data", {})
            }
            
        except Exception as e:
            logger.error(f"❌ Gesture generation error: {e}")
            return {"gesture_sequence": {}, "quality_score": 0.0}
    
    async def generate_co_speech_gestures(self, speech_audio: bytes, 
                                        speech_text: str) -> Dict[str, Any]:
        """
        Generate gestures synchronized with speech
        
        Args:
            speech_audio: Audio data of the speech
            speech_text: Transcript of the speech
            
        Returns:
            Speech-synchronized gesture data
        """
        try:
            audio_b64 = base64.b64encode(speech_audio).decode('utf-8')
            
            request_data = {
                "speech_audio": audio_b64,
                "speech_text": speech_text,
                "sync_mode": "prosodic",  # or "semantic"
                "gesture_intensity": "medium",
                "cultural_style": "neutral"
            }
            
            response = await self._post_async("/api/ai/gesture/generate-co-speech", request_data)
            
            return {
                "synchronized_gestures": response.get("synchronized_gestures", []),
                "speech_duration_ms": response.get("speech_duration_ms", 0),
                "gesture_timing": response.get("gesture_timing", []),
                "synchronization_quality": response.get("sync_quality", 0.0)
            }
            
        except Exception as e:
            logger.error(f"❌ Co-speech gesture generation error: {e}")
            return {"synchronized_gestures": [], "sync_quality": 0.0}
    
    # =============================================================================
    # 🌐 Real-time Interaction Methods
    # =============================================================================
    
    async def start_realtime_conversation(self, video_callback: Callable = None,
                                        audio_callback: Callable = None,
                                        response_callback: Callable = None) -> None:
        """
        Start real-time multimodal conversation
        
        Args:
            video_callback: Called when video frame is needed
            audio_callback: Called when audio data is needed  
            response_callback: Called when Greta responds
        """
        try:
            # Configure real-time session
            session_config = {
                "enable_video": video_callback is not None,
                "enable_audio": audio_callback is not None,
                "enable_emotion_tracking": True,
                "enable_gesture_recognition": True,
                "enable_multimodal_fusion": True,
                "response_mode": "adaptive",
                "personality": "conversational"
            }
            
            await self._post_async("/api/ai/realtime/start-session", session_config)
            
            # Start real-time processing loop
            while True:
                try:
                    # Get video frame if callback provided
                    if video_callback:
                        frame = video_callback()
                        if frame is not None:
                            await self._process_realtime_video(frame)
                    
                    # Get audio data if callback provided
                    if audio_callback:
                        audio = audio_callback()
                        if audio is not None:
                            await self._process_realtime_audio(audio)
                    
                    # Check for responses
                    if self.websocket:
                        try:
                            message = await asyncio.wait_for(
                                self.websocket.recv(), timeout=0.1)
                            data = json.loads(message)
                            
                            if data.get("type") == "multimodal_response" and response_callback:
                                response_callback(data.get("response", {}))
                                
                        except asyncio.TimeoutError:
                            pass
                    
                    await asyncio.sleep(0.033)  # ~30 FPS
                    
                except KeyboardInterrupt:
                    break
                except Exception as e:
                    logger.error(f"Real-time processing error: {e}")
                    await asyncio.sleep(1)
            
        except Exception as e:
            logger.error(f"❌ Real-time conversation error: {e}")
    
    # =============================================================================
    # 🔧 Advanced Integration Methods
    # =============================================================================
    
    async def create_ai_persona(self, persona_config: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create a complete AI persona combining all capabilities
        
        Args:
            persona_config: Configuration for the AI persona
            
        Returns:
            Created persona information
        """
        try:
            request_data = {
                "persona_name": persona_config.get("name", "CustomPersona"),
                "personality_traits": persona_config.get("personality", {}),
                "voice_profile": persona_config.get("voice", "default"),
                "gesture_style": persona_config.get("gestures", "neutral"),
                "emotion_sensitivity": persona_config.get("emotion_sensitivity", 0.7),
                "cultural_adaptation": persona_config.get("cultural_context", "neutral"),
                "interaction_mode": persona_config.get("mode", "conversational")
            }
            
            response = await self._post_async("/api/ai/persona/create", request_data)
            return response
            
        except Exception as e:
            logger.error(f"❌ Persona creation error: {e}")
            return {"success": False, "error": str(e)}
    
    async def get_ai_capabilities(self) -> Dict[str, Any]:
        """Get current AI capabilities and status"""
        try:
            response = await self._get_async("/api/ai/capabilities")
            return response
        except Exception as e:
            logger.error(f"❌ Error getting capabilities: {e}")
            return {}
    
    async def get_performance_metrics(self) -> Dict[str, Any]:
        """Get AI performance metrics"""
        try:
            response = await self._get_async("/api/ai/metrics")
            return response
        except Exception as e:
            logger.error(f"❌ Error getting metrics: {e}")
            return {}
    
    # =============================================================================
    # 🔗 Helper Methods
    # =============================================================================
    
    async def _authenticate(self) -> bool:
        """Authenticate with Greta platform"""
        try:
            auth_data = {
                "username": self.config.username,
                "password": self.config.password
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
                logger.info("🔐 Authentication successful")
                return True
            else:
                logger.error(f"❌ Authentication failed: {response.status_code}")
                return False
                
        except Exception as e:
            logger.error(f"❌ Authentication error: {e}")
            return False
    
    async def _check_ai_capabilities(self) -> None:
        """Check which AI capabilities are available"""
        try:
            response = requests.get(
                f"{self.base_url}/api/ai/status",
                headers=self.session.headers,
                timeout=5
            )
            
            if response.status_code == 200:
                status = response.json()
                self.llm_enabled = status.get("llm_available", False)
                self.emotion_enabled = status.get("emotion_available", False)
                self.multimodal_enabled = status.get("multimodal_available", False)
                self.gesture_enabled = status.get("gesture_available", False)
                self.speech2speech_enabled = status.get("speech2speech_available", False)
            
        except Exception as e:
            logger.warning(f"⚠️ Could not check AI capabilities: {e}")
    
    async def _connect_websocket(self) -> None:
        """Connect to WebSocket for real-time communication"""
        try:
            headers = {
                "Authorization": f"Bearer {self.auth_token}"
            }
            
            self.websocket = await websockets.connect(
                f"{self.ws_url}/ai",
                extra_headers=headers
            )
            
            logger.info("🔌 WebSocket connected")
            
        except Exception as e:
            logger.warning(f"⚠️ WebSocket connection failed: {e}")
    
    async def _post_async(self, endpoint: str, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make async POST request"""
        loop = asyncio.get_event_loop()
        
        def make_request():
            response = requests.post(
                f"{self.base_url}{endpoint}",
                json=data,
                headers=self.session.headers,
                timeout=30
            )
            return response.json() if response.status_code == 200 else {}
        
        return await loop.run_in_executor(None, make_request)
    
    async def _get_async(self, endpoint: str) -> Dict[str, Any]:
        """Make async GET request"""
        loop = asyncio.get_event_loop()
        
        def make_request():
            response = requests.get(
                f"{self.base_url}{endpoint}",
                headers=self.session.headers,
                timeout=10
            )
            return response.json() if response.status_code == 200 else {}
        
        return await loop.run_in_executor(None, make_request)
    
    async def _process_realtime_video(self, frame: np.ndarray) -> None:
        """Process video frame in real-time"""
        try:
            _, buffer = cv2.imencode('.jpg', frame)
            frame_b64 = base64.b64encode(buffer).decode('utf-8')
            
            if self.websocket:
                await self.websocket.send(json.dumps({
                    "type": "video_frame",
                    "data": frame_b64,
                    "timestamp": time.time()
                }))
        except Exception as e:
            logger.debug(f"Video processing error: {e}")
    
    async def _process_realtime_audio(self, audio_data: bytes) -> None:
        """Process audio data in real-time"""
        try:
            audio_b64 = base64.b64encode(audio_data).decode('utf-8')
            
            if self.websocket:
                await self.websocket.send(json.dumps({
                    "type": "audio_chunk",
                    "data": audio_b64,
                    "timestamp": time.time()
                }))
        except Exception as e:
            logger.debug(f"Audio processing error: {e}")
    
    async def shutdown(self) -> None:
        """Shutdown the controller and cleanup resources"""
        logger.info("🛑 Shutting down Greta AI Controller")
        
        if self.websocket:
            await self.websocket.close()
        
        # Stop any active sessions
        try:
            await self._post_async("/api/ai/realtime/stop-session", {})
            await self._post_async("/api/ai/emotion/stop-monitoring", {})
        except:
            pass

# =============================================================================
# 🎮 Example Usage Functions
# =============================================================================

async def example_basic_chat():
    """Example: Basic chat with personality"""
    config = GretaConfig()
    controller = GretaAIController(config)
    
    if await controller.initialize():
        # Chat with different personalities
        personalities = ["teacher", "therapist", "researcher", "assistant"]
        
        for personality in personalities:
            print(f"\n🎭 Chatting with {personality} personality:")
            
            response = await controller.chat_with_llm(
                "Hello! Can you tell me about yourself?",
                personality=personality
            )
            
            print(f"Response: {response['text']}")
            print(f"Confidence: {response['confidence']:.2f}")
    
    await controller.shutdown()

async def example_emotion_analysis():
    """Example: Real-time emotion analysis"""
    config = GretaConfig()
    controller = GretaAIController(config)
    
    if await controller.initialize():
        # Simulate video capture for emotion analysis
        cap = cv2.VideoCapture(0)  # Use webcam
        
        print("📹 Starting emotion analysis (press 'q' to quit)")
        
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            
            # Analyze emotion from video frame
            emotion_result = await controller.analyze_emotion_multimodal(
                video_frame=frame
            )
            
            print(f"Emotion: {emotion_result['primary_emotion']} "
                  f"(confidence: {emotion_result['confidence']:.2f})")
            
            # Display frame with emotion overlay
            cv2.putText(frame, f"Emotion: {emotion_result['primary_emotion']}", 
                       (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
            cv2.imshow('Emotion Analysis', frame)
            
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
        
        cap.release()
        cv2.destroyAllWindows()
    
    await controller.shutdown()

async def example_multimodal_scene():
    """Example: Multimodal scene understanding"""
    config = GretaConfig()
    controller = GretaAIController(config)
    
    if await controller.initialize():
        # Load an image for analysis
        image_path = "test_image.jpg"  # Replace with actual image
        
        if cv2.os.path.exists(image_path):
            frame = cv2.imread(image_path)
            
            # Analyze the scene
            scene_result = await controller.analyze_scene_multimodal(
                video_frame=frame,
                query="What objects and people do you see in this image?"
            )
            
            print("🖼️ Scene Analysis Results:")
            print(f"Description: {scene_result['scene_description']}")
            print(f"Objects: {scene_result['detected_objects']}")
            print(f"People: {scene_result['detected_people']}")
            print(f"Confidence: {scene_result['confidence']:.2f}")
        else:
            print("❌ Please provide a test image at 'test_image.jpg'")
    
    await controller.shutdown()

async def example_gesture_generation():
    """Example: Neural gesture generation"""
    config = GretaConfig()
    controller = GretaAIController(config)
    
    if await controller.initialize():
        # Generate gestures from text
        gesture_texts = [
            "Welcome everyone to today's presentation",
            "This is a very important point",
            "Let me show you something interesting",
            "Thank you for your attention"
        ]
        
        for text in gesture_texts:
            print(f"\n🤲 Generating gesture for: '{text}'")
            
            gesture_result = await controller.generate_gesture_from_text(
                text=text,
                emotion="enthusiastic",
                style="formal"
            )
            
            print(f"Gesture type: {gesture_result['gesture_type']}")
            print(f"Duration: {gesture_result['duration_ms']}ms")
            print(f"Quality: {gesture_result['quality_score']:.2f}")
    
    await controller.shutdown()

async def example_complete_ai_persona():
    """Example: Create complete AI persona"""
    config = GretaConfig()
    controller = GretaAIController(config)
    
    if await controller.initialize():
        # Create a virtual teacher persona
        persona_config = {
            "name": "VirtualTeacher",
            "personality": {
                "empathy": 0.9,
                "enthusiasm": 0.8,
                "patience": 0.9,
                "expertise": 0.8
            },
            "voice": "teacher_voice",
            "gestures": "educational",
            "emotion_sensitivity": 0.8,
            "cultural_context": "educational",
            "mode": "teaching"
        }
        
        print("🎓 Creating Virtual Teacher persona...")
        persona_result = await controller.create_ai_persona(persona_config)
        
        if persona_result.get("success"):
            print(f"✅ Persona created: {persona_result.get('persona_id')}")
            
            # Test conversation with the persona
            response = await controller.chat_with_llm(
                "I'm having trouble understanding calculus. Can you help me?",
                personality="teacher"
            )
            
            print(f"Teacher response: {response['text']}")
        else:
            print(f"❌ Persona creation failed: {persona_result.get('error')}")
    
    await controller.shutdown()

# =============================================================================
# 🚀 Main Entry Point
# =============================================================================

async def main():
    """Main function demonstrating all capabilities"""
    print("🤖 Greta AI Controller - Comprehensive Demo")
    print("=" * 50)
    
    # Choose which example to run
    examples = {
        "1": ("Basic Chat", example_basic_chat),
        "2": ("Emotion Analysis", example_emotion_analysis),
        "3": ("Multimodal Scene", example_multimodal_scene),
        "4": ("Gesture Generation", example_gesture_generation),
        "5": ("Complete AI Persona", example_complete_ai_persona)
    }
    
    print("\nAvailable examples:")
    for key, (name, _) in examples.items():
        print(f"{key}. {name}")
    
    choice = input("\nSelect example (1-5): ").strip()
    
    if choice in examples:
        name, example_func = examples[choice]
        print(f"\n🚀 Running {name} example...")
        await example_func()
    else:
        print("❌ Invalid choice")

if __name__ == "__main__":
    asyncio.run(main())