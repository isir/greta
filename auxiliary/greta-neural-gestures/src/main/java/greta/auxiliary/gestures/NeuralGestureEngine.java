package greta.auxiliary.gestures;

import greta.auxiliary.gestures.model.*;
import java.util.concurrent.CompletableFuture;

/**
 * Neural gesture synthesis engine using motion diffusion models
 * Generates natural, contextually appropriate gestures from high-level descriptions
 */
public interface NeuralGestureEngine {
    
    /**
     * Initialize the neural gesture engine
     * @param config configuration for the engine
     * @return future that completes when initialization is done
     */
    CompletableFuture<Boolean> initialize(NeuralGestureConfig config);
    
    /**
     * Generate gestures from text description
     * @param textInput text describing the intended gesture or speech
     * @param gestureContext context for gesture generation
     * @return future containing generated gesture sequence
     */
    CompletableFuture<GestureSequence> generateFromText(
        String textInput, 
        GestureGenerationContext gestureContext
    );
    
    /**
     * Generate gestures from semantic intention
     * @param intention high-level communicative intention
     * @param context context and constraints for generation
     * @return future containing generated gesture sequence
     */
    CompletableFuture<GestureSequence> generateFromIntention(
        CommunicativeIntention intention,
        GestureGenerationContext context
    );
    
    /**
     * Generate co-speech gestures synchronized with speech
     * @param speechAudio audio of the speech
     * @param speechText transcript of the speech
     * @param context generation context
     * @return future containing speech-synchronized gestures
     */
    CompletableFuture<GestureSequence> generateCoSpeechGestures(
        byte[] speechAudio,
        String speechText,
        GestureGenerationContext context
    );
    
    /**
     * Refine and improve existing gesture sequence
     * @param existingGesture gesture sequence to refine
     * @param refinementCriteria criteria for improvement
     * @return future containing refined gesture sequence
     */
    CompletableFuture<GestureSequence> refineGesture(
        GestureSequence existingGesture,
        RefinementCriteria refinementCriteria
    );
    
    /**
     * Generate gesture variations for the same input
     * @param baseRequest base gesture generation request
     * @param variationCount number of variations to generate
     * @return future containing multiple gesture variations
     */
    CompletableFuture<GestureSequence[]> generateVariations(
        GestureGenerationRequest baseRequest,
        int variationCount
    );
    
    /**
     * Blend multiple gestures smoothly
     * @param gestures array of gesture sequences to blend
     * @param blendingStrategy strategy for blending
     * @return future containing blended gesture sequence
     */
    CompletableFuture<GestureSequence> blendGestures(
        GestureSequence[] gestures,
        BlendingStrategy blendingStrategy
    );
    
    /**
     * Generate gesture from motion capture data
     * @param mocapData motion capture reference data
     * @param adaptationParams parameters for adapting to target character
     * @return future containing adapted gesture sequence
     */
    CompletableFuture<GestureSequence> generateFromMocap(
        MotionCaptureData mocapData,
        MotionAdaptationParams adaptationParams
    );
    
    /**
     * Generate real-time gesture stream
     * @param streamInput streaming input configuration
     * @param callback callback for receiving generated gestures
     * @return future that completes when streaming ends
     */
    CompletableFuture<Void> generateRealTimeStream(
        RealTimeGestureInput streamInput,
        GestureStreamCallback callback
    );
    
    /**
     * Evaluate gesture quality and naturalness
     * @param gesture gesture sequence to evaluate
     * @return future containing quality assessment
     */
    CompletableFuture<GestureQualityAssessment> evaluateGesture(GestureSequence gesture);
    
    /**
     * Get available gesture styles and categories
     * @return supported gesture capabilities
     */
    GestureCapabilities getCapabilities();
    
    /**
     * Get performance metrics
     * @return neural gesture engine performance statistics
     */
    PerformanceMetrics getPerformanceMetrics();
    
    /**
     * Configure the neural gesture engine
     * @param config new configuration
     */
    void configure(NeuralGestureConfig config);
    
    /**
     * Shutdown the neural gesture engine
     */
    void shutdown();
    
    /**
     * Callback interface for real-time gesture streaming
     */
    interface GestureStreamCallback {
        void onGestureFrame(GestureFrame frame);
        void onGestureSequence(GestureSequence sequence);
        void onError(Throwable error);
        void onComplete();
    }
    
    /**
     * Configuration for neural gesture engine
     */
    interface NeuralGestureConfig {
        String getModelType();           // diffusion, transformer, vae
        String getModelPath();
        boolean useGpu();
        int getMaxSequenceLength();
        double getQualityThreshold();
        boolean enableRealTimeGeneration();
        int getRealTimeFrameRate();
        String getCharacterRig();        // target character skeleton
        boolean enablePhysicsConstraints();
        boolean enableCollisionDetection();
    }
    
    /**
     * Blending strategy for multiple gestures
     */
    enum BlendingStrategy {
        LINEAR,              // Linear interpolation
        SPHERICAL,           // Spherical linear interpolation
        WEIGHTED_AVERAGE,    // Weighted averaging
        TEMPORAL_ALIGNMENT,  // Time-aligned blending
        SEMANTIC_AWARE,      // Semantically-aware blending
        PHYSICS_BASED       // Physics-constrained blending
    }
    
    /**
     * Capabilities of the neural gesture system
     */
    interface GestureCapabilities {
        String[] getSupportedGestureTypes();
        String[] getSupportedEmotions();
        String[] getSupportedCulturalStyles();
        String[] getSupportedCharacterRigs();
        boolean supportsRealTimeGeneration();
        boolean supportsCoSpeechGeneration();
        boolean supportsMotionAdaptation();
        boolean supportsStyleTransfer();
        double getMaxGenerationLength();
        int getMaxJointCount();
        double getMinQualityScore();
    }
    
    /**
     * Performance metrics for monitoring
     */
    interface PerformanceMetrics {
        long getTotalGenerations();
        long getSuccessfulGenerations();
        long getFailedGenerations();
        double getAverageGenerationTime();
        double getAverageQualityScore();
        double getRealTimePerformance();  // frames per second
        long getMemoryUsage();
        double getCpuUtilization();
        double getGpuUtilization();
        int getActiveStreams();
    }
}