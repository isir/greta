#!/bin/bash

# Comprehensive Test Runner for Greta Platform
# Executes all test suites in proper order with reporting

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Configuration
RESULTS_DIR="test-results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_PATH="$RESULTS_DIR/$TIMESTAMP"
PARALLEL_TESTS=${PARALLEL_TESTS:-false}
SKIP_LONG_TESTS=${SKIP_LONG_TESTS:-false}

# Create results directory
mkdir -p "$RESULTS_PATH"

print_status $PURPLE "🧪 Greta Platform Comprehensive Test Suite"
print_status $PURPLE "=========================================="
echo ""

# Test execution summary
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
START_TIME=$(date +%s)

# Function to run test suite
run_test_suite() {
    local suite_name=$1
    local suite_path=$2
    local test_command=$3
    local timeout=${4:-1800}  # 30 minutes default
    
    print_status $YELLOW "🔬 Running $suite_name..."
    
    local suite_start=$(date +%s)
    local success=true
    
    # Create suite-specific results directory
    local suite_results="$RESULTS_PATH/$suite_name"
    mkdir -p "$suite_results"
    
    # Run the test command
    if [ "$PARALLEL_TESTS" = "true" ]; then
        timeout $timeout bash -c "$test_command" > "$suite_results/output.log" 2>&1 &
        local test_pid=$!
        echo "$test_pid" > "$suite_results/pid"
    else
        if timeout $timeout bash -c "$test_command" > "$suite_results/output.log" 2>&1; then
            print_status $GREEN "✅ $suite_name passed"
            ((PASSED_TESTS++))
        else
            print_status $RED "❌ $suite_name failed"
            echo "Error output:" >> "$suite_results/output.log"
            tail -20 "$suite_results/output.log"
            success=false
            ((FAILED_TESTS++))
        fi
    fi
    
    ((TOTAL_TESTS++))
    
    local suite_end=$(date +%s)
    local suite_duration=$((suite_end - suite_start))
    echo "$suite_duration" > "$suite_results/duration.txt"
    
    if [ "$success" = false ] && [ "$FAIL_FAST" = "true" ]; then
        print_status $RED "🛑 Failing fast due to test failure"
        exit 1
    fi
}

