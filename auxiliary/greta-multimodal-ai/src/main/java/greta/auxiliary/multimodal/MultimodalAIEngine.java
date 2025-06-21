package greta.auxiliary.multimodal;

import greta.auxiliary.multimodal.model.*;
import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.CompletableFuture;

/**
 * Simplified multimodal AI engine interface
 */
public interface MultimodalAIEngine {
    
    /**
     * Initialize the multimodal AI engine
     */
    void initialize(MultimodalConfig config);
    
    /**
     * Analyze a scene from multimodal input
     */
    CompletableFuture<SceneUnderstanding> analyzeScene(MultimodalInput input);
    
    /**
     * Process an interaction request
     */
    CompletableFuture<InteractionResponse> processInteraction(InteractionRequest request);
    
    /**
     * Start real-time interaction
     */
    Observable<InteractionUpdate> startRealTimeInteraction(RealTimeConfig config);
    
    /**
     * Stop real-time interaction
     */
    void stopRealTimeInteraction();
    
    /**
     * Get current context
     */
    ContextualUnderstanding getCurrentContext();
    
    /**
     * Get capabilities
     */
    MultimodalCapabilities getCapabilities();
    
    /**
     * Get performance metrics
     */
    PerformanceMetrics getPerformanceMetrics();
    
    /**
     * Configure the engine
     */
    void configure(MultimodalConfig config);
    
    /**
     * Shutdown the engine
     */
    void shutdown();
}