
# Greta

Welcome to the public repository of the Greta project.

Greta is a virtual character engine that allows generating realtime socio-emotional behaviors in order to build natural interactional scenario with human users.

It is the result of different research projects conducted by Catherine Pelachaud in her research group.

- Java modular software designed to run on Java 8 only and mainly on Windows, it is not ready for Java 9 yet.
- The "master" repository of Greta is licensed under the GNU GPL v3; so if you use this branch, your modification will also become GNU GPL v3.
- The "master-lgpl" of Greta is licensed under the GNU LGPL v3.
- Depends on external software for Speech Synthesis ([MaryTTS](http://mary.dfki.de/) or CereProc), and 3D Rendering (Ogre or Unity).

## Update

### Sep 03, 2024: changed license from LGPL to GPL, added many functionalities(EN,FR)!
- Greta (master branch) became GPL license from LGPL license.
- Previous LGPL version Greta (master branch) is now in "master-lgpl" branch
- Added incremental behavior realizer module (but unstable yet)
- Added LLM module with Mistral, Mistral module for incremental processing
- Added DeepASR module, a deep learning based incremental ASR
- Added MeaningMiner module,
- Added NVBG module, a nonverbal behavior generator
- At this moment, we only support English and French languages

![Greta logo](https://user-images.githubusercontent.com/54807091/88184824-7c5ba280-cc33-11ea-875e-0a785d95075f.png)

## Installation and usage

For instructions of how to install/compile/use the project please see [WIKI](https://github.com/gretaproject/greta/wiki)

## Functionality

The Greta project is divided into a public repository (this one) and a private one where future functionnalities are being developed by Catherine Pelachaud's research group.
If you use any of the resources provided on this page in any of your publications we ask you to cite the corresponding paper(s).

### SAIBA architecture

Greta is a SAIBA compliant agent architecture, meaning that it is compatible with and can take as inputs Behavior Markup Language and Function Markup Language files in order to produce the behaviors of the agent.

Mancini, M., Niewiadomski, R., Bevacqua, E., & Pelachaud, C. (2008, November).
Greta: a SAIBA compliant ECA system.
In Troisième Workshop sur les Agents Conversationnels Animés.

Kopp, Stefan, Brigitte Krenn, Stacy Marsella, Andrew N. Marshall, Catherine Pelachaud, Hannes Pirker, Kristinn R. Thórisson, and Hannes Vilhjálmsson. (2006) Towards a Common Framework for Multimodal Generation: The Behavior Markup Language. In Lecture Notes of Computer Science. Berlin, Heidelberg: Springer Berlin Heidelberg.
 
### MPEG4 animation

Greta animation engine produces MPEG4 compatible animation. The system outputs frame of Body Animation Parameters and Facial Animation Parameters.
The Greta platform is also capable of taking as inputs these animation parameters to render the animation on a virtual character using Ogre3D or Unity3D.

Niewiadomski, R., Bevacqua, E., Mancini, M., & Pelachaud, C. (2009, May).
Greta: an interactive expressive ECA system.
In Proceedings of The 8th International Conference on Autonomous Agents and Multiagent Systems-Volume 2 (pp. 1399-1400).
International Foundation for Autonomous Agents and Multiagent Systems.

### Listener Backchannels

An important part of human communication is performed by the subtle behaviors one produces when listening. These behaviors, called backchannels, include head nods and other small sounds like "hmm hmm".
Greta provides a system to configure and use these behaviors in an automatic fashion in order to design more life-like interaction.

Bevacqua, E., Pammi, S., Hyniewska, S. J., Schröder, M., & Pelachaud, C. (2010, September).
Multimodal backchannels for embodied conversational agents.
In International Conference on Intelligent Virtual Agents (pp. 194-200). Springer, Berlin, Heidelberg.
