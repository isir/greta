How to use NetBeans with Greta
====
This page is dedicated to developers to install, compile and make development on Greta Project.
NetBeans use many templates to help developers to code easily. But its default managment is to simplistic for the development of Greta. That's why we must take care on some stuff when we create a new NetBeans project in Greta.

How to install Greta and compile it with NetBeans
-----------
Installation
________

* Install Greta using git clone https://github.com/isir/greta/new/gpl-grimaldi or download zip
* Install Java 64 bits http://java.com/fr/download/manual.jsp
* Install NetBeans https://netbeans.org/downloads/ version all

Configuration files modification
____________

* Open <GRETA_DIR>/bin/Player/Lib/External/Win64/Plugins_DX9.cfg and <GRETA_DIR>/bin/Player/Lib/External/Win64/Plugins_OpenGL.cfg
* Change "PluginFolder" value to absolute path

NetBeans configuration
_________
* Open NetBeans and right click on project space (left of NetBeans window)
* Select Project Group/New Group and choose the name that you want to give to your project
* Select Folder of Projects and browse in the directory (previously get from SVN) /trunk/VIB
.. image:: http://greta.isir.upmc.fr/images/2/29/GretaNetbeans.JPG 
* Compile the project All_Javadoc by clicking right on it
* Do the same for compiling Modular

Set your correct @author tag
_________
Make this step only once.

* In the NetBeans menu, choose "Tools" -> "Templates" to open the template manager
* Click on "Settings" button. It opens the User.properties file.
* Add the line with you name using the correct case user=Firstname Lastname

Template to include the license in the code
__________
Make this step only once.
* In the NetBeans menu, choose "Tools" -> "Templates" to open the template manager
* select the "Licenses" folder and clic on "Add..." button
* choose the file  <GRETA_DIR>/license-greta.txt
* select the "Licenses" folder and clic on "Add..." button
* choose the file <GRETA_DIR>/license-greta-auxiliary.txt
If you don't do it, you will have some (benign) error when creating a new class.

Create a library
_______

* create a new project with NetBeans, and choose "Java Class Library" then choose a good name and path
* edit the file <myProject>/build.xml 
* insert (and check relative path !) :

.. code-block:: xml

  import file="../../nbbuild.xml"
  
it must be added before the line

.. code-block:: xml

  import file="nbproject/build-impl.xml"
  
* edit the file <myProject>/nbproject/project.properties and add lines :

.. code-block:: ini

  compile.on.save=true
  project.license=greta
  
* change the values (and check relative path!):

.. code-block:: ini

  application.vendor=Catherine Pelachaud
  dist.dir=../../bin/Common/Lib/Internal
  javac.source=1.8
  javac.target=1.8

first: because it's Catherine's. second : set the final destination of the jar .
If your version of NetBeans is lower than 7.x, you must add the line : mkdist.disabled=true

* save the two files
* shut down NetBeans and restart it

Create an executable
_______
It's almost the same:

* create a new project with NetBeans, and choose "Java Application" then choose a good name and path
* edit the file <myProject>/build.xml
* insert (and check relative path !)

.. code-block:: xml

  import file="../../nbbuild.xml"
  
it must be added before the line:

.. code-block:: xml

  import file="nbproject/build-impl.xml"
  
* edit the file <myProject>/nbproject/project.properties and add lines :

.. code-block:: ini

  compile.on.save=true
  work.dir=../../bin
  project.license=greta

change the values (and check relative path !):

.. code-block:: ini

  application.vendor=Catherine Pelachaud
  dist.dir=../../bin
  javac.source=1.8
  javac.target=1.8

first: because it's Catherine's. second : set the final destination of the jar
* save the two files
* shut down NetBeans and restart it

Link an existing library to a project
_________
* right clic on the project name in NetBeans and select "Properties"
* on the pop up window, choose "Libraries"
* choose the "Compile" tab if it is not already the case
* to add an existing Greta projet :
* clic on "Add Project..." button
* choose the good one
* check if the corresponding jar is in a good folder (<GRETA_DIR>/bin/Common/Lib/Internal for libraries, <GRETA_DIR>/bin for executables)

To add an external jar :

* make sure that this one is in <GRETA_DIR>/bin/Common/Lib/External/ folder
* click on "Add JAR/Folder" button
* choose the good one (in the good folder)
* before validating you choice, select "Relative Path"
