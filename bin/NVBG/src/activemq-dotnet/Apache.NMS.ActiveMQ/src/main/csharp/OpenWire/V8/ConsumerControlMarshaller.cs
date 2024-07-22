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
 *  Marshaler code for OpenWire format for ConsumerControl
 *
 *  NOTE!: This file is auto generated - do not modify!
 *         if you need to make a change, please see the Java Classes
 *         in the nms-activemq-openwire-generator module
 *
 */

using System;
using System.IO;

using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.OpenWire.V8
{
    /// <summary>
    ///  Marshalling code for Open Wire Format for ConsumerControl
    /// </summary>
    class ConsumerControlMarshaller : BaseCommandMarshaller
    {
        /// <summery>
        ///  Creates an instance of the Object that this marshaller handles.
        /// </summery>
        public override DataStructure CreateObject() 
        {
            return new ConsumerControl();
        }

        /// <summery>
        ///  Returns the type code for the Object that this Marshaller handles..
        /// </summery>
        public override byte GetDataStructureType() 
        {
            return ConsumerControl.ID_CONSUMERCONTROL;
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void TightUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn, BooleanStream bs) 
        {
            base.TightUnmarshal(wireFormat, o, dataIn, bs);

            ConsumerControl info = (ConsumerControl)o;
            info.Destination = (ActiveMQDestination) TightUnmarshalNestedObject(wireFormat, dataIn, bs);
            info.Close = bs.ReadBoolean();
            info.ConsumerId = (ConsumerId) TightUnmarshalNestedObject(wireFormat, dataIn, bs);
            info.Prefetch = dataIn.ReadInt32();
            info.Flush = bs.ReadBoolean();
            info.Start = bs.ReadBoolean();
            info.Stop = bs.ReadBoolean();
        }

        //
        // Write the booleans that this object uses to a BooleanStream
        //
        public override int TightMarshal1(OpenWireFormat wireFormat, Object o, BooleanStream bs)
        {
            ConsumerControl info = (ConsumerControl)o;

            int rc = base.TightMarshal1(wireFormat, o, bs);
        rc += TightMarshalNestedObject1(wireFormat, (DataStructure)info.Destination, bs);
            bs.WriteBoolean(info.Close);
        rc += TightMarshalNestedObject1(wireFormat, (DataStructure)info.ConsumerId, bs);
            bs.WriteBoolean(info.Flush);
            bs.WriteBoolean(info.Start);
            bs.WriteBoolean(info.Stop);

            return rc + 4;
        }

        // 
        // Write a object instance to data output stream
        //
        public override void TightMarshal2(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut, BooleanStream bs)
        {
            base.TightMarshal2(wireFormat, o, dataOut, bs);

            ConsumerControl info = (ConsumerControl)o;
            TightMarshalNestedObject2(wireFormat, (DataStructure)info.Destination, dataOut, bs);
            bs.ReadBoolean();
            TightMarshalNestedObject2(wireFormat, (DataStructure)info.ConsumerId, dataOut, bs);
            dataOut.Write(info.Prefetch);
            bs.ReadBoolean();
            bs.ReadBoolean();
            bs.ReadBoolean();
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void LooseUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn) 
        {
            base.LooseUnmarshal(wireFormat, o, dataIn);

            ConsumerControl info = (ConsumerControl)o;
            info.Destination = (ActiveMQDestination) LooseUnmarshalNestedObject(wireFormat, dataIn);
            info.Close = dataIn.ReadBoolean();
            info.ConsumerId = (ConsumerId) LooseUnmarshalNestedObject(wireFormat, dataIn);
            info.Prefetch = dataIn.ReadInt32();
            info.Flush = dataIn.ReadBoolean();
            info.Start = dataIn.ReadBoolean();
            info.Stop = dataIn.ReadBoolean();
        }

        // 
        // Write a object instance to data output stream
        //
        public override void LooseMarshal(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut)
        {

            ConsumerControl info = (ConsumerControl)o;

            base.LooseMarshal(wireFormat, o, dataOut);
            LooseMarshalNestedObject(wireFormat, (DataStructure)info.Destination, dataOut);
            dataOut.Write(info.Close);
            LooseMarshalNestedObject(wireFormat, (DataStructure)info.ConsumerId, dataOut);
            dataOut.Write(info.Prefetch);
            dataOut.Write(info.Flush);
            dataOut.Write(info.Start);
            dataOut.Write(info.Stop);
        }
    }
}
