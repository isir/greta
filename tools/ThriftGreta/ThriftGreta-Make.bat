mkdir generated

thrift-0.13.0 -o generated --gen java   ThriftGreta.thrift
thrift-0.13.0 -o generated --gen csharp ThriftGreta.thrift
thrift-0.13.0 -o generated --gen cpp    ThriftGreta.thrift
