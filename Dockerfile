# Multi-stage optimized Dockerfile for Greta Platform
# Build arguments for customization
ARG MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m"
ARG JAVA_BUILD_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication"
ARG GRETA_VERSION="1.0.0-SNAPSHOT"

# =============================================================================
# Dependencies stage - for better layer caching
# =============================================================================
FROM eclipse-temurin:11-jdk-alpine AS dependencies

# Install build dependencies
RUN apk add --no-cache curl git && \
    rm -rf /var/cache/apk/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn/
RUN chmod +x mvnw

# Copy all POM files first for optimal dependency caching
COPY pom.xml ./

# Create directory structure for POMs
RUN mkdir -p core auxiliary application

# Copy all module POM files (ignore failures for missing directories)
COPY --chown=root:root . /tmp/source/
RUN for dir in /tmp/source/core/*/; do \
      if [ -d "$dir" ] && [ -f "$dir/pom.xml" ]; then \
        module=$(basename "$dir"); \
        mkdir -p "core/$module"; \
        cp "$dir/pom.xml" "core/$module/"; \
      fi; \
    done 2>/dev/null || true && \
    for dir in /tmp/source/auxiliary/*/; do \
      if [ -d "$dir" ] && [ -f "$dir/pom.xml" ]; then \
        module=$(basename "$dir"); \
        mkdir -p "auxiliary/$module"; \
        cp "$dir/pom.xml" "auxiliary/$module/"; \
      fi; \
    done 2>/dev/null || true && \
    for dir in /tmp/source/application/*/; do \
      if [ -d "$dir" ] && [ -f "$dir/pom.xml" ]; then \
        module=$(basename "$dir"); \
        mkdir -p "application/$module"; \
        cp "$dir/pom.xml" "application/$module/"; \
      fi; \
    done 2>/dev/null || true && \
    rm -rf /tmp/source

# Download dependencies with optimized settings
ENV MAVEN_OPTS="${MAVEN_OPTS}"
RUN ./mvnw dependency:go-offline dependency:resolve-sources -B -T 1C

# =============================================================================
# Builder stage - compile and package
# =============================================================================
FROM dependencies AS builder

# Copy source code in optimal order (most stable first)
COPY core/ core/
COPY auxiliary/ auxiliary/
COPY application/ application/
COPY src/ src/
COPY bin/ bin/

# Build with optimized Maven settings
ENV JAVA_OPTS="${JAVA_BUILD_OPTS}"
RUN ./mvnw clean package -DskipTests -B -T 1C \
    -Dmaven.compile.fork=true \
    -Dmaven.compiler.maxmem=1024m && \
    # Clean up build artifacts to reduce layer size
    find . -name "*.class" -type f -delete 2>/dev/null || true && \
    find . -name "surefire-reports" -type d -exec rm -rf {} + 2>/dev/null || true

# =============================================================================
# Production runtime stage - optimized for size and security
# =============================================================================
FROM eclipse-temurin:11-jre-alpine AS runtime

# Install runtime dependencies in single layer
RUN apk add --no-cache \
    # Core system utilities
    bash curl tini \
    # Graphics and font support
    fontconfig ttf-dejavu mesa-gl \
    # Audio support
    alsa-lib \
    # Network utilities
    net-tools procps \
    && rm -rf /var/cache/apk/* /tmp/* /var/tmp/*

# Create application user and directories
RUN addgroup -g 1001 -S greta && \
    adduser -D -u 1001 -G greta -S greta && \
    mkdir -p /app/{lib,data,logs,config,cache} && \
    mkdir -p /tmp/greta && \
    chown -R greta:greta /app /tmp/greta

# Set working directory
WORKDIR /app

# Copy built artifacts with proper permissions
COPY --from=builder --chown=greta:greta /app/application/*/target/*.jar ./
COPY --from=builder --chown=greta:greta /app/core/*/target/*.jar ./lib/
COPY --from=builder --chown=greta:greta /app/auxiliary/*/target/*.jar ./lib/

# Copy essential data and configuration files
COPY --from=builder --chown=greta:greta /app/bin/Common/Data/ ./data/
COPY --from=builder --chown=greta:greta /app/auxiliary/*/Data/ ./data/auxiliary/ 2>/dev/null || true
COPY --from=builder --chown=greta:greta /app/src/main/resources/ ./config/

