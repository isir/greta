# Greta
Model of nonverbal behavior for socio-emotional virtual characters 

Welcome to the public repository of the Greta project. Greta is a virtual character engine that allows its user to generate socio-emotional behaviors in order to build natural interactional scenario with users.

(Greta is a Java modular software designed to run on Java 1.8 only and mainly on Windows, it is not ready for Java 1.9 yet.)

Greta depends on external software for Speech Synthesis, Cereproc or OpenMary, and 3D Rendering, Ogre or Unity. Therefore, a little configuration is required once you retrieve the source files of the project before being able to start it.

# Quick start
1) Clone the repository to retrieve all files from the Greta Projects.
2) Greta is composed of several Java projects, you need to open them in your favorite Java IDE and to build them.
3) Go to the folder "\bin\Player\Lib\External\{your platform}\" . You need to edit the plugins configuration file and change the PluginFolder variable so it uses the absolute path (starting from "C:/...") instead of the relative path. By default (you can edit this in the configuration file vib.ini), Greta uses openGL so you should edit the Plugins_OpenGL.cfg.
4) 2 Speech Synthesizer are compatible with Greta so far, OpenMary and Cereproc. If you plan on using Cereproc, you need to acquire a license and place the voices and their respective license files in the "bin\Common\Data\Cereproc" folder. If you plan on using OpenMary, you need to have an OpenMary server running. You can change the address and the port number of your server in the file "vib.ini". You can check the character files in "bin\Common\Data\characters" to verify which voices are needed and used.
4) The main project (the runnable one) is called "Modular". You can now launch it. A blank window should appear from where you can choose File/Open to load a module configuration. 2 configurations are available, one that uses OpenMary and another that uses Cereproc.
5) The module FML File Reader allows you to launch an FML File and see the resulting animation.
