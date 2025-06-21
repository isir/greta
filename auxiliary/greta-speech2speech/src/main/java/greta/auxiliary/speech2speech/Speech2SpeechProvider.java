package greta.auxiliary.speech2speech;

import greta.auxiliary.speech2speech.model.AudioSegment;
import greta.auxiliary.speech2speech.model.Speech2SpeechRequest;
import greta.auxiliary.speech2speech.model.Speech2SpeechResponse;
import greta.auxiliary.speech2speech.model.VoiceProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for Speech-to-Speech foundation model providers
 * Supports direct speech conversion without intermediate text
 */
public interface Speech2SpeechProvider {
    
    /**
     * Get the provider name
     * @return provider identifier
     */
    String getProviderName();
    
    /**
     * Check if the provider is available and models are loaded
     * @return true if ready to use
     */
    boolean isAvailable();
    
    /**
     * Convert speech to speech using foundation models
     * @param request the speech conversion request
     * @return future containing the converted speech
     */
    CompletableFuture<Speech2SpeechResponse> convertSpeech(Speech2SpeechRequest request);
    
    /**
     * Convert speech with specific voice characteristics
     * @param request the conversion request
     * @param targetVoice the target voice profile
     * @return future containing voice-converted speech
     */
    CompletableFuture<Speech2SpeechResponse> convertWithVoice(
        Speech2SpeechRequest request, 
        VoiceProfile targetVoice
    );
    
    /**
     * Stream speech conversion for real-time processing
     * @param request the conversion request
     * @param callback callback for streaming audio chunks
     * @return future that completes when streaming is done
     */
    CompletableFuture<Void> streamConversion(
        Speech2SpeechRequest request,
        StreamingAudioCallback callback
    );
    
    /**
     * Clone a voice from reference audio samples
     * @param referenceAudio audio samples of the target voice
     * @param speakerName identifier for the cloned voice
     * @return future containing the voice profile
     */
    CompletableFuture<VoiceProfile> cloneVoice(
        AudioSegment[] referenceAudio,
        String speakerName
    );
    
    /**
     * Generate expressive speech with emotion and style control
     * @param request the conversion request
     * @param emotion target emotion (happy, sad, angry, neutral, etc.)
     * @param style speaking style (casual, formal, energetic, calm, etc.)
     * @return future containing expressive speech
     */
    CompletableFuture<Speech2SpeechResponse> generateExpressiveSpeech(
        Speech2SpeechRequest request,
        String emotion,
        String style
    );
    
    /**
     * Enhance speech quality (denoising, upsampling, etc.)
     * @param audioSegment input audio to enhance
     * @return future containing enhanced audio
     */
    CompletableFuture<AudioSegment> enhanceAudio(AudioSegment audioSegment);
    
    /**
     * Get available voices for this provider
     * @return list of available voice profiles
     */
    VoiceProfile[] getAvailableVoices();
    
    /**
     * Get supported emotions and styles
     * @return supported expression parameters
     */
    ExpressionCapabilities getExpressionCapabilities();
    
    /**
     * Get performance metrics
     * @return provider performance stats
     */
    PerformanceStats getPerformanceStats();
    
    /**
     * Shutdown the provider and cleanup resources
     */
    void shutdown();
    
    /**
     * Callback interface for streaming audio
     */
    interface StreamingAudioCallback {
        void onAudioChunk(byte[] audioData, int sampleRate);
        void onComplete(Speech2SpeechResponse finalResponse);
        void onError(Throwable error);
    }
    
    /**
     * Expression capabilities of the provider
     */
    interface ExpressionCapabilities {
        String[] getSupportedEmotions();
        String[] getSupportedStyles();
        boolean supportsEmotionIntensity();
        boolean supportsStyleMixing();
        boolean supportsRealTimeControl();
    }
    
    /**
     * Performance statistics for monitoring
     */
    interface PerformanceStats {
        long getTotalConversions();
        long getSuccessfulConversions();
        long getFailedConversions();
        double getAverageProcessingTime();
        double getRealTimeFactor(); // processing_time / audio_duration
        long getMemoryUsage();
        double getGpuUtilization();
    }
}