/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 *  Marshaler code for OpenWire format for PartialCommand
 *
 *  NOTE!: This file is auto generated - do not modify!
 *         if you need to make a change, please see the Java Classes
 *         in the nms-activemq-openwire-generator module
 *
 */

using System;
using System.IO;

using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.OpenWire.V10
{
    /// <summary>
    ///  Marshalling code for Open Wire Format for PartialCommand
    /// </summary>
    class PartialCommandMarshaller : BaseDataStreamMarshaller
    {
        /// <summery>
        ///  Creates an instance of the Object that this marshaller handles.
        /// </summery>
        public override DataStructure CreateObject() 
        {
            return new PartialCommand();
        }

        /// <summery>
        ///  Returns the type code for the Object that this Marshaller handles..
        /// </summery>
        public override byte GetDataStructureType() 
        {
            return PartialCommand.ID_PARTIALCOMMAND;
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void TightUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn, BooleanStream bs) 
        {
            base.TightUnmarshal(wireFormat, o, dataIn, bs);

            PartialCommand info = (PartialCommand)o;
            info.CommandId = dataIn.ReadInt32();
            info.Data = ReadBytes(dataIn, bs.ReadBoolean());
        }

        //
        // Write the booleans that this object uses to a BooleanStream
        //
        public override int TightMarshal1(OpenWireFormat wireFormat, Object o, BooleanStream bs)
        {
            PartialCommand info = (PartialCommand)o;

            int rc = base.TightMarshal1(wireFormat, o, bs);
            bs.WriteBoolean(info.Data!=null);
            rc += info.Data==null ? 0 : info.Data.Length+4;

            return rc + 4;
        }

        // 
        // Write a object instance to data output stream
        //
        public override void TightMarshal2(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut, BooleanStream bs)
        {
            base.TightMarshal2(wireFormat, o, dataOut, bs);

            PartialCommand info = (PartialCommand)o;
            dataOut.Write(info.CommandId);
            if(bs.ReadBoolean()) {
                dataOut.Write(info.Data.Length);
                dataOut.Write(info.Data);
            }
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void LooseUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn) 
        {
            base.LooseUnmarshal(wireFormat, o, dataIn);

            PartialCommand info = (PartialCommand)o;
            info.CommandId = dataIn.ReadInt32();
            info.Data = ReadBytes(dataIn, dataIn.ReadBoolean());
        }

        // 
        // Write a object instance to data output stream
        //
        public override void LooseMarshal(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut)
        {

            PartialCommand info = (PartialCommand)o;

            base.LooseMarshal(wireFormat, o, dataOut);
            dataOut.Write(info.CommandId);
            dataOut.Write(info.Data!=null);
            if(info.Data!=null) {
               dataOut.Write(info.Data.Length);
               dataOut.Write(info.Data);
            }
        }
    }
}
