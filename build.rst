
Build Projects
=====

Greta is composed of several **Java projects**, you need to open them in your favorite Java IDE and **to build** them.
In this page we propose two different ways to build the java projects:

* via NetBeans
* via build script

The easiest solution for the general user can be via script, but if you are using the platform and you want to develop other modules it is suggested to use NetBeans.

Build via NetBeans
------

In order to build the projects, open NetBeans and select File --> Project Groups... --> New Group... -->Folder of Project --> Browse... to find and open the folder just downloaded from GitHub --> Create a Group.

Once open all the projects, select all of them and with the right button of the mouse select Build

.. image:: https://user-images.githubusercontent.com/39828750/52046882-1fa08f80-2548-11e9-96ec-f9b676778f57.png


Now you are ready to run the Modular application.

Troubleshooting
________

If you have the correct version of JDK but can't build the project, you might have to specify to NetBeans wich version of JDK you want to use. To do so, go to `C:/Program Files/NetBeans-12.6/netbeans/etc/netbeans.conf`, and verify the version of JAVA.

JDK Default version in NetBeans.conf: 

.. image:: https://user-images.githubusercontent.com/19309307/147920187-dc4f8028-ba79-46b5-9e8e-c97a0a46c772.png

Build via script
-------

ANT Installation and Setup (in Windows)
________

You can download ANT from [https://ant.apache.org/](https://ant.apache.org/). Download the binary distribution, unzip it, and move it to a directory of your choice. After that, set these environment variables (if don't know how to do it, you can try to see this example video: https://www.youtube.com/watch?v=83SccoBYSfA ) all in the user variables section:
1. ANT_HOME: \<the-unzipped-ANT-directory\>
2. Path: add the \<the-unzipped-ANT-directory\\bin\>
3. JAVA_HOME: \<your-default-JDK-directory\> (you might already have this set)

You are ready to use ANT! (don't forget to close the existing command line windows, though)

Running the ANT Build Scripts
__________

To build from a clean state, open the command prompt, go to the `<GRETA_DIR>/` directory on your pc and run this command: `ant build`

Running the ANT Clean Scripts
_________

To clean from a compiled state, open the command prompt, go to the `<GRETA_DIR>/` directory on your pc and run this command: `ant clean`
