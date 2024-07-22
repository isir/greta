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
 *  Marshaler code for OpenWire format for WireFormatInfo
 *
 *  NOTE!: This file is auto generated - do not modify!
 *         if you need to make a change, please see the Java Classes
 *         in the nms-activemq-openwire-generator module
 *
 */

using System;
using System.IO;

using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.OpenWire.V6
{
    /// <summary>
    ///  Marshalling code for Open Wire Format for WireFormatInfo
    /// </summary>
    class WireFormatInfoMarshaller : BaseDataStreamMarshaller
    {
        /// <summery>
        ///  Creates an instance of the Object that this marshaller handles.
        /// </summery>
        public override DataStructure CreateObject() 
        {
            return new WireFormatInfo();
        }

        /// <summery>
        ///  Returns the type code for the Object that this Marshaller handles..
        /// </summery>
        public override byte GetDataStructureType() 
        {
            return WireFormatInfo.ID_WIREFORMATINFO;
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void TightUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn, BooleanStream bs) 
        {
            base.TightUnmarshal(wireFormat, o, dataIn, bs);

            WireFormatInfo info = (WireFormatInfo)o;

            info.BeforeUnmarshall(wireFormat);

            info.Magic = ReadBytes(dataIn, 8);
            info.Version = dataIn.ReadInt32();
            info.MarshalledProperties = ReadBytes(dataIn, bs.ReadBoolean());

            info.AfterUnmarshall(wireFormat);
        }

        //
        // Write the booleans that this object uses to a BooleanStream
        //
        public override int TightMarshal1(OpenWireFormat wireFormat, Object o, BooleanStream bs)
        {
            WireFormatInfo info = (WireFormatInfo)o;

            info.BeforeMarshall(wireFormat);

            int rc = base.TightMarshal1(wireFormat, o, bs);
            bs.WriteBoolean(info.MarshalledProperties!=null);
            rc += info.MarshalledProperties==null ? 0 : info.MarshalledProperties.Length+4;

            return rc + 12;
        }

        // 
        // Write a object instance to data output stream
        //
        public override void TightMarshal2(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut, BooleanStream bs)
        {
            base.TightMarshal2(wireFormat, o, dataOut, bs);

            WireFormatInfo info = (WireFormatInfo)o;
            dataOut.Write(info.Magic, 0, 8);
            dataOut.Write(info.Version);
            if(bs.ReadBoolean()) {
                dataOut.Write(info.MarshalledProperties.Length);
                dataOut.Write(info.MarshalledProperties);
            }

            info.AfterMarshall(wireFormat);
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void LooseUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn) 
        {
            base.LooseUnmarshal(wireFormat, o, dataIn);

            WireFormatInfo info = (WireFormatInfo)o;

            info.BeforeUnmarshall(wireFormat);

            info.Magic = ReadBytes(dataIn, 8);
            info.Version = dataIn.ReadInt32();
            info.MarshalledProperties = ReadBytes(dataIn, dataIn.ReadBoolean());

            info.AfterUnmarshall(wireFormat);
        }

        // 
        // Write a object instance to data output stream
        //
        public override void LooseMarshal(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut)
        {

            WireFormatInfo info = (WireFormatInfo)o;

            info.BeforeMarshall(wireFormat);

            base.LooseMarshal(wireFormat, o, dataOut);
            dataOut.Write(info.Magic, 0, 8);
            dataOut.Write(info.Version);
            dataOut.Write(info.MarshalledProperties!=null);
            if(info.MarshalledProperties!=null) {
               dataOut.Write(info.MarshalledProperties.Length);
               dataOut.Write(info.MarshalledProperties);
            }

            info.AfterMarshall(wireFormat);
        }
    }
}
