Openface integration
=====

OpenFace 2, developed at the CMU MultiComp Lab, is a facial behavior analysis toolkit capable of real-time performance.
The system is capable of performing a number of facial analysis tasks:
 
 * Facial Landmark Detection
 * Facial Landmark and head pose tracking (links to YouTube videos)
 * Facial Action Unit Recognition
 * Gaze tracking (image of it in action)
 * Facial Feature Extraction (aligned faces and HOG features)

It can forward all facial expressions, head pose, and gaze to any other software

.. image:: https://user-images.githubusercontent.com/16133942/88185323-191e4000-cc34-11ea-8973-4d1600574de7.png
.. image:: https://user-images.githubusercontent.com/16133942/98125771-1e3a8e80-1eb5-11eb-9500-218b6d23b617.png)

It can either :

 * read the standard output file from the standard OpenFace2 program. Reading is dynamic, synchronous with OpenFace writing.
 * connect via ZeroMQ (https://zeromq.org/) protocol on port 5000. Using the OpenFaceOfflineZeroMQ application (built with this branch of OpenFace 2 (https://github.com/isir/OpenFace/tree/feature/streaming-AU-ZeroMQ))


.. image:: https://user-images.githubusercontent.com/16133942/98126357-c94b4800-1eb5-11eb-9b6c-24bf8d3ad8e8.png

In Greta use "OpenFace2 Output Stream Reader" module to listen to OpenFace inputs and connect them to the specific Greta modules handling AUs and headpose.

.. image:: https://user-images.githubusercontent.com/16133942/98126601-10393d80-1eb6-11eb-8fea-75c6a66e8925.png

The module allows :

1) OpenFace data Input selection
2 Signal processing
3) To forward any information to a debug application using the OSC protocol

OpenFace data Input selection
________________
The UI allows dynamic and easy selection of the OpenFace 2 information. It can be used to use only a specific set of AUs.

.. image:: https://user-images.githubusercontent.com/16133942/98127775-5d69df00-1eb7-11eb-817b-1f91ce252bfb.png

Signal processing
_________

A Facial action unit is composed of:

 *a continuous signal : how much is feature is activated
 * a discrete signal : is feature detected or not 
 
Raw signal :

.. image:: https://user-images.githubusercontent.com/16133942/98127984-9f932080-1eb7-11eb-8947-f223e8ba69fa.png

Mask :

.. image:: https://user-images.githubusercontent.com/16133942/98128060-b89bd180-1eb7-11eb-865d-715739e1df41.png

Hence the need to filter the signal processing filter.

Filtered signal (kernel size of 5, weight function with power 0.5) :

.. image:: https://user-images.githubusercontent.com/16133942/98128124-cb160b00-1eb7-11eb-952e-3766b4239c44.png

A dynamically sized kernel processing approach where the most recent signal value is the last index of the kernel. 
Each kernel weight is valued with the mathematical “pow” function which conveniently grows from 0 to 1 for x =[0-1]. 
So the most recent values have the most weight.

.. image:: https://user-images.githubusercontent.com/16133942/98128303-0d3f4c80-1eb8-11eb-997e-9b3c1527baee.png
12 normalized kernel values for different power values

Demonstration videos :

* `AU4-brow Lowerer <https://cloud.isir.upmc.fr/owncloud/index.php/s/mawOTdo7JgWmgym>`_
* `AU2-outer brow raiser <https://cloud.isir.upmc.fr/owncloud/index.php/s/JKIsFWU4g1zer4e>`_
* `AU26-jaw drop <https://cloud.isir.upmc.fr/owncloud/index.php/s/yWE3OrSDdi68yHt>`_
