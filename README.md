# Greta
Realtime platform to control socio-emotional virtual characters 

![Greta logo](https://github.com/gretaproject/Greta/blob/master/pictures/Greta_hello.png)

Welcome to the public repository of the Greta project.
Greta is a virtual character engine that allows generating socio-emotional behaviors in order to build natural interactional scenario with human users.

Greta is a Java modular software designed to run on Java 1.8 only and mainly on Windows, it is not ready for Java 1.9 yet.
All parts of Greta/VIB are licensed under the GNU GPL v3.

Greta depends on external software for Speech Synthesis, Cereproc or [OpenMary](http://mary.dfki.de/), and 3D Rendering, Ogre or Unity.
Therefore, some configuration is required once you retrieve the source files of the project before being able to start it.

# Quick start
1) Clone the repository to retrieve all files from the Greta Projects.
2) Greta is composed of several Java projects, you need to open them in your favorite Java IDE and to build them.
3) Go to the folder "\bin\Player\Lib\External\{your platform}\" . You need to edit the plugins configuration file and change the PluginFolder variable so it uses the absolute path (starting from "C:/...") instead of the relative path. By default (you can edit this in the configuration file vib.ini), Greta uses openGL so you should edit the Plugins_OpenGL.cfg.
4) 2 Speech Synthesizers are compatible with Greta so far, OpenMary and Cereproc. If you plan on using Cereproc, you need to acquire a license and place the voices and their respective license files in the "bin\Common\Data\Cereproc" folder. If you plan on using OpenMary, you need to have an OpenMary server running. You can change the address and the port number of your server in the file "vib.ini". You can check the character files in "bin\Common\Data\characters" to verify which voices are needed and used.
4) The main project (the runnable one) is called "Modular". You can now launch it. A blank window should appear from where you can choose File/Open to load a module configuration. 2 configurations are available, one that uses OpenMary and another one that uses Cereproc.
5) The module FML File Reader allows you to launch an FML File and see the resulting animation.



Greta is the result of different research projects conducted by Catherine Pelachaud in her research group.

## WIKI

**For instructions of how to install/compile/use the project please see [WIKI](https://github.com/gretaproject/greta/wiki)**

## Functionality

The Greta project is divided into a public repository (this one) and a private one where future functionnalities are being developed by Catherine Pelachaud's research group.
If you use any of the resources provided on this page in any of your publications we ask you to cite the corresponding paper(s).

### Overall project
(to be completed)


The functionnalities available with the public version are the following:

### SAIBA architecture

Greta is a SAIBA compliant agent architecture, meaning that it is compatible with and can take as inputs Behavior Markup Language and Function Markup Language files in order to produce the behaviors of the agent.

Mancini, M., Niewiadomski, R., Bevacqua, E., & Pelachaud, C. (2008, November).
Greta: a SAIBA compliant ECA system.
In Troisième Workshop sur les Agents Conversationnels Animés.
 
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