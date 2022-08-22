openSMILE
=========

**Note:** this is the internal repository of openSMILE that includes proprietary, non-public
components. You can find the open-source repository and official releases on 
[GitHub](https://github.com/audeering/opensmile).

See also the standalone [opensmile](https://github.com/audeering/opensmile-python)
Python package for an easy-to-use wrapper if you are working in Python.

Quick start
-----------

For more details on how to customize builds, build for other platforms and use
openSMILE, see Section [Get started](http://tools.pp.audeering.com/opensmile/get-started.html)
in the documentation.

### Linux/MacOS

Prerequisites:
- A version of gcc and g++ or Clang need to be installed that supports C++11.
- CMake 3.5.1 or later needs to be installed and in the PATH.

1. In ``build_flags.sh``, set build flags and options as desired.
2. Run ``bash build.sh``.

Build files will be generated in the ``./build`` subdirectory.
You can find the main SMILExtract binary in ``./build/progsrc/smilextract``.

### Windows

Prerequisites:
- Visual Studio 2017 or higher with C++ components is required.
- CMake 3.15 or later needs to be installed and in the PATH.

1. In ``build_flags.ps1``, set build flags and options as desired.
2. Run ``powershell -ExecutionPolicy Bypass -File build.ps1``.

Build files will be generated in the ``./build`` subdirectory.
You can find the main SMILExtract.exe binary in ``./build/progsrc/smilextract``.

Documentation
-------------

You can find extensive documentation with step-by-step instructions on how to build 
openSMILE and get started at http://tools.pp.audeering.com/opensmile/.
