
# Greta

Welcome to the public repository of the Greta project.

Greta is a virtual character engine that allows generating realtime socio-emotional behaviors in order to build natural interactional scenario with human users.

It is the result of different research projects conducted by Catherine Pelachaud in her research group.

- Java modular software designed to run on Java 8 only and mainly on Windows, it is not ready for Java 9 yet.
- The "master" repository of Greta is licensed under the GNU GPL v3; so if you use this branch, your modification will also become GNU GPL v3.
- The "master-lgpl" of Greta is licensed under the GNU LGPL v3.
- Depends on external software for Speech Synthesis ([MaryTTS](http://mary.dfki.de/) or CereProc), and 3D Rendering (Ogre or Unity).

## Directory structure

> ATTENTION:
> - Before checking each component here, ***STRONGLY RECOMMEND TO CHECK [Quick start](https://github.com/isir/greta/wiki/Quick-start) and try to run Greta first.***
> - Then, check the "For developers" section in the sidebar of this wiki
> - After that, you can check each detailed functionality from the "Functionalities" section in the sidebar of this wiki

- `application`: directory for the Modular application, which connects all the modules in the Greta platform
- `auxiliary`: auxiliary modules
- `bin`: compiled JAR files and non-Java programs should be in this directory. 
- `core`: core modules (if you are new, recommend starting from [BehaviorPlanner](core/BehaviorPlanner/src/greta/core/behaviorplanner/Planner.java) and [BehaviorRealizer](core/BehaviorRealizer/src/greta/core/behaviorrealizer/Realizer.java) using Netbeans IDE)
- `doc`: previously used but not maintained anymore
- `pictures`: pictures that are used in wiki pages. You cannot find all the images used in the wiki from here because some are embedded directly into the wiki page.
- `tools`: some tool modules. Please refer [here](https://github.com/isir/greta/wiki/Technical-Specifications#name-correspondence-between-greta-modules-and-java-projects) for the included modules

## Update

### Jun 08, 2025: release of Greta 2.0
- Added LipBlander module and FaceBlender module
  - Greta can now run in parallel both the SAIBA-based process and the frame-by-frame generation process based on neural networks
- Added ASR (DeepGram) module
- Added LLM (Mistral) module
- Added VAP-based turn-management module
- Added VAD-based backchannel module
- Added facial expression generation based on MODIFF-8

### Sep 03, 2024: changed license from LGPL to GPL, added many functionalities(EN, FR)!
- Greta (master branch) was changed from LGPL licensed to GPL licensed.
- Previous LGPL version Greta (master branch) is now in "master-lgpl" branch
- Added incremental behavior realizer module
- Added LLM module with Mistral, Mistral module for incremental processing
- Added DeepASR module, a deep learning based incremental ASR
- Added MeaningMiner module,
- Added NVBG module, a nonverbal behavior generator
- At this moment, we only support English and French languages

## Installation and usage

For instructions on how to install/compile/use the project, please see [WIKI](https://github.com/gretaproject/greta/wiki)

![Greta logo](https://user-images.githubusercontent.com/54807091/88184824-7c5ba280-cc33-11ea-875e-0a785d95075f.png)

