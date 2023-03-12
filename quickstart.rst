Quick Start
=====

Prerequisite
------

* Java SE Development Kit 8 and NetBeans 8.2 --> https://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-3413139-esa.html
* Visual C++ Redistributable for Visual Studio 2013 (if doesn't work try with 2015):
 https://www.microsoft.com/it-it/download/details.aspx?id=40784.
* Speech synthesizer (see page [Installing the speech synthesizer](https://github.com/gretaproject/greta/wiki/Installing-the-speech-synthesizer))

Greta is a Java modular software designed to run on Java 8 working on Windows, Linux and OSX. 

Most parts of Greta are licensed under the GNU LGPL v3 (the master branch of the GitHub repository).

Some parts of Greta are licensed under the GNU GPL v3 (the gpl branch of the GitHub repository); so if you use this branch, be aware that Greta will become GNU GPL v3 at the same time.

Greta depends on external software for Speech Synthesis ([MaryTTS](http://mary.dfki.de/) or CereProc), and 3D Rendering (Ogre or Unity).

How to start
-------

Navigate to [https://github.com/gretaproject/greta](https://github.com/gretaproject/greta) and clone the repository to retrieve all files from the Greta project. In this repository you have 2 main branches: master (for the LGPL version of Greta), and gpl (for the GPL version of Greta, including GPL libraries and modules).

In the image below, at the left of the page you can find the button **Branch master**. Clicking on it you can choose the branch you want to download or just look into. Once selected the branch, to download the code, at the right you can see a green button **Clone or download**. Via the green button you can choose to **Open in Desktop** or **Download ZIP**


.. image:: https://user-images.githubusercontent.com/39828750/51596950-5f3cfb00-1efa-11e9-87ca-c2fd4016ae9b.png


Greta is composed of several **Java projects**, you need to open them in NetBeans 8.2 IDE and **to build** them.
To know how to build them, you can look into the page [Build the java projects](https://github.com/gretaproject/greta/wiki/Build-the-java-projects).

Once built you can run the **Modular.jar** from the `<GRETA_DIR>/bin/` folder to launch Greta virtual agent plaform.
This action would launch the start window

.. image:: https://user-images.githubusercontent.com/39828750/51598717-68c86200-1efe-11e9-82aa-2bd3b30512fd.png


- Then go to **File**, **Open** and choose one of the .xml configuration file to open it;

.. image:: https://user-images.githubusercontent.com/39828750/51599234-674b6980-1eff-11e9-8f32-7c1906e3c83c.png


In Modular Application (https://github.com/gretaproject/greta/wiki/Modular-application) page you will find also a tutorial on how to create a basic configuration (the video will show you all the steps).

Problem could face during platform launching and related solutions
--------------------

If you try to run one of the already existent configuration and the system crush, usually the problem is the Ogre3D. To solve the problem you should check you have installed Visual C++ Redistributable for Visual Studio 2013 (if doesn't work try with 2015): https://www.microsoft.com/it-it/download/details.aspx?id=40784 .

Once you did these you can try to run again the Modular.jar and open one of the configuration.

If the platform still crush you can open the file `<GRETA_DIR>/bin/Player/Lib/External/Win64/Configs/Plugins_OpenGL.cfg` and comment the line where you have `Plugin=Plugin_CgProgramManager` (to comment the line you just have to put # at the beginning of the row).
