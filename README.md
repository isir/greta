
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
> - before checking each component here, ***STRONGLY RECOMMEND TO CHECK [Quick start](https://github.com/isir/greta/wiki/Quick-start) and try to run Greta first.***
> - then, check "For developpers" section in the sidebar of this wiki
> - after that, you can check each detailed functionalities from the "Functionalities" section in the sidebar of this wiki

- `application`: directory for Modular application, which connects all the modules in Greta platform
- `auxiliary`: auxiliary modules
- `bin`: compiled JAR files and non-Java programs should be in this directory. 
- `core`: core modules (if you are new, recommend to start from [BehaviorPlanner](core/BehaviorPlanner/src/greta/core/behaviorplanner/Planner.java) and [BehaviorRealizer](core/BehaviorRealizer/src/greta/core/behaviorrealizer/Realizer.java) using Netbeans IDE)
- `doc`: previously used but not maintained any more
- `pictures`: pictures which used in wiki pages. You cannot find all of the pictures used in the wiki from here because some are directory embedded into the wiki page.
- `tools`: some tool modules. Please refer [here](https://github.com/isir/greta/wiki/Technical-Specifications#name-correspondence-between-greta-modules-and-java-projects) for the included modules

## Update

### Sep 03, 2024: changed license from LGPL to GPL, added many functionalities(EN,FR)!
- Greta (master branch) became GPL license from LGPL license.
- Previous LGPL version Greta (master branch) is now in "master-lgpl" branch
- Added incremental behavior realizer module
- Added LLM module with Mistral, Mistral module for incremental processing
- Added DeepASR module, a deep learning based incremental ASR
- Added MeaningMiner module,
- Added NVBG module, a nonverbal behavior generator
- At this moment, we only support English and French languages

## Installation and usage

For instructions of how to install/compile/use the project please see [WIKI](https://github.com/gretaproject/greta/wiki)

![Greta logo](https://user-images.githubusercontent.com/54807091/88184824-7c5ba280-cc33-11ea-875e-0a785d95075f.png)

