# Multi-stage optimized Dockerfile for Greta Platform
# Build arguments for customization
ARG MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m"
ARG JAVA_BUILD_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication"
ARG GRETA_VERSION="1.0.0-SNAPSHOT"

# =============================================================================
# Dependencies stage - for better layer caching
# =============================================================================
FROM amazoncorretto:11 AS dependencies

# Set environment variables
ENV GRETA_VERSION="${GRETA_VERSION}"

# Install build dependencies
RUN yum update -y && yum install -y curl git && \
    yum clean all

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw ./
COPY .mvn .mvn/
RUN chmod +x mvnw

# Copy all POM files first for optimal dependency caching
COPY pom.xml ./

# Create directory structure for POMs
RUN mkdir -p core auxiliary application

# Copy all module POM files (including nested directories like TTS)
COPY --chown=root:root . /tmp/source/
RUN find /tmp/source -name "pom.xml" -not -path "/tmp/source/pom.xml" | while read pom; do \
      rel_path=$(echo "$pom" | sed 's|/tmp/source/||' | sed 's|/pom.xml||'); \
      mkdir -p "$rel_path"; \
      cp "$pom" "$rel_path/"; \
    done && \
    rm -rf /tmp/source

# Download dependencies with optimized settings
ENV MAVEN_OPTS="-Xmx2g -Dmaven.repo.local=/root/.m2/repository"
RUN ./mvnw dependency:go-offline -B -T 1C

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
COPY HeadlessServer.java ./

# Build with optimized Maven settings
ENV JAVA_OPTS="-Xmx1g"
RUN ./mvnw clean package -DskipTests -B -T 1C \
    -Dmaven.compile.fork=true \
    -Dmaven.compiler.maxmem=1024m && \
    # Compile HeadlessServer \
    javac HeadlessServer.java && \
    # Clean up build artifacts to reduce layer size (keep HeadlessServer.class)
    find . -name "*.class" -not -name "HeadlessServer.class" -type f -delete 2>/dev/null || true && \
    find . -name "surefire-reports" -type d -exec rm -rf {} + 2>/dev/null || true

# =============================================================================
# Production runtime stage - optimized for size and security
# =============================================================================
FROM amazoncorretto:11 AS runtime

# Install runtime dependencies in single layer
RUN yum update -y && yum install -y \
    # Core system utilities
    bash curl tini \
    # Graphics and font support
    fontconfig dejavu-fonts mesa-dri-drivers \
    # Audio support
    alsa-lib \
    # Network utilities
    net-tools procps-ng \
    && yum clean all

# Create application user and directories
RUN groupadd -g 1001 greta && \
    useradd -r -u 1001 -g greta -s /bin/bash greta && \
    mkdir -p /app/{lib,data,logs,config,cache} && \
    mkdir -p /tmp/greta && \
    chown -R greta:greta /app /tmp/greta

# Set working directory
WORKDIR /app

# Copy built artifacts with proper permissions
COPY --from=builder --chown=greta:greta /app/application/*/target/*.jar ./
COPY --from=builder --chown=greta:greta /app/core/*/target/*.jar ./lib/
COPY --from=builder --chown=greta:greta /app/auxiliary/*/target/*.jar ./lib/
COPY --from=builder --chown=greta:greta /app/HeadlessServer.class ./

# Copy essential data and configuration files
COPY --from=builder --chown=greta:greta /app/bin/Common/Data/ ./data/
RUN --mount=from=builder,source=/app,target=/tmp/builder \
    mkdir -p ./data/auxiliary/ && \
    if [ -d "/tmp/builder/auxiliary" ]; then \
      find /tmp/builder/auxiliary -name "Data" -type d -exec cp -r {} ./data/auxiliary/ \; 2>/dev/null || true; \
    fi
COPY --from=builder --chown=greta:greta /app/src/main/resources/ ./config/

# Create startup script with better error handling
RUN echo '#!/bin/bash' > /app/start-greta.sh && \
    echo 'set -e' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo '# Environment setup' >> /app/start-greta.sh && \
    echo 'export GRETA_HOME="/app"' >> /app/start-greta.sh && \
    echo 'export GRETA_DATA="/app/data"' >> /app/start-greta.sh && \
    echo 'export GRETA_LOGS="/app/logs"' >> /app/start-greta.sh && \
    echo 'export GRETA_CONFIG="/app/config"' >> /app/start-greta.sh && \
    echo 'export JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom"' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo '# JVM optimization based on container resources' >> /app/start-greta.sh && \
    echo 'MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo "2147483648")' >> /app/start-greta.sh && \
    echo 'CPU_LIMIT=$(nproc)' >> /app/start-greta.sh && \
    echo 'HEAP_SIZE=$((MEMORY_LIMIT / 1024 / 1024 * 75 / 100))' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo 'if [ $HEAP_SIZE -lt 512 ]; then HEAP_SIZE=512; fi' >> /app/start-greta.sh && \
    echo 'if [ $HEAP_SIZE -gt 4096 ]; then HEAP_SIZE=4096; fi' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo '# Default JVM options if not provided' >> /app/start-greta.sh && \
    echo 'if [ -z "$JAVA_OPTS" ]; then' >> /app/start-greta.sh && \
    echo '    export JAVA_OPTS="-Xmx${HEAP_SIZE}m -Xms$((HEAP_SIZE / 2))m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Dfile.encoding=UTF-8 -Duser.timezone=UTC"' >> /app/start-greta.sh && \
    echo 'fi' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo '# Find the main JAR file' >> /app/start-greta.sh && \
    echo 'MAIN_JAR=$(find /app -maxdepth 1 -name "greta-application-*.jar" | head -1)' >> /app/start-greta.sh && \
    echo 'if [ -z "$MAIN_JAR" ]; then' >> /app/start-greta.sh && \
    echo '    echo "ERROR: No main application JAR found"' >> /app/start-greta.sh && \
    echo '    exit 1' >> /app/start-greta.sh && \
    echo 'fi' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo 'echo "Starting Greta Platform..."' >> /app/start-greta.sh && \
    echo 'echo "Java Options: $JAVA_OPTS"' >> /app/start-greta.sh && \
    echo 'echo "Main JAR: $MAIN_JAR"' >> /app/start-greta.sh && \
    echo 'echo "Available Memory: $HEAP_SIZE MB"' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo '# Try to start the GUI application first' >> /app/start-greta.sh && \
    echo 'echo "Attempting to start GUI application..."' >> /app/start-greta.sh && \
    echo 'timeout 10 java $JAVA_OPTS -jar "$MAIN_JAR" "$@" 2>/dev/null' >> /app/start-greta.sh && \
    echo 'GUI_EXIT_CODE=$?' >> /app/start-greta.sh && \
    echo '' >> /app/start-greta.sh && \
    echo '# If GUI fails, start headless web server' >> /app/start-greta.sh && \
    echo 'if [ $GUI_EXIT_CODE -ne 0 ]; then' >> /app/start-greta.sh && \
    echo '    echo "GUI failed, starting headless web server on port 8080..."' >> /app/start-greta.sh && \
    echo '    exec java HeadlessServer' >> /app/start-greta.sh && \
    echo 'fi' >> /app/start-greta.sh
RUN chmod +x /app/start-greta.sh

# Switch to application user
USER greta

# Set optimized environment variables
ENV GRETA_HOME="/app" \
    GRETA_DATA="/app/data" \
    GRETA_LOGS="/app/logs" \
    GRETA_CONFIG="/app/config" \
    GRETA_VERSION="${GRETA_VERSION}" \
    JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom" \
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
ENTRYPOINT ["/usr/bin/tini", "--"]

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