# Function to wait for parallel tests
wait_for_parallel_tests() {
    if [ "$PARALLEL_TESTS" = "true" ]; then
        print_status $YELLOW "⏳ Waiting for parallel tests to complete..."
        
        for suite_dir in "$RESULTS_PATH"/*; do
            if [ -f "$suite_dir/pid" ]; then
                local pid=$(cat "$suite_dir/pid")
                local suite_name=$(basename "$suite_dir")
                
                if wait $pid; then
                    print_status $GREEN "✅ $suite_name completed successfully"
                    ((PASSED_TESTS++))
                else
                    print_status $RED "❌ $suite_name failed"
                    ((FAILED_TESTS++))
                fi
                
                rm -f "$suite_dir/pid"
            fi
        done
    fi
}

# Pre-test setup
print_status $YELLOW "🔧 Setting up test environment..."

# Verify prerequisites
if ! command -v docker &> /dev/null; then
    print_status $RED "❌ Docker not found"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_status $RED "❌ Docker Compose not found"
    exit 1
fi

# Build application
print_status $BLUE "Building application..."
if ! ./mvnw clean package -DskipTests > "$RESULTS_PATH/build.log" 2>&1; then
    print_status $RED "❌ Build failed"
    cat "$RESULTS_PATH/build.log"
    exit 1
fi

print_status $GREEN "✅ Build completed"

# Start test infrastructure
print_status $BLUE "Starting test infrastructure..."
docker-compose -f docker-compose.test.yml up -d > "$RESULTS_PATH/infrastructure.log" 2>&1
sleep 10

# 1. Unit Tests
run_test_suite "unit-tests" "." "./mvnw test -B" 900

# 2. Integration Tests
run_test_suite "integration-tests" "integration-tests" "cd integration-tests && ./mvnw test -B" 1800

# 3. Security Tests
run_test_suite "security-tests" "security-tests" "cd security-tests && ./mvnw test -B" 1200

# 4. User Acceptance Tests
run_test_suite "uat-tests" "uat-tests" "cd uat-tests && ./mvnw test -B" 2400

# Wait for parallel tests if enabled
wait_for_parallel_tests

# 5. Performance Tests (optional for quick runs)
if [ "$SKIP_LONG_TESTS" != "true" ]; then
    run_test_suite "performance-tests" "performance-tests" "./run-performance-tests.sh" 7200
fi

# 6. Memory Leak Tests (optional for quick runs)
if [ "$SKIP_LONG_TESTS" != "true" ]; then
    run_test_suite "memory-leak-tests" "performance-tests" "cd performance-tests && ./mvnw test -Dtest=LongRunningMemoryLeakTest" 3600
fi

# Generate comprehensive report
print_status $YELLOW "📊 Generating test report..."

END_TIME=$(date +%s)
TOTAL_DURATION=$((END_TIME - START_TIME))

cat > "$RESULTS_PATH/test-summary.md" << EOF
# Greta Platform Test Execution Report

**Date**: $(date)
**Duration**: $((TOTAL_DURATION / 60)) minutes $((TOTAL_DURATION % 60)) seconds
**Total Test Suites**: $TOTAL_TESTS
**Passed**: $PASSED_TESTS
**Failed**: $FAILED_TESTS
**Success Rate**: $((PASSED_TESTS * 100 / TOTAL_TESTS))%

## Test Suite Results

EOF

# Add individual suite results
for suite_dir in "$RESULTS_PATH"/*; do
    if [ -d "$suite_dir" ] && [ -f "$suite_dir/duration.txt" ]; then
        suite_name=$(basename "$suite_dir")
        duration=$(cat "$suite_dir/duration.txt")
        
        if grep -q "BUILD SUCCESS\|Tests run.*Failures: 0" "$suite_dir/output.log" 2>/dev/null; then
            status="✅ PASSED"
        else
            status="❌ FAILED"
        fi
        
        echo "### $suite_name" >> "$RESULTS_PATH/test-summary.md"
        echo "- **Status**: $status" >> "$RESULTS_PATH/test-summary.md"
        echo "- **Duration**: $((duration / 60))m $((duration % 60))s" >> "$RESULTS_PATH/test-summary.md"
        echo "- **Log**: [output.log]($suite_name/output.log)" >> "$RESULTS_PATH/test-summary.md"
        echo "" >> "$RESULTS_PATH/test-summary.md"
    fi
done

# Add recommendations
cat >> "$RESULTS_PATH/test-summary.md" << EOF
## Recommendations

$(if [ $FAILED_TESTS -eq 0 ]; then
    echo "🎉 All tests passed! The system is ready for deployment."
else
    echo "⚠️ $FAILED_TESTS test suite(s) failed. Review failed tests before proceeding:"
    for suite_dir in "$RESULTS_PATH"/*; do
        if [ -d "$suite_dir" ] && [ -f "$suite_dir/output.log" ]; then
            suite_name=$(basename "$suite_dir")
            if ! grep -q "BUILD SUCCESS\|Tests run.*Failures: 0" "$suite_dir/output.log" 2>/dev/null; then
                echo "- Review $suite_name/output.log for details"
            fi
        fi
    done
fi)

## Quality Metrics

$(if [ -f "$RESULTS_PATH/unit-tests/output.log" ]; then
    echo "### Code Coverage"
    if grep -q "jacoco" "$RESULTS_PATH/unit-tests/output.log"; then
        echo "- Coverage report available in target/site/jacoco/index.html"
    else
        echo "- Run './mvnw jacoco:report' to generate coverage report"
    fi
fi)

$(if [ -f "$RESULTS_PATH/security-tests/output.log" ]; then
    echo "### Security Analysis"
    if grep -q "dependency-check" "$RESULTS_PATH/security-tests/output.log"; then
        echo "- OWASP dependency check completed"
        echo "- Review target/dependency-check-report.html for vulnerabilities"
    fi
fi)

$(if [ -f "$RESULTS_PATH/performance-tests/output.log" ]; then
    echo "### Performance Metrics"
    echo "- Performance test results in performance-results/"
    echo "- Review JMH benchmarks and Gatling reports"
fi)

EOF

# Cleanup test infrastructure
print_status $YELLOW "🧹 Cleaning up test infrastructure..."
docker-compose -f docker-compose.test.yml down > /dev/null 2>&1

# Display results
print_status $PURPLE "📋 Test Execution Summary"
print_status $PURPLE "========================="
echo ""
print_status $BLUE "Total Duration: $((TOTAL_DURATION / 60))m $((TOTAL_DURATION % 60))s"
print_status $BLUE "Test Suites: $TOTAL_TESTS"
print_status $GREEN "Passed: $PASSED_TESTS"
if [ $FAILED_TESTS -gt 0 ]; then
    print_status $RED "Failed: $FAILED_TESTS"
else
    print_status $GREEN "Failed: $FAILED_TESTS"
fi

success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
if [ $success_rate -eq 100 ]; then
    print_status $GREEN "Success Rate: ${success_rate}%"
else
    print_status $YELLOW "Success Rate: ${success_rate}%"
fi

echo ""
print_status $BLUE "📊 Detailed results: $RESULTS_PATH/test-summary.md"

# Open results if possible
if command -v open &> /dev/null; then
    open "$RESULTS_PATH/test-summary.md"
elif command -v xdg-open &> /dev/null; then
    xdg-open "$RESULTS_PATH/test-summary.md"
fi

# Exit with appropriate code
if [ $FAILED_TESTS -eq 0 ]; then
    print_status $GREEN "🎉 All tests passed!"
    exit 0
else
    print_status $RED "❌ Some tests failed"
    exit 1
fi