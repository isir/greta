package greta.performance;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Memory profiling and leak detection for Greta components
 */
public class MemoryProfiler {
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final Runtime runtime = Runtime.getRuntime();
    
    public static void main(String[] args) {
        System.out.println(VM.current().details());
        
        // Run different memory profiling scenarios
        profileAnimationMemoryUsage();
        profileBehaviorPlannerMemory();
        profileCacheMemoryUsage();
        detectMemoryLeaks();
    }
    
    /**
     * Profile memory usage of animation system
     */
    private static void profileAnimationMemoryUsage() {
        System.out.println("\n=== Animation System Memory Profile ===");
        
        // Create animation objects
        List<AnimationFrame> frames = new ArrayList<>();
        
        // Measure baseline
        long baselineMemory = getUsedMemory();
        
        // Create 10,000 animation frames
        for (int i = 0; i < 10000; i++) {
            AnimationFrame frame = new AnimationFrame(i / 30.0);
            frame.addJoint("head", new Position(0, 0, 0));
            frame.addJoint("leftHand", new Position(1, 1, 1));
            frame.addJoint("rightHand", new Position(-1, -1, -1));
            frames.add(frame);
        }
        
        long memoryAfterCreation = getUsedMemory();
        long memoryUsed = memoryAfterCreation - baselineMemory;
        
        System.out.println("Memory used for 10,000 frames: " + formatBytes(memoryUsed));
        System.out.println("Average memory per frame: " + formatBytes(memoryUsed / 10000));
        
        // Analyze object layout
        System.out.println("\nAnimationFrame layout:");
        System.out.println(ClassLayout.parseClass(AnimationFrame.class).toPrintable());
        
        // Analyze object graph
        AnimationFrame sampleFrame = frames.get(0);
        System.out.println("\nObject graph size:");
        System.out.println(GraphLayout.parseInstance(sampleFrame).toFootprint());
        
        // Clear for GC
        frames.clear();
        System.gc();
    }
    
    /**
     * Profile behavior planner memory usage
     */
    private static void profileBehaviorPlannerMemory() {
        System.out.println("\n=== Behavior Planner Memory Profile ===");
        
        long baselineMemory = getUsedMemory();
        
        // Create behavior plan
        BehaviorPlan plan = new BehaviorPlan();
        
        // Add behaviors
        for (int i = 0; i < 1000; i++) {
            Behavior behavior = new Behavior("behavior_" + i);
            behavior.setStartTime(i * 0.1);
            behavior.setDuration(2.0);
            behavior.addParameter("intensity", 0.8);
            behavior.addParameter("target", "user");
            plan.addBehavior(behavior);
        }
        
        long memoryAfterPlan = getUsedMemory();
        long planMemory = memoryAfterPlan - baselineMemory;
        
        System.out.println("Memory used for behavior plan (1000 behaviors): " + 
                         formatBytes(planMemory));
        System.out.println("Average memory per behavior: " + 
                         formatBytes(planMemory / 1000));
        
        // Profile behavior resolution
        for (int i = 0; i < 100; i++) {
            plan.resolveConflicts();
        }
        
        long memoryAfterResolution = getUsedMemory();
        System.out.println("Additional memory for conflict resolution: " + 
                         formatBytes(memoryAfterResolution - memoryAfterPlan));
    }
    
    /**
     * Profile cache memory usage
     */
    private static void profileCacheMemoryUsage() {
        System.out.println("\n=== Cache Memory Profile ===");
        
        AnimationCache cache = new AnimationCache(1000);
        long baselineMemory = getUsedMemory();
        
        // Fill cache
        for (int i = 0; i < 1000; i++) {
            String key = "animation_" + i;
            CachedAnimation animation = new CachedAnimation(key);
            animation.setData(new byte[1024]); // 1KB per cached item
            cache.put(key, animation);
        }
        
        long cacheMemory = getUsedMemory() - baselineMemory;
        System.out.println("Cache memory usage (1000 items): " + formatBytes(cacheMemory));
        System.out.println("Average memory per cached item: " + formatBytes(cacheMemory / 1000));
        
        // Test cache eviction
        for (int i = 1000; i < 2000; i++) {
            String key = "animation_" + i;
            CachedAnimation animation = new CachedAnimation(key);
            animation.setData(new byte[1024]);
            cache.put(key, animation);
        }
        
        long memoryAfterEviction = getUsedMemory();
        System.out.println("Memory after cache eviction: " + 
                         formatBytes(memoryAfterEviction - baselineMemory));
        
        // Analyze cache efficiency
        System.out.println("Cache hit rate: " + cache.getHitRate() + "%");
        System.out.println("Cache size: " + cache.size());
    }
    
