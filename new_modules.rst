..  Copyright (c) 2014-present PlatformIO <contact@platformio.org>
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

.. _faq_convert_ino_to_cpp:

The easiest way is to copy the folder "auxiliary/SampleModule" into a new folder (for exemple "auxiliary/**MyModule**").


It contains two dummy modules that take input data from other module to forward it to the connected ones. They could for instance dynamically add data before forwarding it, or log it somwhere.

.. image:: https://user-images.githubusercontent.com/16133942/108232538-2b694980-7143-11eb-8cee-1967229b56d0.png

To interact with other pre-existing modules, you have to think at what kind of data they give you or what kind of data you want to send.
A lot of pre-existing data format exist, and it's better to re-use them. The last option can be to create you own interfaces and replicate this approach.
For each of data, you usually have an "Emitter" module that send the data to all connected "Performer" modules.


For exemple the Intention interface (https://github.com/isir/greta/blob/master/core/Intentions/src/greta/core/intentions/Intention.java) is defined in the Intentions.jar library and this two interfaces define the communication : 

* IntentionPerformer: (https://github.com/isir/greta/blob/master/core/Intentions/src/greta/core/intentions/IntentionPerformer.java)
* IntentionEmitter: (https://github.com/isir/greta/blob/master/core/Intentions/src/greta/core/intentions/IntentionEmitter.java)

In this exemple the IntentionForwarder (https://github.com/isir/greta/blob/master/auxiliary/SampleModule/src/greta/auxiliary/sampleModule/IntentionForwarder.java) module will forwarder data, so it implements both interfaces.

Add Dependecies
-------

You may need to have acces to other modules classes. For that you should add the project dependecies in NetBeans: 

* Right click on your project
* Go on Properties
* Add Dependecies : here you can add Project dependencies (suggested) or .jar dependecies

Jar files are should be located in bin/Common/Lib/ , in Internal (Greta modules) or External (external jars)

Update Modular
-------
The connections are also defined in the Modular.xml, (https://github.com/isir/greta/wiki/Modular.xml) file in the "connectors" section to allow the UI to make the arrow connection.


.. code-block:: xml

    <connector id="IntentionEmitterToIntentionPerformer">
        <input class="greta.core.intentions.IntentionEmitter" lib_id="greta_intentions"/>
        <output class="greta.core.intentions.IntentionPerformer" lib_id="greta_intentions"/>
        <connect from="input" method="addIntentionPerformer" to="output"/>    <!-- it will call input.addIntentionPerformer(output) on arrow connection -->
        <disconnect from="input" method="removeIntentionPerformer" to="output"/>    <!-- it will call input.removeIntentionPerformer(output) on arrow disconnection  -->
    </connector>


You'll need to tell Modular how to load the new Module in the [Modular.xml](https://github.com/isir/greta/wiki/Modular.xml) file.

You'll need a menu in the "menus" node : 
.. code-block:: xml

    <menu name="Sample">
	    <item module="Signal Forwarder" name="Signal Forwarder"/>
	    <item module="Intention Forwarder" name="Intention Forwarder"/>
    </menu>

Corresponding Modules in the "modules" node :

.. code-block:: xml

    <module name="Signal Forwarder" style="Signals">
	    <object class="greta.auxiliary.sampleModule.SignalForwarder" lib_id="greta_sampleModule"/>            
    </module>
    <module name="Intention Forwarder" style="Intentions">
	    <object class="greta.auxiliary.sampleModule.IntentionForwarder" lib_id="greta_sampleModule"/>                 
    </module>

And tell where to find the library : 

.. code-block:: xml

    <lib id="greta_sampleModule" path="./Common/Lib/Internal/SampleModule.jar">
	    <depends lib_id="greta_util"/>
	    <depends lib_id="greta_signals"/>
	    <depends lib_id="greta_intentions"/>			
    </lib>


