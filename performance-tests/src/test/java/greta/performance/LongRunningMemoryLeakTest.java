package greta.performance;

import org.junit.jupiter.api.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.*;

/**
 * Long-running memory leak detection tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LongRunningMemoryLeakTest {
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
    private static final int TEST_DURATION_HOURS = 2;
    private static final int SAMPLE_INTERVAL_SECONDS = 30;
    
    private List<MemorySnapshot> memorySnapshots = new ArrayList<>();
    private ExecutorService backgroundService;
    
    @BeforeEach
    public void setup() {
        backgroundService = Executors.newFixedThreadPool(4);
        memorySnapshots.clear();
        // Force GC before starting
        System.gc();
        System.gc();
        Thread.yield();
    }
    
    @AfterEach
    public void cleanup() {
        if (backgroundService != null) {
            backgroundService.shutdown();
            try {
                if (!backgroundService.awaitTermination(10, TimeUnit.SECONDS)) {
                    backgroundService.shutdownNow();
                }
            } catch (InterruptedException e) {
                backgroundService.shutdownNow();
            }
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Detect memory leaks in animation processing")
    @Timeout(value = TEST_DURATION_HOURS + 1, unit = TimeUnit.HOURS)
    public void testAnimationProcessingMemoryLeaks() throws InterruptedException {
        System.out.println("Starting " + TEST_DURATION_HOURS + "-hour animation processing memory leak test");
        
        // Baseline measurement
        takeMemorySnapshot("baseline");
        
        long endTime = System.currentTimeMillis() + (TEST_DURATION_HOURS * 60 * 60 * 1000);
        long lastSampleTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() < endTime) {
            // Simulate continuous animation processing
            simulateAnimationProcessing();
            
            // Take memory sample every interval
            if (System.currentTimeMillis() - lastSampleTime > SAMPLE_INTERVAL_SECONDS * 1000) {
                takeMemorySnapshot("sample_" + memorySnapshots.size());
                lastSampleTime = System.currentTimeMillis();
                
                // Early detection of obvious leaks
                if (memorySnapshots.size() > 10) {
                    analyzeMemoryTrend();
                }
            }
            
            Thread.sleep(1000); // 1 second between operations
        }
        
        // Final analysis
        MemoryLeakAnalysisResult result = performMemoryLeakAnalysis();
        generateMemoryLeakReport(result);
        
        Assertions.assertFalse(result.hasSignificantLeak(), 
            "Memory leak detected in animation processing: " + result.getLeakDescription());
    }
    
    @Test
    @Order(2)
    @DisplayName("Detect memory leaks in behavior planning")
    @Timeout(value = 1, unit = TimeUnit.HOURS)
    public void testBehaviorPlanningMemoryLeaks() throws InterruptedException {
        System.out.println("Starting behavior planning memory leak test");
        
        takeMemorySnapshot("behavior_baseline");
        
        for (int i = 0; i < 1000; i++) {
            simulateBehaviorPlanning();
            
            if (i % 50 == 0) {
                takeMemorySnapshot("behavior_sample_" + i);
                System.gc(); // Force GC periodically
                Thread.sleep(100);
            }
            
            Thread.sleep(50);
        }
        
        MemoryLeakAnalysisResult result = performMemoryLeakAnalysis();
        Assertions.assertFalse(result.hasSignificantLeak(), 
            "Memory leak detected in behavior planning");
    }
    
    @Test
    @Order(3)
    @DisplayName("Detect memory leaks in cache operations")
    public void testCacheMemoryLeaks() throws InterruptedException {
        System.out.println("Starting cache operations memory leak test");
        
        AnimationCache cache = new AnimationCache(1000);
        takeMemorySnapshot("cache_baseline");
        
        // Fill and empty cache multiple times
        for (int cycle = 0; cycle < 10; cycle++) {
            // Fill cache
            for (int i = 0; i < 1500; i++) { // Overfill to trigger eviction
                cache.put("key_" + i, new CachedAnimation("anim_" + i, new byte[1024]));
            }
            
            takeMemorySnapshot("cache_filled_" + cycle);
            
            // Clear cache
            cache.clear();
            System.gc();
            Thread.sleep(100);
            
            takeMemorySnapshot("cache_cleared_" + cycle);
        }
        
        MemoryLeakAnalysisResult result = performMemoryLeakAnalysis();
        Assertions.assertFalse(result.hasSignificantLeak(), 
            "Memory leak detected in cache operations");
    }
    
    @Test
    @Order(4)
    @DisplayName("Detect memory leaks in concurrent operations")
    public void testConcurrentOperationsMemoryLeaks() throws InterruptedException {
        System.out.println("Starting concurrent operations memory leak test");
        
        takeMemorySnapshot("concurrent_baseline");
        
        List<Future<Void>> futures = new ArrayList<>();
        
        // Submit many concurrent tasks
        for (int i = 0; i < 100; i++) {
            Future<Void> future = backgroundService.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    simulateAnimationProcessing();
                    simulateBehaviorPlanning();
                    
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }
                return null;
            });
            futures.add(future);
        }
        
        // Monitor memory while tasks execute
        int sampleCount = 0;
        while (!allTasksComplete(futures) && sampleCount < 60) {
            Thread.sleep(5000);
            takeMemorySnapshot("concurrent_sample_" + sampleCount++);
        }
        
        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                future.cancel(true);
            }
        }
        
        System.gc();
        Thread.sleep(1000);
        takeMemorySnapshot("concurrent_final");
        
        MemoryLeakAnalysisResult result = performMemoryLeakAnalysis();
        Assertions.assertFalse(result.hasSignificantLeak(), 
            "Memory leak detected in concurrent operations");
    }
    
    private void simulateAnimationProcessing() {
        // Simulate animation frame processing
        List<AnimationFrame> frames = new ArrayList<>();
        for (int i = 0; i < 30; i++) { // 1 second at 30 FPS
            AnimationFrame frame = new AnimationFrame(i / 30.0);
            frame.addJoint("head", new Position(Math.random(), Math.random(), Math.random()));
            frame.addJoint("leftHand", new Position(Math.random(), Math.random(), Math.random()));
            frame.addJoint("rightHand", new Position(Math.random(), Math.random(), Math.random()));
            frames.add(frame);
        }
        
        // Process frames
        for (AnimationFrame frame : frames) {
            frame.interpolate(0.5);
        }
        
        // Clear references
        frames.clear();
    }
    
    private void simulateBehaviorPlanning() {
        BehaviorPlan plan = new BehaviorPlan();
        
        // Create complex behavior plan
        for (int i = 0; i < 20; i++) {
            Behavior behavior = new Behavior("behavior_" + i);
            behavior.setStartTime(Math.random() * 10);
            behavior.setDuration(Math.random() * 3 + 1);
            behavior.addParameter("intensity", Math.random());
            behavior.addParameter("emotion", "neutral");
            plan.addBehavior(behavior);
        }
        
        // Resolve conflicts multiple times
        for (int i = 0; i < 5; i++) {
            plan.resolveConflicts();
        }
        
        // Clear plan
        plan.clear();
    }
    
    private void takeMemorySnapshot(String label) {
        MemorySnapshot snapshot = new MemorySnapshot(label);
        snapshot.heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        snapshot.heapMax = memoryBean.getHeapMemoryUsage().getMax();
        snapshot.nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        snapshot.timestamp = System.currentTimeMillis();
        
        // Pool-specific memory usage
        for (MemoryPoolMXBean pool : memoryPools) {
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                snapshot.poolUsage.put(pool.getName(), usage.getUsed());
            }
        }
        
        memorySnapshots.add(snapshot);
        
        System.out.printf("[%s] Heap: %s, Non-Heap: %s%n", 
            label, formatBytes(snapshot.heapUsed), formatBytes(snapshot.nonHeapUsed));
    }
    
    private void analyzeMemoryTrend() {
        if (memorySnapshots.size() < 10) return;
        
        // Check last 10 samples for concerning trends
        List<MemorySnapshot> recent = memorySnapshots.subList(
            memorySnapshots.size() - 10, memorySnapshots.size());
        
        long firstHeap = recent.get(0).heapUsed;
        long lastHeap = recent.get(recent.size() - 1).heapUsed;
        double growthRate = (double)(lastHeap - firstHeap) / firstHeap;
        
        if (growthRate > 0.5) { // 50% growth in recent samples
            System.out.println("WARNING: Significant memory growth detected: " + 
                String.format("%.2f%%", growthRate * 100));
        }
    }
    
    private MemoryLeakAnalysisResult performMemoryLeakAnalysis() {
        MemoryLeakAnalysisResult result = new MemoryLeakAnalysisResult();
        
        if (memorySnapshots.size() < 3) {
            result.setInconclusive("Insufficient data for analysis");
            return result;
        }
        
        // Analyze heap memory trend
        long baselineHeap = memorySnapshots.get(0).heapUsed;
        long finalHeap = memorySnapshots.get(memorySnapshots.size() - 1).heapUsed;
        
        // Calculate growth rate
        double totalGrowthRate = (double)(finalHeap - baselineHeap) / baselineHeap;
        result.heapGrowthRate = totalGrowthRate;
        
        // Analyze per-pool trends
        for (String poolName : memorySnapshots.get(0).poolUsage.keySet()) {
            long baselinePool = memorySnapshots.get(0).poolUsage.get(poolName);
            long finalPool = memorySnapshots.get(memorySnapshots.size() - 1).poolUsage.get(poolName);
            
            if (baselinePool > 0) {
                double poolGrowth = (double)(finalPool - baselinePool) / baselinePool;
                result.poolGrowthRates.put(poolName, poolGrowth);
            }
        }
        
        // Determine if there's a significant leak
        result.hasLeak = totalGrowthRate > 0.3 || // 30% total growth
            result.poolGrowthRates.values().stream()
                .anyMatch(growth -> growth > 0.5); // 50% growth in any pool
        
        if (result.hasLeak) {
            result.leakDescription = String.format(
                "Heap growth: %.2f%%, Max pool growth: %.2f%%",
                totalGrowthRate * 100,
                result.poolGrowthRates.values().stream()
                    .mapToDouble(d -> d).max().orElse(0) * 100
            );
        }
        
        return result;
    }
    
    private void generateMemoryLeakReport(MemoryLeakAnalysisResult result) {
        System.out.println("\n=== MEMORY LEAK ANALYSIS REPORT ===");
        System.out.println("Test Duration: " + TEST_DURATION_HOURS + " hours");
        System.out.println("Total Samples: " + memorySnapshots.size());
        System.out.printf("Heap Growth Rate: %.2f%%%n", result.heapGrowthRate * 100);
        
        System.out.println("\nMemory Pool Growth Rates:");
        result.poolGrowthRates.forEach((pool, growth) -> 
            System.out.printf("  %s: %.2f%%%n", pool, growth * 100));
        
        if (result.hasLeak) {
            System.out.println("\n❌ MEMORY LEAK DETECTED: " + result.leakDescription);
        } else {
            System.out.println("\n✅ No significant memory leak detected");
        }
        
        System.out.println("\nMemory Usage Timeline:");
        for (int i = 0; i < memorySnapshots.size(); i += Math.max(1, memorySnapshots.size() / 10)) {
            MemorySnapshot snapshot = memorySnapshots.get(i);
            System.out.printf("  %s: %s heap, %s non-heap%n",
                snapshot.label, formatBytes(snapshot.heapUsed), formatBytes(snapshot.nonHeapUsed));
        }
    }
    
    private boolean allTasksComplete(List<Future<Void>> futures) {
        return futures.stream().allMatch(Future::isDone);
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
        return String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }
    
    // Helper classes
    static class MemorySnapshot {
        String label;
        long timestamp;
        long heapUsed;
        long heapMax;
        long nonHeapUsed;
        Map<String, Long> poolUsage = new HashMap<>();
        
        MemorySnapshot(String label) {
            this.label = label;
        }
    }
    
    static class MemoryLeakAnalysisResult {
        boolean hasLeak = false;
        double heapGrowthRate = 0.0;
        Map<String, Double> poolGrowthRates = new HashMap<>();
        String leakDescription = "";
        boolean inconclusive = false;
        String inconclusiveReason = "";
        
        boolean hasSignificantLeak() {
            return hasLeak && !inconclusive;
        }
        
        String getLeakDescription() {
            return inconclusive ? inconclusiveReason : leakDescription;
        }
        
        void setInconclusive(String reason) {
            this.inconclusive = true;
            this.inconclusiveReason = reason;
        }
    }
    
    // Mock classes for testing
    static class AnimationFrame {
        private final double timestamp;
        private final Map<String, Position> joints = new HashMap<>();
        
        public AnimationFrame(double timestamp) {
            this.timestamp = timestamp;
        }
        
        public void addJoint(String name, Position position) {
            joints.put(name, position);
        }
        
        public void interpolate(double factor) {
            // Simulate interpolation calculations
            joints.forEach((name, pos) -> {
                double newX = pos.x * factor;
                double newY = pos.y * factor;
                double newZ = pos.z * factor;
                // Results would normally be used
            });
        }
    }
    
    static class Position {
        final double x, y, z;
        
        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    static class BehaviorPlan {
        private final List<Behavior> behaviors = new ArrayList<>();
        
        public void addBehavior(Behavior behavior) {
            behaviors.add(behavior);
        }
        
        public void resolveConflicts() {
            // Simulate complex conflict resolution
            Collections.sort(behaviors, Comparator.comparingDouble(Behavior::getStartTime));
            
            for (int i = 0; i < behaviors.size() - 1; i++) {
                Behavior current = behaviors.get(i);
                Behavior next = behaviors.get(i + 1);
                
                if (current.getEndTime() > next.getStartTime()) {
                    // Resolve conflict by adjusting timing
                    next.setStartTime(current.getEndTime() + 0.1);
                }
            }
        }
        
        public void clear() {
            behaviors.clear();
        }
    }
    
    static class Behavior {
        private final String name;
        private double startTime;
        private double duration;
        private final Map<String, Object> parameters = new HashMap<>();
        
        public Behavior(String name) {
            this.name = name;
        }
        
        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }
        
        public double getStartTime() {
            return startTime;
        }
        
        public void setDuration(double duration) {
            this.duration = duration;
        }
        
        public double getEndTime() {
            return startTime + duration;
        }
        
        public void addParameter(String key, Object value) {
            parameters.put(key, value);
        }
    }
    
    static class AnimationCache {
        private final int maxSize;
        private final Map<String, CachedAnimation> cache = new ConcurrentHashMap<>();
        
        public AnimationCache(int maxSize) {
            this.maxSize = maxSize;
        }
        
        public void put(String key, CachedAnimation animation) {
            if (cache.size() >= maxSize) {
                // Simple LRU eviction
                String firstKey = cache.keySet().iterator().next();
                cache.remove(firstKey);
            }
            cache.put(key, animation);
        }
        
        public void clear() {
            cache.clear();
        }
    }
    
    static class CachedAnimation {
        private final String id;
        private final byte[] data;
        
        public CachedAnimation(String id, byte[] data) {
            this.id = id;
            this.data = data;
        }
    }
}