# Create startup script with better error handling
RUN cat > /app/start-greta.sh << 'EOF' && \
#!/bin/bash
set -e

# Environment setup
export GRETA_HOME="/app"
export GRETA_DATA="/app/data"
export GRETA_LOGS="/app/logs"
export GRETA_CONFIG="/app/config"
export JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

# JVM optimization based on container resources
MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo "2147483648")
CPU_LIMIT=$(nproc)
HEAP_SIZE=$((MEMORY_LIMIT / 1024 / 1024 * 75 / 100))

if [ $HEAP_SIZE -lt 512 ]; then HEAP_SIZE=512; fi
if [ $HEAP_SIZE -gt 4096 ]; then HEAP_SIZE=4096; fi

# Default JVM options if not provided
if [ -z "$JAVA_OPTS" ]; then
    export JAVA_OPTS="-Xmx${HEAP_SIZE}m -Xms$((HEAP_SIZE / 2))m \
        -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
        -XX:+UseStringDeduplication \
        -XX:+OptimizeStringConcat \
        -Djava.awt.headless=true \
        -Dfile.encoding=UTF-8 \
        -Duser.timezone=UTC"
fi

# Find the main JAR file
MAIN_JAR=$(find /app -maxdepth 1 -name "greta-application-*.jar" | head -1)
if [ -z "$MAIN_JAR" ]; then
    echo "ERROR: No main application JAR found"
    exit 1
fi

echo "Starting Greta Platform..."
echo "Java Options: $JAVA_OPTS"
echo "Main JAR: $MAIN_JAR"
echo "Available Memory: $HEAP_SIZE MB"

# Start the application
exec java $JAVA_OPTS -jar "$MAIN_JAR" "$@"
EOF
RUN chmod +x /app/start-greta.sh

# Switch to application user
USER greta

# Set optimized environment variables
ENV GRETA_HOME="/app" \
    GRETA_DATA="/app/data" \
    GRETA_LOGS="/app/logs" \
    GRETA_CONFIG="/app/config" \
    GRETA_VERSION="${GRETA_VERSION}" \
    JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom" \
    TZ="UTC"

# Expose ports with documentation
EXPOSE 8080/tcp
EXPOSE 61616/tcp
EXPOSE 1883/tcp
EXPOSE 8081/tcp

# Enhanced health check
HEALTHCHECK --interval=30s --timeout=15s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/health || \
        curl -f http://localhost:8081/health || \
        pgrep -f "java.*greta" > /dev/null || exit 1

# Use tini as init system for proper signal handling
ENTRYPOINT ["/sbin/tini", "--"]

# Default command with fallback
CMD ["/app/start-greta.sh"]

# Comprehensive metadata labels
LABEL org.opencontainers.image.title="Greta Platform" \
      org.opencontainers.image.description="Modernized Embodied Conversational Agent Platform" \
      org.opencontainers.image.source="https://github.com/isir/greta" \
      org.opencontainers.image.version="${GRETA_VERSION}" \
      org.opencontainers.image.vendor="ISIR" \
      org.opencontainers.image.licenses="LGPL-3.0" \
      org.opencontainers.image.documentation="https://github.com/isir/greta/blob/modernization_with_claude/DEPLOYMENT.md" \
      org.opencontainers.image.url="https://github.com/isir/greta" \
      maintainer="modernization-team" \
      greta.platform.version="${GRETA_VERSION}" \
      greta.build.optimization="production"