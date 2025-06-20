# Multi-stage Dockerfile for Greta Platform
FROM eclipse-temurin:11-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy POM files for dependency resolution
COPY pom.xml ./
COPY core/*/pom.xml core/
COPY auxiliary/*/pom.xml auxiliary/ 2>/dev/null || true
COPY application/*/pom.xml application/ 2>/dev/null || true

# Download dependencies (this layer will be cached if POMs don't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY core core
COPY auxiliary auxiliary 2>/dev/null || true
COPY application application 2>/dev/null || true

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:11-jre-alpine AS runtime

# Install required packages for GUI and graphics
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    bash \
    && rm -rf /var/cache/apk/*

# Create application user
RUN addgroup -g 1001 greta && \
    adduser -D -u 1001 -G greta greta

# Set working directory
WORKDIR /app

# Copy built artifacts from builder stage
COPY --from=builder /app/target/*.jar ./
COPY --from=builder /app/*/target/*.jar ./lib/

# Copy configuration and data files
COPY --from=builder /app/bin/Common/Data ./data/
COPY --from=builder /app/auxiliary/*/Data ./auxiliary-data/ 2>/dev/null || true

# Change ownership to application user
RUN chown -R greta:greta /app

# Switch to application user
USER greta

# Set environment variables
ENV JAVA_OPTS="-Xmx2g -Xms512m" \
    GRETA_HOME="/app" \
    GRETA_DATA="/app/data"

# Expose common ports (can be overridden)
EXPOSE 8080 61616 1883

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Default command (can be overridden)
CMD ["sh", "-c", "java $JAVA_OPTS -jar greta-application-*.jar"]

# Labels for metadata
LABEL org.opencontainers.image.title="Greta Platform" \
      org.opencontainers.image.description="Embodied Conversational Agent Platform" \
      org.opencontainers.image.source="https://github.com/isir/greta" \
      org.opencontainers.image.version="1.0.0-SNAPSHOT" \
      org.opencontainers.image.vendor="ISIR" \
      maintainer="modernization-team"