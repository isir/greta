package greta.auxiliary.emotion;

import greta.auxiliary.emotion.model.*;
import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.CompletableFuture;

/**
 * Main engine for multimodal emotion recognition
 * Combines voice, facial, and physiological signals for comprehensive emotion analysis
 */
public interface EmotionRecognitionEngine {
    
    /**
     * Initialize the emotion recognition engine
     * @return future that completes when initialization is done
     */
    CompletableFuture<Boolean> initialize();
    
    /**
     * Analyze emotion from audio input
     * @param audioInput audio data for analysis
     * @return future containing voice emotion analysis
     */
    CompletableFuture<VoiceEmotionResult> analyzeVoiceEmotion(AudioInput audioInput);
    
    /**
     * Analyze emotion from facial expression
     * @param faceInput facial image or video data
     * @return future containing facial emotion analysis
     */
    CompletableFuture<FacialEmotionResult> analyzeFacialEmotion(FaceInput faceInput);
    
    /**
     * Analyze emotion from physiological signals
     * @param physioInput physiological data (heart rate, GSR, etc.)
     * @return future containing physiological emotion analysis
     */
    CompletableFuture<PhysiologicalEmotionResult> analyzePhysiologicalEmotion(PhysiologicalInput physioInput);
    
    /**
     * Perform multimodal emotion analysis combining all available inputs
     * @param multimodalInput combined input from multiple modalities
     * @return future containing fused emotion analysis
     */
    CompletableFuture<MultimodalEmotionResult> analyzeMultimodalEmotion(MultimodalInput multimodalInput);
    
    /**
     * Start real-time emotion monitoring
     * @param inputStreams configuration for input streams
     * @return observable stream of emotion updates
     */
    Observable<EmotionUpdate> startRealTimeMonitoring(RealTimeConfig inputStreams);
    
    /**
     * Stop real-time emotion monitoring
     */
    void stopRealTimeMonitoring();
    
    /**
     * Get current emotion state
     * @return latest recognized emotion state
     */
    EmotionState getCurrentEmotionState();
    
    /**
     * Get emotion recognition capabilities
     * @return supported emotions, confidence levels, etc.
     */
    EmotionCapabilities getCapabilities();
    
    /**
     * Calibrate the system for a specific user
     * @param calibrationData user-specific calibration data
     * @return future that completes when calibration is done
     */
    CompletableFuture<Boolean> calibrateForUser(UserCalibrationData calibrationData);
    
    /**
     * Get performance metrics
     * @return emotion recognition performance statistics
     */
    PerformanceMetrics getPerformanceMetrics();
    
    /**
     * Configure emotion recognition parameters
     * @param config configuration parameters
     */
    void configure(EmotionRecognitionConfig config);
    
    /**
     * Shutdown the emotion recognition engine
     */
    void shutdown();
    
    /**
     * Real-time configuration for emotion monitoring
     */
    interface RealTimeConfig {
        boolean isVoiceEnabled();
        boolean isFacialEnabled();
        boolean isPhysiologicalEnabled();
        int getUpdateIntervalMs();
        double getConfidenceThreshold();
        boolean shouldFuseModalities();
    }
    
    /**
     * Capabilities of the emotion recognition system
     */
    interface EmotionCapabilities {
        String[] getSupportedEmotions();
        String[] getSupportedModalities();
        double getMinConfidenceLevel();
        double getMaxConfidenceLevel();
        boolean supportsRealTimeProcessing();
        boolean supportsUserCalibration();
        boolean supportsEmotionHistory();
        int getMaxHistoryLength();
    }
    
    /**
     * Performance metrics for monitoring system health
     */
    interface PerformanceMetrics {
        long getTotalAnalyses();
        long getSuccessfulAnalyses();
        long getFailedAnalyses();
        double getAverageProcessingTime();
        double getAverageConfidence();
        double getVoiceAccuracy();
        double getFacialAccuracy();
        double getMultimodalAccuracy();
        long getMemoryUsage();
        double getCpuUtilization();
        double getGpuUtilization();
    }
}