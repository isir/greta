
Thrift usage in Greta:

"ThriftGreta".thrift is a script file describing the variables (for a list of basic types used in thrift see https://thrift.apache.org/docs/types ), structures, classes and functions that will be created by Thrift in the language chosen (see https://thrift.apache.org/docs/Languages to see the list of languages).
You can get information on "ThriftGreta".thrift writting on https://thrift.apache.org/docs/idl

"thrift-0.13.0".exe is the cpp executable that will create these variables, structures, classes and functions in the language chosen.
You can generate it from the Thrift source code
    https://thrift.apache.org/download : thrift-0.13.0.tar.gz
or directly get the executable thrift compiler for windows
    https://thrift.apache.org/download : thrift-0.13.0.exe

Here we have get the windows compiler.

To generate the variables, structures, classes and functions described in "ThriftGreta".thrift in the language chosen, here Java, in a command shell, write
thrift-0.13.0 --gen "language" "ThriftGreta".thrift
here we write exactly: thrift-0.13.0 --gen java ThriftGreta.thrift

New classes are auto-generated in gen-"language"/"package name chosen in "ThriftGreta".thrift"".
In Greta project let this package vib.auxiliary.thrift.gen_"language" !
