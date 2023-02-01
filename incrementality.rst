Incrementality
=====

This module is the result of the ongoing implementation of an incremental approach in GRETA. The end goal is to give the agent the opportunity to react and adapt during a conversation, given specific event such as an interuption or an emotional response. 

Content
-----

The Incrementality package is composed of the following new modules:

* Incremental Realizer V2 : Modified version of the basic realizer that takes in a list of signals, generate keyframes and separate them into chunks (groups based on keyframe starting time). It then uses a dedicated thread to send keyframes chunks one by one according to their temporality.
* Incremental Realizer Interaction : Module that allows to interact with the realizer by sending interuption command.

How to use
-----

Add the two modules that can be found under the _Add > Incrementality: 

.. image:: https://user-images.githubusercontent.com/19309307/186421318-5f3d2d2b-d4dc-48a3-8a62-ac31d2cd21c3.png

Connect the module like so:

.. image:: https://user-images.githubusercontent.com/19309307/186934419-78d45c8c-575f-4be9-adda-3871f6d477d5.PNG

You can also use the configuration file under _File > Open > Greta - Incrementality Testing.xml

.. image:: https://raw.githubusercontent.com/sgraux/greta/incrementality/Wiki%20pictures/Config%20file.png

Once the two modules are set and linked as shown in the pictures above, you can send FML files like you would do normaly.

Build
-----

To build the project, select all the projects in the project group, use right _click > clean then right _click > build. Once all the projects are built, if you modify a project, you can clean and build only the one newly modified.

Incremental Realizer V2 Module
________

This module is a modified version of the Realizer module, used to implement the incrementality approach. It works with two parts:

* Realizer part : Transforms a Signal List sent by the Planner into a list of keyframe, forms chunks of keyframes based on their starting time and sends the list of keyframes chunk to the Thread Scheduler.
* Thread Scheduler :  Receives a list of keyframes chunk and schedule them based on their starting time, waiting between chunks to ensudre good timing.
The Incrementality realizer architecture is shown in the following picture:

.. image:: https://user-images.githubusercontent.com/19309307/186659349-6aae8f6c-fbac-462c-bcdf-26b5ec9c911c.png

Incremental Realizer Interaction module
_____

This module is used to interact with the Incremental realizer V2 module in a live setting. It features 4 buttons doing the following actions:

* Interrupt : Stop the next chunks from being sent. Note that the voice and lips movement based on voice will continue to play since they are based on a single keyframe each sent in the first chunk
* Resume : Continue sending chunk after an Interrupt command. Note that it will resume to where the execution should be at and not where you pressed the Interrupt button at. It is due to syncing later down the line.
* Stop : Fully stop the execution and return at the rest position, voice will be stopped too. Also display informations based on the button press such as its absolute time, the length of the stopped FML and the time of the stop command relative to the length of the stopped FML.
* Clear Thread Queue: Empty the chunk list and close the queue in the thread.

.. image:: https://user-images.githubusercontent.com/19309307/186660621-eb8c10a9-4b73-4070-b0f4-f998f37206b6.PNG

Chunk Details
_____

Chunks are groups of keyframes, based on keyframe offset. Chunks have a time period and contain all keyframe within that time period. For example, if you are working with chunks of two seconds, the first chunk might contain all keyframes from 0 seconds to 2 seconds, the second chunk might contain all keyframes from 2 seconds to 4 seconds, etc ... The following image shows the process in a real situation with chunks of 2 seconds:

.. image:: https://user-images.githubusercontent.com/19309307/186904448-4e3f23a4-3208-4c3d-9bd9-dc5cf67003dd.png

Dev Log
----

In this section will be briefly presented what has been tried or tested in past version of the implementation and what future tests will be. The goal is to provide a better understanding and some information for further development. 

Previous version
____

* Signal scheduler parser version 1: used to treat signals without making sure they are temporized resulting in gesture signals having a starting time of 0.00. That lead to moving the step 1 from the realizer to the Signal Scheduler. 
* Individualized threads for signal bursts: used threads to manage signal bursts resulting in each signal burst having its own thread. The use would have been to be able to synchronize sending bursts using threads. Since threads weren't vital to achieve that, they have been deprecated. 
* Saved generated keyframes to test the impact of realizer sorting: tested how the sorting on keyframes in the realizer impacted the fluidity between keyframes. Deprecated because fluidity seams to come from the treatment of signals resulting in the creation of keyframes.
* Retrieving current keyframe method: used to generate keyframe once for Previous + Current + Next bursts and once for Current alone in order to retrieve Current keyframes linked with Previous and Next. Changed to only Current keyframes and retrieving based on ID to make it easier to process and to account for possibly saved gesture signals. (cf illustration bellow)

.. image:: https://user-images.githubusercontent.com/19309307/186659713-78d44f44-6c73-4e97-bf45-a63dacb35fb0.png
.. image:: https://user-images.githubusercontent.com/19309307/186659836-04fa7394-70f8-4719-93b0-412933fb3eab.png
.. image:: https://user-images.githubusercontent.com/19309307/186659987-5cca5959-cbbf-4f63-8e55-c51fa415131e.png
