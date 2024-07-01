# Greta

**Realtime platform to control socio-emotional virtual characters**

Welcome to the public repository of the Greta project!

## Features
1) A virtual character engine generating socio-emotional behaviors for natural interactions with human users.
2) A Java modular software designed to run on Java 8 and working on Windows, Linux and OSX.
3) Greta depends on external software for Speech Synthesis ([MaryTTS](http://mary.dfki.de/) or CereProc), 3D Rendering (Ogre or Unity), and more.

For more details, please visit [WIKI](https://github.com/gretaproject/greta/wiki).

![Greta logo](https://user-images.githubusercontent.com/54807091/88184824-7c5ba280-cc33-11ea-875e-0a785d95075f.png)

https://user-images.githubusercontent.com/49474878/132516042-2bdfc9a5-8414-4589-88fd-bf3186221f43.mp4

## Quick start
1) Clone the repository to retrieve all files from the Greta Projects.
2) Greta is composed of several Java projects, you need to open them in NetBeans 8.2 IDE and to build them.
3) 2 Speech Synthesizers are compatible with Greta so far, MaryTTS and CereProc.
If you plan on using CereProc, you need to acquire a license and place the voices and their respective license files in the `<GRETA_DIR>/bin/Common/Data/CereProc/` folder. 
If you plan on using MaryTTS, you need to:
  * First download  [MaryTTS](http://mary.dfki.de/);
  * run `<MARYTTS_DIR>/marytts-5.2/bin/marytts-component-installer` and download the voices after checking in the character .ini files (go to the folder `<GRETA_DIR>/bin/Common/Data/characters`) which voices are used;
  * MaryTTS server running (`<MARYTTS_DIR>/marytts-5.2/bin/marytts-server`);
  * Check if the address and the port number of your server are the same in the file `<GRETA_DIR>/bin/Greta.ini`. If they are different, change  the port number (`MARY_PORT = `) in the file `<GRETA_DIR>/bin/Greta.ini`.
4) The main project (the runnable one) is called "Modular". You can now launch it. A blank window should appear from where you can choose File/Open to load a module configuration. 2 configurations are available, one that uses MaryTTS and another one that uses CereProc.
5) The module FML File Reader allows you to launch an FML File and see the resulting animation.


Greta is the result of different research projects conducted by Catherine Pelachaud in her research group.


## Functionality

The Greta project is divided into a public repository (this one) and a private one where future functionnalities are being developed by Catherine Pelachaud's research group.
If you use any of the resources provided on this page in any of your publications we ask you to cite the corresponding paper(s).

### Overall project
(to be completed)


The functionnalities available with the public version are the following:

### SAIBA architecture

Greta is a SAIBA compliant agent architecture, meaning that it is compatible with and can take as inputs Behavior Markup Language and Function Markup Language files in order to produce the behaviors of the agent.

Kopp, S., Krenn, B., Marsella, S., Marshall, A. N., Pelachaud, C., Pirker, H., Thórisson, K. R., & Vilhjálmsson, H. (2006, August).
Towards a Common Framework for Multimodal Generation: The Behavior Markup Language.
In International Conference on Intelligent Virtual Agents (pp. 205-217). Springer, Berlin, Heidelberg.

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

## License

Most parts of Greta are licensed under the GNU LGPL v3 (the master branch of the GitHub repository).
Some parts of Greta are licensed under the GNU GPL v3 (the gpl branch of the GitHub repository); so if you use this branch, be aware that Greta will become GNU GPL v3 at the same time.
