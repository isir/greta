package greta.auxiliary.multimodal;

import greta.auxiliary.multimodal.model.*;
import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.CompletableFuture;

/**
 * Advanced multimodal AI engine that combines vision, speech, gesture, and language understanding
 * for comprehensive scene understanding and natural interaction
 */
public interface MultimodalAIEngine {
    
    /**
     * Initialize the multimodal AI engine
     * @param config configuration for the engine
     * @return future that completes when initialization is done
     */
    CompletableFuture<Boolean> initialize(MultimodalConfig config);
    
    /**
     * Analyze a complete multimodal scene
     * @param sceneInput combined input from all modalities
     * @return future containing comprehensive scene understanding
     */
    CompletableFuture<SceneUnderstanding> analyzeScene(MultimodalSceneInput sceneInput);
    
    /**
     * Understand visual content with natural language
     * @param visionInput visual data (images, video)
     * @param languageQuery natural language query about the visual content
     * @return future containing vision-language understanding
     */
    CompletableFuture<VisionLanguageResult> understandVisionWithLanguage(
        VisionInput visionInput, 
        String languageQuery
    );
    
    /**
     * Recognize and understand gestures in context
     * @param gestureInput gesture data (3D pose, hand tracking, etc.)
     * @param contextInput surrounding context (speech, scene, etc.)
     * @return future containing gesture understanding
     */
    CompletableFuture<GestureUnderstanding> understandGesture(
        GestureInput gestureInput,
        ContextualInput contextInput
    );
    
    /**
     * Generate appropriate responses across multiple modalities
     * @param userInput multimodal user input
     * @param responseIntent intended response type and goal
     * @return future containing multimodal response plan
     */
    CompletableFuture<MultimodalResponse> generateMultimodalResponse(
        MultimodalInput userInput,
        ResponseIntent responseIntent
    );
    
    /**
     * Start real-time multimodal interaction
     * @param streamConfig configuration for input streams
     * @return observable stream of interaction updates
     */
    Observable<InteractionUpdate> startRealTimeInteraction(RealTimeStreamConfig streamConfig);
    
    /**
     * Stop real-time interaction
     */
    void stopRealTimeInteraction();
    
    /**
     * Understand spatial relationships and 3D scene
     * @param spatialInput 3D spatial data
     * @return future containing spatial understanding
     */
    CompletableFuture<SpatialUnderstanding> understandSpatialScene(SpatialInput spatialInput);
    
    /**
     * Predict user intentions from multimodal cues
     * @param observationWindow recent multimodal observations
     * @return future containing intention predictions
     */
    CompletableFuture<IntentionPrediction> predictUserIntentions(
        MultimodalObservationWindow observationWindow
    );
    
    /**
     * Adapt behavior based on multimodal feedback
     * @param feedbackInput user feedback across modalities
     * @return future containing adaptation plan
     */
    CompletableFuture<AdaptationPlan> adaptToFeedback(MultimodalFeedback feedbackInput);
    
    /**
     * Get current understanding of the interaction context
     * @return current contextual understanding
     */
    ContextualUnderstanding getCurrentContext();
    
    /**
     * Get multimodal AI capabilities
     * @return supported features and limitations
     */
    MultimodalCapabilities getCapabilities();
    
    /**
     * Get performance metrics
     * @return multimodal AI performance statistics
     */
    PerformanceMetrics getPerformanceMetrics();
    
    /**
     * Configure the multimodal AI engine
     * @param config new configuration
     */
    void configure(MultimodalConfig config);
    
    /**
     * Shutdown the multimodal AI engine
     */
    void shutdown();
    
    /**
     * Configuration for multimodal AI engine
     */
    interface MultimodalConfig {
        boolean isVisionEnabled();
        boolean isSpeechEnabled();
        boolean isGestureEnabled();
        boolean isEmotionEnabled();
        boolean isSpatialEnabled();
        String getVisionModel();        // GPT-4V, CLIP, custom
        String getLanguageModel();      // GPT-4, Claude, custom
        String getGestureModel();       // MediaPipe, custom
        double getConfidenceThreshold();
        int getMaxProcessingTimeMs();
        boolean shouldFuseModalities();
        FusionStrategy getFusionStrategy();
    }
    
    /**
     * Strategy for fusing multimodal information
     */
    enum FusionStrategy {
        EARLY_FUSION,      // Combine raw features
        LATE_FUSION,       // Combine decision outputs
        HIERARCHICAL,      // Multi-stage fusion
        ATTENTION_BASED,   // Attention-weighted fusion
        DYNAMIC           // Context-dependent fusion
    }
    
    /**
     * Real-time streaming configuration
     */
    interface RealTimeStreamConfig {
        boolean isVideoStreamEnabled();
        boolean isAudioStreamEnabled();
        boolean isGestureStreamEnabled();
        int getVideoFps();
        int getAudioSampleRate();
        int getGestureTrackingFps();
        int getProcessingIntervalMs();
        boolean shouldBufferInputs();
        int getBufferSizeMs();
    }
    
    /**
     * Capabilities of the multimodal AI system
     */
    interface MultimodalCapabilities {
        String[] getSupportedVisionTasks();
        String[] getSupportedGestures();
        String[] getSupportedLanguages();
        String[] getSupportedEmotions();
        boolean supportsRealTimeProcessing();
        boolean supports3DUnderstanding();
        boolean supportsIntentionPrediction();
        boolean supportsAdaptiveBehavior();
        double getMaxVideoResolution();
        int getMaxAudioChannels();
        int getMaxGestureTrackingPoints();
    }
    
    /**
     * Performance metrics for monitoring
     */
    interface PerformanceMetrics {
        long getTotalSceneAnalyses();
        long getSuccessfulAnalyses();
        long getFailedAnalyses();
        double getAverageProcessingTime();
        double getVisionProcessingTime();
        double getSpeechProcessingTime();
        double getGestureProcessingTime();
        double getFusionProcessingTime();
        double getAverageConfidence();
        long getMemoryUsage();
        double getCpuUtilization();
        double getGpuUtilization();
        int getActiveStreams();
        double getStreamingLatency();
    }
}