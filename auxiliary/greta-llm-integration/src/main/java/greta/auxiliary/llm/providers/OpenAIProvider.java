package greta.auxiliary.llm.providers;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import greta.auxiliary.llm.LLMProvider;
import greta.auxiliary.llm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * OpenAI GPT provider for LLM integration
 */
public class OpenAIProvider implements LLMProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);
    
    private final OpenAiService service;
    private final UsageStatsImpl usageStats;
    private final String defaultModel;
    
    public OpenAIProvider(String apiKey) {
        this(apiKey, "gpt-4", Duration.ofSeconds(60));
    }
    
    public OpenAIProvider(String apiKey, String defaultModel, Duration timeout) {
        this.service = new OpenAiService(apiKey, timeout);
        this.usageStats = new UsageStatsImpl();
        this.defaultModel = defaultModel;
    }
    
    @Override
    public String getProviderName() {
        return "OpenAI";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Test with a simple request
            service.listModels();
            return true;
        } catch (Exception e) {
            logger.warn("OpenAI service not available: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public CompletableFuture<LLMResponse> generateResponse(LLMRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            usageStats.incrementRequests();
            
            try {
                List<ChatMessage> messages = buildMessages(request);
                
                ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(request.getParameters().getModel() != null ? 
                           request.getParameters().getModel() : defaultModel)
                    .messages(messages)
                    .maxTokens(request.getParameters().getMaxTokens())
                    .temperature(request.getParameters().getTemperature())
                    .topP(request.getParameters().getTopP())
                    .frequencyPenalty(request.getParameters().getFrequencyPenalty())
                    .presencePenalty(request.getParameters().getPresencePenalty())
                    .stop(request.getParameters().getStopSequences())
                    .build();
                
                ChatCompletionResult result = service.createChatCompletion(completionRequest);
                
                LLMResponse response = new LLMResponse();
                response.setText(result.getChoices().get(0).getMessage().getContent());
                response.setRequestId(result.getId());
                response.setModel(result.getModel());
                response.setResponseTimeMs(System.currentTimeMillis() - startTime);
                
                // Set usage information
                if (result.getUsage() != null) {
                    LLMResponse.UsageInfo usage = new LLMResponse.UsageInfo();
                    usage.setPromptTokens((int) result.getUsage().getPromptTokens());
                    usage.setCompletionTokens((int) result.getUsage().getCompletionTokens());
                    usage.setTotalTokens((int) result.getUsage().getTotalTokens());
                    usage.setCost(calculateCost(result.getModel(), (int) result.getUsage().getTotalTokens()));
                    response.setUsage(usage);
                    
                    usageStats.addTokens((int) result.getUsage().getTotalTokens());
                    usageStats.addCost(usage.getCost());
                }
                
                // Set confidence (simplified heuristic)
                response.setConfidence(calculateConfidence(result));
                
                usageStats.incrementSuccessful();
                usageStats.addResponseTime(response.getResponseTimeMs());
                
                return response;
                
            } catch (Exception e) {
                usageStats.incrementFailed();
                logger.error("Error generating OpenAI response", e);
                throw new RuntimeException("Failed to generate response from OpenAI", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<LLMResponse> generatePersonalizedResponse(
            LLMRequest request, PersonalityProfile personality) {
        
        // Create a new request with personality-aware system message
        LLMRequest personalizedRequest = new LLMRequest();
        personalizedRequest.setPrompt(request.getPrompt());
        personalizedRequest.setParameters(request.getParameters());
        personalizedRequest.setMetadata(request.getMetadata());
        
        // Create context with personality system prompt
        ConversationContext personalizedContext = request.getContext() != null ? 
            request.getContext() : new ConversationContext();
        
        // Add personality system message at the beginning
        List<ConversationContext.ConversationTurn> history = new ArrayList<>();
        history.add(new ConversationContext.ConversationTurn("system", personality.generateSystemPrompt()));
        
        if (personalizedContext.getHistory() != null) {
            history.addAll(personalizedContext.getHistory());
        }
        
        personalizedContext.setHistory(history);
        personalizedRequest.setContext(personalizedContext);
        
        return generateResponse(personalizedRequest);
    }
    
    @Override
    public CompletableFuture<Void> streamResponse(LLMRequest request, StreamingCallback callback) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<ChatMessage> messages = buildMessages(request);
                
                ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(request.getParameters().getModel() != null ? 
                           request.getParameters().getModel() : defaultModel)
                    .messages(messages)
                    .maxTokens(request.getParameters().getMaxTokens())
                    .temperature(request.getParameters().getTemperature())
                    .stream(true)
                    .build();
                
                StringBuilder fullResponse = new StringBuilder();
                
                service.streamChatCompletion(completionRequest)
                    .doOnNext(chunk -> {
                        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                            String token = chunk.getChoices().get(0).getMessage().getContent();
                            if (token != null) {
                                fullResponse.append(token);
                                callback.onToken(token);
                            }
                        }
                    })
                    .doOnComplete(() -> {
                        LLMResponse finalResponse = new LLMResponse(fullResponse.toString());
                        callback.onComplete(finalResponse);
                    })
                    .doOnError(callback::onError)
                    .blockingSubscribe();
                    
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<LLMResponse[]> generateVariations(LLMRequest request, int count) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ChatMessage> messages = buildMessages(request);
                
                ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(request.getParameters().getModel() != null ? 
                           request.getParameters().getModel() : defaultModel)
                    .messages(messages)
                    .maxTokens(request.getParameters().getMaxTokens())
                    .temperature(request.getParameters().getTemperature())
                    .n(count)
                    .build();
                
                ChatCompletionResult result = service.createChatCompletion(completionRequest);
                
                LLMResponse[] responses = new LLMResponse[result.getChoices().size()];
                
                for (int i = 0; i < result.getChoices().size(); i++) {
                    responses[i] = new LLMResponse();
                    responses[i].setText(result.getChoices().get(i).getMessage().getContent());
                    responses[i].setRequestId(result.getId());
                    responses[i].setModel(result.getModel());
                }
                
                return responses;
                
            } catch (Exception e) {
                logger.error("Error generating OpenAI variations", e);
                throw new RuntimeException("Failed to generate variations from OpenAI", e);
            }
        });
    }
    
    @Override
    public double estimateCost(LLMRequest request) {
        // Rough token estimation
        int estimatedTokens = estimateTokenCount(request.getPrompt()) + 
                              request.getParameters().getMaxTokens();
        return calculateCost(request.getParameters().getModel(), estimatedTokens);
    }
    
    @Override
    public UsageStats getUsageStats() {
        return usageStats;
    }
    
    @Override
    public void shutdown() {
        // OpenAI service doesn't require explicit shutdown
        logger.info("OpenAI provider shut down");
    }
    
    private List<ChatMessage> buildMessages(LLMRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // Add conversation history
        if (request.getContext() != null && request.getContext().getHistory() != null) {
            for (ConversationContext.ConversationTurn turn : request.getContext().getHistory()) {
                ChatMessageRole role;
                switch (turn.getRole().toLowerCase()) {
                    case "user": role = ChatMessageRole.USER; break;
                    case "assistant": role = ChatMessageRole.ASSISTANT; break;
                    case "system": role = ChatMessageRole.SYSTEM; break;
                    default: role = ChatMessageRole.USER; break;
                }
                messages.add(new ChatMessage(role.value(), turn.getContent()));
            }
        }
        
        // Add current prompt
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), request.getPrompt()));
        
        return messages;
    }
    
    private double calculateCost(String model, int tokens) {
        // Simplified cost calculation - update with current OpenAI pricing
        double costPerThousandTokens;
        switch (model != null ? model.toLowerCase() : defaultModel.toLowerCase()) {
            case "gpt-4":
                costPerThousandTokens = 0.03; // $0.03 per 1K tokens
                break;
            case "gpt-3.5-turbo":
                costPerThousandTokens = 0.002; // $0.002 per 1K tokens
                break;
            default:
                costPerThousandTokens = 0.02;
                break;
        }
        return (tokens / 1000.0) * costPerThousandTokens;
    }
    
    private double calculateConfidence(ChatCompletionResult result) {
        // Simplified confidence calculation
        // In practice, you might use logprobs or other metrics
        return 0.8; // Default confidence
    }
    
    private int estimateTokenCount(String text) {
        // Rough estimation: ~4 characters per token for English
        return text.length() / 4;
    }
    
    private static class UsageStatsImpl implements UsageStats {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicReference<Double> totalResponseTime = new AtomicReference<>(0.0);
        private final AtomicReference<Double> totalCost = new AtomicReference<>(0.0);
        private final AtomicLong tokensConsumed = new AtomicLong(0);
        
        void incrementRequests() { totalRequests.incrementAndGet(); }
        void incrementSuccessful() { successfulRequests.incrementAndGet(); }
        void incrementFailed() { failedRequests.incrementAndGet(); }
        void addResponseTime(long responseTime) { 
            totalResponseTime.updateAndGet(current -> current + responseTime);
        }
        void addCost(double cost) {
            totalCost.updateAndGet(current -> current + cost);
        }
        void addTokens(long tokens) { tokensConsumed.addAndGet(tokens); }
        
        @Override
        public long getTotalRequests() { return totalRequests.get(); }
        
        @Override
        public long getSuccessfulRequests() { return successfulRequests.get(); }
        
        @Override
        public long getFailedRequests() { return failedRequests.get(); }
        
        @Override
        public double getAverageResponseTime() {
            long total = getTotalRequests();
            return total > 0 ? totalResponseTime.get() / total : 0.0;
        }
        
        @Override
        public double getTotalCost() { return totalCost.get(); }
        
        @Override
        public long getTokensConsumed() { return tokensConsumed.get(); }
    }
}