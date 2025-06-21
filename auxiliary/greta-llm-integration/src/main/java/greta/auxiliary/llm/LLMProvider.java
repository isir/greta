package greta.auxiliary.llm;

import greta.auxiliary.llm.model.ConversationContext;
import greta.auxiliary.llm.model.LLMRequest;
import greta.auxiliary.llm.model.LLMResponse;
import greta.auxiliary.llm.model.PersonalityProfile;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for Large Language Model providers
 * Supports multiple LLM backends for natural conversation generation
 */
public interface LLMProvider {
    
    /**
     * Get the provider name
     * @return provider identifier
     */
    String getProviderName();
    
    /**
     * Check if the provider is available and configured
     * @return true if ready to use
     */
    boolean isAvailable();
    
    /**
     * Generate a conversational response
     * @param request the LLM request with context and parameters
     * @return future containing the generated response
     */
    CompletableFuture<LLMResponse> generateResponse(LLMRequest request);
    
    /**
     * Generate a response with specific personality traits
     * @param request the LLM request
     * @param personality the personality profile to apply
     * @return future containing the personality-aware response
     */
    CompletableFuture<LLMResponse> generatePersonalizedResponse(
        LLMRequest request, 
        PersonalityProfile personality
    );
    
    /**
     * Stream a response for real-time interaction
     * @param request the LLM request
     * @param callback callback for streaming tokens
     * @return future that completes when streaming is done
     */
    CompletableFuture<Void> streamResponse(
        LLMRequest request,
        StreamingCallback callback
    );
    
    /**
     * Generate multiple response variations
     * @param request the LLM request
     * @param count number of variations to generate
     * @return future containing multiple response options
     */
    CompletableFuture<LLMResponse[]> generateVariations(
        LLMRequest request, 
        int count
    );
    
    /**
     * Estimate the cost of a request
     * @param request the LLM request
     * @return estimated cost in cents
     */
    double estimateCost(LLMRequest request);
    
    /**
     * Get usage statistics
     * @return provider usage stats
     */
    UsageStats getUsageStats();
    
    /**
     * Shutdown the provider and cleanup resources
     */
    void shutdown();
    
    /**
     * Callback interface for streaming responses
     */
    interface StreamingCallback {
        void onToken(String token);
        void onComplete(LLMResponse finalResponse);
        void onError(Throwable error);
    }
    
    /**
     * Usage statistics for monitoring
     */
    interface UsageStats {
        long getTotalRequests();
        long getSuccessfulRequests();
        long getFailedRequests();
        double getAverageResponseTime();
        double getTotalCost();
        long getTokensConsumed();
    }
}