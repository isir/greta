#!/bin/bash

# Performance Testing Script for Greta Platform
# Runs JMH benchmarks and Gatling load tests

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_status $RED "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Configuration
RESULTS_DIR="performance-results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_PATH="$RESULTS_DIR/$TIMESTAMP"

# Create results directory
mkdir -p "$RESULTS_PATH"

print_status $PURPLE "🚀 Greta Performance Testing Suite"
print_status $PURPLE "================================="
echo ""

# 1. Build the application if needed
print_status $YELLOW "📦 Building Greta application..."
if [ ! -f "target/greta-application-*.jar" ]; then
    cd ..
    ./mvnw clean package -DskipTests
    cd performance-tests
fi

# 2. Start Greta application for testing
print_status $YELLOW "🐳 Starting Greta application..."
docker-compose -f ../docker-compose.yml up -d
sleep 30  # Wait for application to start

# 3. Run JMH Benchmarks
print_status $YELLOW "⚡ Running JMH benchmarks..."
if ./mvnw clean package; then
    java -jar target/greta-benchmarks.jar \
        -rf json -rff "$RESULTS_PATH/jmh-results.json" \
        -rf text -rff "$RESULTS_PATH/jmh-results.txt"
    print_status $GREEN "✅ JMH benchmarks completed"
else
    print_status $RED "❌ JMH benchmarks failed"
fi

# 4. Run Memory Profiling
print_status $YELLOW "💾 Running memory profiling..."
java -XX:+UseG1GC -Xmx2g \
     -cp target/greta-performance-tests-*.jar \
     greta.performance.MemoryProfiler \
     > "$RESULTS_PATH/memory-profile.txt" 2>&1
print_status $GREEN "✅ Memory profiling completed"

# 5. Run Gatling Load Tests
print_status $YELLOW "📊 Running Gatling load tests..."

# Normal load test
print_status $BLUE "Running normal load profile..."
./mvnw gatling:test \
    -Dgatling.simulationClass=greta.performance.GretaLoadSimulation \
    -Dgreta.test.users=100 \
    -Dgreta.test.duration=300 \
    -Dgatling.resultsFolder="$RESULTS_PATH/gatling-normal"

# Spike test
print_status $BLUE "Running spike test..."
./mvnw gatling:test \
    -Dgatling.simulationClass=greta.performance.GretaLoadSimulation \
    -Dgatling.profile=spike \
    -Dgatling.resultsFolder="$RESULTS_PATH/gatling-spike"

# Stress test
print_status $BLUE "Running stress test..."
./mvnw gatling:test \
    -Dgatling.simulationClass=greta.performance.GretaLoadSimulation \
    -Dgatling.profile=stress \
    -Dgatling.resultsFolder="$RESULTS_PATH/gatling-stress"

print_status $GREEN "✅ Gatling load tests completed"

# 6. Collect container metrics
print_status $YELLOW "📈 Collecting container metrics..."
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" \
    > "$RESULTS_PATH/container-metrics.txt"

# 7. Generate summary report
print_status $YELLOW "📝 Generating summary report..."
cat > "$RESULTS_PATH/performance-summary.md" << EOF
# Greta Performance Test Results

**Date**: $(date)
**Test Duration**: ~15 minutes

## JMH Benchmark Results

### Animation Processing
$(grep -A 5 "AnimationBenchmark.benchmarkAnimationProcessing" "$RESULTS_PATH/jmh-results.txt" || echo "No results")

### Concurrent Processing
$(grep -A 5 "AnimationBenchmark.benchmarkConcurrentProcessing" "$RESULTS_PATH/jmh-results.txt" || echo "No results")

## Memory Profile Summary
$(tail -20 "$RESULTS_PATH/memory-profile.txt")

## Load Test Results

### Normal Load (100 users)
- See: $RESULTS_PATH/gatling-normal/index.html

### Spike Test
- See: $RESULTS_PATH/gatling-spike/index.html

### Stress Test
- See: $RESULTS_PATH/gatling-stress/index.html

## Container Resource Usage
$(cat "$RESULTS_PATH/container-metrics.txt")

## Recommendations

1. **Performance Optimizations**:
   - Review any operations taking >1000ms
   - Optimize database queries showing high latency
   - Consider caching for frequently accessed data

2. **Scalability**:
   - Current setup handles ~X concurrent users
   - Consider horizontal scaling at >Y users
   - Database connection pooling may need adjustment

3. **Memory Management**:
   - Monitor for memory leaks in long-running scenarios
   - Adjust JVM heap settings based on actual usage
   - Consider G1GC tuning parameters
EOF

# 8. Cleanup
print_status $YELLOW "🧹 Cleaning up..."
docker-compose -f ../docker-compose.yml down

# 9. Display results
print_status $GREEN "✅ Performance testing completed!"
print_status $BLUE "📊 Results saved to: $RESULTS_PATH"
print_status $BLUE "📈 View detailed reports:"
echo "  - JMH Results: $RESULTS_PATH/jmh-results.txt"
echo "  - Memory Profile: $RESULTS_PATH/memory-profile.txt"
echo "  - Load Test Reports: $RESULTS_PATH/gatling-*/index.html"
echo "  - Summary: $RESULTS_PATH/performance-summary.md"

# Open summary in browser if available
if command -v open &> /dev/null; then
    open "$RESULTS_PATH/performance-summary.md"
fi