    /**
     * Detect potential memory leaks
     */
    private static void detectMemoryLeaks() {
        System.out.println("\n=== Memory Leak Detection ===");
        
        List<Long> memorySnapshots = new ArrayList<>();
        LeakySingleton leaky = LeakySingleton.getInstance();
        
        // Simulate operations that might leak memory
        for (int iteration = 0; iteration < 10; iteration++) {
            // Perform operations
            for (int i = 0; i < 10000; i++) {
                leaky.addListener(new EventListener("listener_" + i));
                leaky.processEvent("event_" + i);
            }
            
            // Force GC and measure memory
            System.gc();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            long usedMemory = getUsedMemory();
            memorySnapshots.add(usedMemory);
            
            System.out.println("Iteration " + iteration + ": " + formatBytes(usedMemory));
            
            // Clear some listeners (but not all - simulating a leak)
            leaky.clearListeners(5000); // Only clear half
        }
        
        // Analyze memory growth
        boolean potentialLeak = isMemoryLeaking(memorySnapshots);
        if (potentialLeak) {
            System.out.println("WARNING: Potential memory leak detected!");
            System.out.println("Memory growth over iterations: " + 
                             formatBytes(memorySnapshots.get(9) - memorySnapshots.get(0)));
        } else {
            System.out.println("No significant memory leak detected.");
        }
    }
    
    /**
     * Helper method to get current used memory
     */
    private static long getUsedMemory() {
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * Format bytes to human-readable string
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
        return String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }
    
    /**
     * Analyze memory snapshots for potential leaks
     */
    private static boolean isMemoryLeaking(List<Long> snapshots) {
        if (snapshots.size() < 3) return false;
        
        // Calculate average growth rate
        long totalGrowth = 0;
        for (int i = 1; i < snapshots.size(); i++) {
            totalGrowth += snapshots.get(i) - snapshots.get(i - 1);
        }
        
        double averageGrowth = (double) totalGrowth / (snapshots.size() - 1);
        
        // If average growth is positive and significant, it might be a leak
        return averageGrowth > 1024 * 1024; // More than 1MB average growth
    }
    
    // Mock classes for profiling
    static class AnimationFrame {
        private final double timestamp;
        private final Map<String, Position> joints = new HashMap<>();
        
        public AnimationFrame(double timestamp) {
            this.timestamp = timestamp;
        }
        
        public void addJoint(String name, Position position) {
            joints.put(name, position);
        }
    }
    
    static class Position {
        private final double x, y, z;
        
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
            // Simulate conflict resolution
            Collections.sort(behaviors, Comparator.comparingDouble(Behavior::getStartTime));
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
        
        public void addParameter(String key, Object value) {
            parameters.put(key, value);
        }
    }
    
    static class AnimationCache {
        private final int maxSize;
        private final Map<String, CachedAnimation> cache = new ConcurrentHashMap<>();
        private long hits = 0;
        private long misses = 0;
        
        public AnimationCache(int maxSize) {
            this.maxSize = maxSize;
        }
        
        public void put(String key, CachedAnimation animation) {
            if (cache.size() >= maxSize) {
                // Simple eviction - remove first item
                String firstKey = cache.keySet().iterator().next();
                cache.remove(firstKey);
            }
            cache.put(key, animation);
        }
        
        public CachedAnimation get(String key) {
            CachedAnimation result = cache.get(key);
            if (result != null) {
                hits++;
            } else {
                misses++;
            }
            return result;
        }
        
        public int size() {
            return cache.size();
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total == 0 ? 0 : (hits * 100.0) / total;
        }
    }
    
    static class CachedAnimation {
        private final String id;
        private byte[] data;
        
        public CachedAnimation(String id) {
            this.id = id;
        }
        
        public void setData(byte[] data) {
            this.data = data;
        }
    }
    
    // Singleton with potential memory leak
    static class LeakySingleton {
        private static final LeakySingleton INSTANCE = new LeakySingleton();
        private final List<EventListener> listeners = new ArrayList<>();
        private final List<String> processedEvents = new ArrayList<>();
        
        public static LeakySingleton getInstance() {
            return INSTANCE;
        }
        
        public void addListener(EventListener listener) {
            listeners.add(listener);
        }
        
        public void processEvent(String event) {
            processedEvents.add(event);
            // Notify listeners (but don't remove old events - memory leak!)
        }
        
        public void clearListeners(int count) {
            // Only clear some listeners, not all
            for (int i = 0; i < count && !listeners.isEmpty(); i++) {
                listeners.remove(0);
            }
        }
    }
    
    static class EventListener {
        private final String name;
        private final byte[] data = new byte[1024]; // 1KB per listener
        
        public EventListener(String name) {
            this.name = name;
        }
    }
}