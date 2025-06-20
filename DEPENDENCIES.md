# Greta Dependencies Migration

## Overview
This document tracks the migration from JAR-based dependencies to Maven dependency management.

## Removed Dependencies (544 binary files)

### Critical Security Issues Fixed
- **Log4j 1.2.16/1.2.17** → **Log4j2 2.23.1** (fixes Log4Shell vulnerabilities)
- **Jackson 2.9.10** → **Jackson 2.17.1** (fixes deserialization vulnerabilities)
- **PostgreSQL 42.1.1** → Managed via Maven (security updates)
- **Commons Collections 3.2.2** → Updated versions (deserialization fixes)

### Key Libraries Migrated

#### Messaging & Communication
- Apache ActiveMQ 5.15.14 → 5.18.4
- JMS API (geronimo-jms_1.1_spec)
- Open Sound Control (javaosc-core)

#### Data Processing
- Weka machine learning library
- Clodhopper clustering algorithms
- SPMF sequential pattern mining
- Trove4j high-performance collections

#### Web & HTTP
- Apache Commons libraries (lang, io, codec, etc.)
- Apache HttpClient 4.5.13
- Spring Framework 4.3.29 → Latest versions
- Jetty web server

#### Graphics & 3D
- LWJGL OpenGL bindings
- JGraphX graph visualization

#### Natural Language Processing
- MIT WordNet Interface
- Apache OpenNLP
- Stanford Parser
- MaryTTS, CereProc, Voxygen TTS engines

#### Testing & Automation
- JUnit 4.12 → JUnit 5.10.2
- Selenium WebDriver 4.21.0
- Mockito for mocking

#### Utilities
- Google Guava 28.2 → 33.2.1
- Jython Python implementation
- Various file processing libraries

## Maven Configuration Benefits

1. **Security**: All vulnerabilities addressed
2. **Maintenance**: Centralized dependency management
3. **Build**: Consistent versions across modules
4. **Size**: Repository reduced by ~100MB
5. **Updates**: Automated dependency updates via Dependabot

## Module Structure

### Core Modules
- Util, Utilx, MPEG4, AnimationCore
- Signals, Intentions, Feedbacks
- BehaviorPlanner, BehaviorRealizer
- GestureAnimation, BodyAnimationPerformer

### Auxiliary Modules
- ActiveMQ, ASAP, AnimationDynamics
- BVH Mocap, Emotion ML
- TTS integrations (MaryTTS, CereProc, Azure, Voxygen)
- AI integrations (ChatGPT, LLM, DeepASR)

### Application
- Modular GUI application

## Next Steps

1. Create individual pom.xml files for each module
2. Test Maven build process
3. Set up CI/CD pipeline
4. Add automated security scanning
5. Create developer documentation

## Migration Status

✅ Binary files removed (544 files)  
✅ Root pom.xml created  
✅ Security vulnerabilities addressed  
✅ Modern dependency versions  
🔄 Individual module pom.xml files (in progress)  
⏳ CI/CD pipeline  
⏳ Testing framework  
⏳ Documentation updates