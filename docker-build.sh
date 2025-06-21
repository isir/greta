#!/bin/bash

# Docker Build Script for Greta Platform
# Optimized for caching, multi-platform builds, and CI/CD integration

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Configuration
IMAGE_NAME="greta"
REGISTRY="${DOCKER_REGISTRY:-}"
TAG="${DOCKER_TAG:-latest}"
BUILD_ARGS=""
PLATFORMS="${DOCKER_PLATFORMS:-linux/amd64}"
CACHE_FROM=""
PUSH_IMAGE=false
BUILD_PROD=false
QUIET=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --registry)
            REGISTRY="$2"
            shift 2
            ;;
        --tag)
            TAG="$2"
            shift 2
            ;;
        --platforms)
            PLATFORMS="$2"
            shift 2
            ;;
        --push)
            PUSH_IMAGE=true
            shift
            ;;
        --prod)
            BUILD_PROD=true
            shift
            ;;
        --quiet)
            QUIET=true
            shift
            ;;
        --cache-from)
            CACHE_FROM="$2"
            shift 2
            ;;
        --build-arg)
            BUILD_ARGS="$BUILD_ARGS --build-arg $2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --registry REGISTRY    Docker registry (optional)"
            echo "  --tag TAG             Image tag (default: latest)"
            echo "  --platforms PLATFORMS Comma-separated platforms (default: linux/amd64)"
            echo "  --push                Push image to registry"
            echo "  --prod                Use production build configuration"
            echo "  --quiet               Suppress verbose output"
            echo "  --cache-from IMAGE    Use image for cache"
            echo "  --build-arg ARG=VAL   Pass build argument"
            echo "  --help                Show this help message"
            exit 0
            ;;
        *)
            print_status $RED "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Setup full image name
FULL_IMAGE_NAME="$IMAGE_NAME:$TAG"
if [ -n "$REGISTRY" ]; then
    FULL_IMAGE_NAME="$REGISTRY/$FULL_IMAGE_NAME"
fi

# Production build configuration
if [ "$BUILD_PROD" = true ]; then
    BUILD_ARGS="$BUILD_ARGS --build-arg MAVEN_OPTS='-Dmaven.repo.local=/root/.m2/repository -Xmx2048m'"
    BUILD_ARGS="$BUILD_ARGS --build-arg JAVA_BUILD_OPTS='-XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat'"
    BUILD_ARGS="$BUILD_ARGS --build-arg GRETA_VERSION=1.0.0-SNAPSHOT"
fi

# Setup cache configuration
if [ -n "$CACHE_FROM" ]; then
    CACHE_FROM="--cache-from $CACHE_FROM"
fi

# Welcome message
if [ "$QUIET" != true ]; then
    clear
    print_status $PURPLE "🐳 Greta Platform Docker Build"
    print_status $PURPLE "============================="
    echo ""
    print_status $BLUE "Image: $FULL_IMAGE_NAME"
    print_status $BLUE "Platforms: $PLATFORMS"
    print_status $BLUE "Production: $BUILD_PROD"
    print_status $BLUE "Push: $PUSH_IMAGE"
    echo ""
fi

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_status $RED "❌ Docker is not installed or not in PATH"
    exit 1
fi

# Check if buildx is available for multi-platform builds
if [[ "$PLATFORMS" == *","* ]] && ! docker buildx version &> /dev/null; then
    print_status $RED "❌ Docker Buildx is required for multi-platform builds"
    exit 1
fi

# Create builder instance if needed for multi-platform
if [[ "$PLATFORMS" == *","* ]]; then
    if [ "$QUIET" != true ]; then
        print_status $YELLOW "🔧 Setting up multi-platform builder..."
    fi
    
    docker buildx create --name greta-builder --use 2>/dev/null || true
    docker buildx inspect --bootstrap
fi

# Build command construction
BUILD_CMD="docker"
if [[ "$PLATFORMS" == *","* ]]; then
    BUILD_CMD="$BUILD_CMD buildx build --platform $PLATFORMS"
else
    BUILD_CMD="$BUILD_CMD build"
fi

BUILD_CMD="$BUILD_CMD -t $FULL_IMAGE_NAME"
BUILD_CMD="$BUILD_CMD $BUILD_ARGS"
BUILD_CMD="$BUILD_CMD $CACHE_FROM"

if [ "$PUSH_IMAGE" = true ]; then
    BUILD_CMD="$BUILD_CMD --push"
fi

if [ "$QUIET" = true ]; then
    BUILD_CMD="$BUILD_CMD --quiet"
fi

BUILD_CMD="$BUILD_CMD ."

# Pre-build checks
if [ "$QUIET" != true ]; then
    print_status $YELLOW "🔍 Running pre-build checks..."
fi

# Check if Dockerfile exists
if [ ! -f "Dockerfile" ]; then
    print_status $RED "❌ Dockerfile not found in current directory"
    exit 1
fi

# Check Maven wrapper
if [ ! -f "mvnw" ]; then
    print_status $RED "❌ Maven wrapper not found"
    exit 1
fi

# Check if .dockerignore exists
if [ ! -f ".dockerignore" ]; then
    print_status $YELLOW "⚠️  .dockerignore not found - build context may be large"
fi

# Build the image
if [ "$QUIET" != true ]; then
    print_status $YELLOW "🏗️  Building Docker image..."
    print_status $BLUE "Command: $BUILD_CMD"
    echo ""
fi

# Execute build with timing
START_TIME=$(date +%s)

if eval $BUILD_CMD; then
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    if [ "$QUIET" != true ]; then
        print_status $GREEN "✅ Build completed successfully in ${DURATION}s"
        
        # Show image information
        if [ "$PUSH_IMAGE" != true ]; then
            echo ""
            print_status $BLUE "📊 Image Information:"
            docker images | grep "$IMAGE_NAME" | head -1
            
            # Show image size
            IMAGE_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$FULL_IMAGE_NAME" | awk '{print $2}')
            if [ -n "$IMAGE_SIZE" ]; then
                print_status $BLUE "Size: $IMAGE_SIZE"
            fi
        fi
    fi
else
    print_status $RED "❌ Build failed"
    exit 1
fi

# Security scan (if available)
if command -v docker &> /dev/null && [ "$PUSH_IMAGE" != true ] && [ "$QUIET" != true ]; then
    print_status $YELLOW "🔒 Running security scan..."
    
    if docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
       -v $(pwd):/app aquasec/trivy:latest image --exit-code 1 --severity HIGH,CRITICAL "$FULL_IMAGE_NAME" 2>/dev/null; then
        print_status $GREEN "✅ Security scan passed"
    else
        print_status $YELLOW "⚠️  Security scan found issues (run manually for details)"
    fi
fi

# Cleanup builder if created
if [[ "$PLATFORMS" == *","* ]]; then
    docker buildx rm greta-builder 2>/dev/null || true
fi

# Final summary
if [ "$QUIET" != true ]; then
    echo ""
    print_status $PURPLE "🎉 Docker Build Complete!"
    print_status $GREEN "Image: $FULL_IMAGE_NAME"
    
    if [ "$PUSH_IMAGE" = true ]; then
        print_status $GREEN "✅ Image pushed to registry"
    else
        print_status $BLUE "💡 To push: docker push $FULL_IMAGE_NAME"
        print_status $BLUE "💡 To run: docker run -p 8080:8080 $FULL_IMAGE_NAME"
    fi
fi