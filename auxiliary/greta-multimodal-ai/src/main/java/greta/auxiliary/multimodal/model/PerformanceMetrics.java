package greta.auxiliary.multimodal.model;

/**
 * Performance metrics interface for multimodal AI
 */
public interface PerformanceMetrics {
    
    double getAverageLatency();
    
    double getThroughput();
    
    long getTotalProcessed();
    
    double getErrorRate();
}