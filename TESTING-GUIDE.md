# Greta Platform Testing Guide

## Overview

This comprehensive testing guide covers all aspects of testing the modernized Greta platform, from unit tests to production deployment validation. The testing framework supports multiple testing methodologies and automation levels.

## Table of Contents

1. [Testing Architecture](#testing-architecture)
2. [Test Types and Frameworks](#test-types-and-frameworks)
3. [Execution Procedures](#execution-procedures)
4. [Results Analysis](#results-analysis)
5. [CI/CD Integration](#cicd-integration)
6. [Troubleshooting](#troubleshooting)

## Testing Architecture

### Test Pyramid Structure

```
                    [ E2E Tests ]
                [ Integration Tests ]
            [ Performance & Security Tests ]
        [     Component & Contract Tests      ]
    [           Unit Tests                        ]
```

### Test Categories

1. **Unit Tests**: Individual component testing
2. **Integration Tests**: System component interaction testing
3. **Performance Tests**: Load, stress, and benchmark testing
4. **Security Tests**: Vulnerability and penetration testing
5. **User Acceptance Tests**: End-user scenario validation
6. **Scalability Tests**: Horizontal and vertical scaling validation

## Test Types and Frameworks

### 1. Unit Tests (Maven Module Level)

**Location**: `*/src/test/java/`
**Framework**: JUnit 5 + Mockito
**Coverage Goal**: 80%+ line coverage

```bash
# Run unit tests for all modules
./mvnw test

# Run tests for specific module
./mvnw test -pl core/greta-util

# Generate coverage report
./mvnw jacoco:report
```

### 2. Integration Tests

**Location**: `integration-tests/`
**Framework**: JUnit 5 + Testcontainers + REST Assured
**Duration**: 15-30 minutes

```bash
# Run integration tests
cd integration-tests
./mvnw test

# Run specific integration test suite
./mvnw test -Dtest=AnimationSystemIT
```

**Key Features**:
- Containerized test environment (PostgreSQL, Redis, ActiveMQ)
- Real HTTP API testing
- Database integration validation
- Message queue testing
- Cross-module interaction testing

### 3. Performance Tests

**Location**: `performance-tests/`
**Frameworks**: JMH (microbenchmarks) + Gatling (load testing)
**Duration**: 2-4 hours

```bash
# Run complete performance test suite
./run-performance-tests.sh

# Run JMH benchmarks only
cd performance-tests
./mvnw clean package
java -jar target/greta-benchmarks.jar

# Run Gatling load tests
./mvnw gatling:test -Dgatling.simulationClass=greta.performance.GretaLoadSimulation
```

**Test Scenarios**:
- **Baseline**: 50 users, 5 minutes
- **Normal Load**: 100 users, 10 minutes  
- **Peak Load**: 500 users, 15 minutes
- **Stress Test**: 1000+ users, 10 minutes
- **Spike Test**: Sudden load increases
- **Memory Leak Detection**: 2+ hour sustained load

### 4. Security Tests

**Location**: `security-tests/`
**Framework**: JUnit 5 + OWASP tools + Custom security validators
**Duration**: 30-60 minutes

```bash
# Run security test suite
cd security-tests
./mvnw test

# Run OWASP dependency check
./mvnw org.owasp:dependency-check-maven:check

# Run security-focused static analysis
./mvnw spotbugs:check -Dspotbugs.includeFilterFile=spotbugs-security.xml
```

**Security Test Coverage**:
- Authentication and authorization
- Input validation and injection attacks
- CORS and CSRF protection
- Session management
- Cryptographic implementations
- Dependency vulnerabilities

### 5. User Acceptance Tests (UAT)

**Location**: `uat-tests/`
**Framework**: Cucumber (BDD) + Selenium + REST Assured
**Duration**: 45-90 minutes

```bash
# Run UAT scenarios
cd uat-tests
./mvnw test

# Run specific feature
./mvnw test -Dcucumber.filter.tags="@animation"
```

**UAT Scenarios**:
- Animation creation and management
- Behavior planning workflows
- Multi-user collaboration
- Educational content creation
- Research study execution

### 6. Scalability Tests

**Location**: `scalability-tests/`
**Framework**: Kubernetes + Gatling + Custom monitoring
**Duration**: 3-4 hours

```bash
# Run scalability test suite (requires Kubernetes cluster)
cd scalability-tests
./run-scalability-tests.sh

# Run specific scaling scenario
kubectl apply -f k8s/greta-scalability-test.yaml
```

**Scalability Scenarios**:
- Horizontal pod autoscaling (3-20 replicas)
- Database connection pool scaling
- Cache scalability validation
- Network bandwidth testing
- Geographic distribution simulation

## Execution Procedures

### Pre-Test Setup

1. **Environment Verification**:
   ```bash
   # Check Java version
   java -version  # Should be Java 11+
   
   # Check Docker
   docker --version
   docker-compose --version
   
   # Check Maven
   ./mvnw --version
   ```

2. **Build Application**:
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. **Start Dependencies** (for integration tests):
   ```bash
   docker-compose up -d postgres redis activemq
   ```

### Test Execution Order

#### Development Testing (Daily)
```bash
# 1. Unit tests (5-10 minutes)
./mvnw test

# 2. Integration tests (15-30 minutes)  
cd integration-tests && ./mvnw test

# 3. Security basics (10-15 minutes)
cd security-tests && ./mvnw test -Dtest=AuthenticationSecurityTest
```

#### Pre-Commit Testing (30-45 minutes)
```bash
# Run comprehensive test suite
./run-comprehensive-tests.sh
```

#### Release Testing (4-6 hours)
```bash
# 1. Full test suite
./run-all-tests.sh

# 2. Performance validation
./run-performance-tests.sh

# 3. Security audit
cd security-tests && ./mvnw verify

# 4. Scalability validation (if Kubernetes available)
cd scalability-tests && ./run-scalability-tests.sh
```

### Continuous Monitoring Tests

#### Smoke Tests (Production)
```bash
# Basic health and functionality checks
curl http://production-url/health
curl http://production-url/api/animation/status
```

#### Performance Monitoring
```bash
# Weekly performance baseline
./mvnw gatling:test -Dgatling.simulationClass=greta.performance.BaselineSimulation
```

## Results Analysis

### Performance Metrics Interpretation

#### Response Time Thresholds
- **Excellent**: < 500ms average
- **Good**: 500ms - 1000ms average
- **Acceptable**: 1000ms - 2000ms average
- **Poor**: > 2000ms average

#### Throughput Targets
- **Animation Creation**: 100+ requests/second
- **Behavior Planning**: 50+ requests/second
- **Resource Retrieval**: 500+ requests/second

#### Resource Usage Limits
- **CPU**: < 70% average, < 90% peak
- **Memory**: < 80% average, < 95% peak
- **Database Connections**: < 80% pool utilization

### Memory Leak Analysis

#### Warning Signs
- Consistent memory growth over time (> 10% per hour)
- GC frequency increase without load increase
- Out of memory errors in logs

#### Analysis Tools
```bash
# Heap dump analysis
jmap -dump:format=b,file=heap.hprof <pid>

# GC analysis
jstat -gc <pid> 5s

# Memory profiling during tests
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/
```

### Security Test Results

#### Critical Issues (Must Fix)
- Authentication bypass
- SQL injection vulnerabilities
- XSS vulnerabilities
- Insecure direct object references

#### High Priority Issues
- Weak password policies
- Insufficient session management
- Missing input validation

#### Acceptable Findings
- Minor information disclosure
- Low-impact configuration issues

## CI/CD Integration

### GitHub Actions Workflow

The testing framework integrates with GitHub Actions for automated testing:

```yaml
# .github/workflows/test.yml
name: Comprehensive Testing
on: [push, pull_request]
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '11'
      - name: Run Unit Tests
        run: ./mvnw test
  
  integration-tests:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test123
      redis:
        image: redis:7.2-alpine
    steps:
      - name: Run Integration Tests
        run: cd integration-tests && ./mvnw test
  
  security-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Run Security Tests
        run: cd security-tests && ./mvnw test
      - name: OWASP Dependency Check
        run: ./mvnw org.owasp:dependency-check-maven:check
```

### Quality Gates

#### Merge Requirements
- [ ] All unit tests pass (100%)
- [ ] Integration tests pass (100%)
- [ ] Security tests pass (100%)
- [ ] Code coverage > 80%
- [ ] No critical security vulnerabilities

#### Release Requirements
- [ ] All test suites pass
- [ ] Performance benchmarks within 10% of baseline
- [ ] No memory leaks detected
- [ ] Security audit clean
- [ ] Load testing successful

## Troubleshooting

### Common Issues

#### Test Environment Setup

**Issue**: Docker containers fail to start
```bash
# Solution: Clean and restart Docker
docker system prune -f
docker-compose down -v
docker-compose up -d
```

**Issue**: Port conflicts
```bash
# Solution: Check and kill processes using required ports
lsof -i :8080
kill -9 <pid>
```

#### Performance Test Issues

**Issue**: Gatling tests fail with connection errors
```bash
# Solution: Increase connection limits
ulimit -n 65536
# Or modify Gatling configuration
echo "gatling.http.pool.connectionTimeout = 30000" >> gatling.conf
```

**Issue**: Memory leak false positives
```bash
# Solution: Ensure proper GC before measurements
System.gc()
Thread.sleep(1000)
System.gc()
```

#### Security Test Issues

**Issue**: False positive security findings
```bash
# Solution: Add to suppression file
echo "<suppress>...</suppress>" >> owasp-suppressions.xml
```

### Log Analysis

#### Application Logs
```bash
# View real-time logs during testing
docker-compose logs -f greta-app

# Search for errors
grep -i error logs/greta.log

# Analyze performance logs
grep "SLOW QUERY" logs/greta.log
```

#### Test Execution Logs
```bash
# Maven test output
./mvnw test | tee test-output.log

# Gatling reports
open target/gatling/*/index.html

# JMH benchmark results
cat target/jmh-results.json | jq '.[]'
```

### Performance Debugging

#### CPU Profiling
```bash
# Enable JFR during tests
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=profile.jfr

# Analyze with JDK Mission Control
jmc profile.jfr
```

#### Memory Profiling
```bash
# Enable memory tracking
java -XX:NativeMemoryTracking=summary -XX:+PrintGCDetails

# Dump memory usage
jcmd <pid> VM.native_memory
```

## Best Practices

### Test Development
1. **Write tests first** (TDD approach)
2. **Use descriptive test names** that explain the scenario
3. **Keep tests independent** and idempotent
4. **Mock external dependencies** in unit tests
5. **Use real dependencies** in integration tests

### Test Maintenance
1. **Regular test review** and cleanup
2. **Update test data** to reflect realistic scenarios
3. **Monitor test execution time** and optimize slow tests
4. **Keep test documentation** up to date

### Performance Testing
1. **Establish baselines** early in development
2. **Test with production-like data** volumes
3. **Monitor resource usage** during tests
4. **Automate performance regression** detection

### Security Testing
1. **Test early and often** in development cycle
2. **Keep security tools** up to date
3. **Review and validate** all security findings
4. **Integrate security testing** into CI/CD pipeline

## Conclusion

This testing framework provides comprehensive validation of the Greta platform across all quality dimensions. Regular execution of these tests ensures the platform maintains high standards of functionality, performance, security, and scalability.

For questions or issues with the testing framework, consult the troubleshooting section or refer to individual test module documentation.