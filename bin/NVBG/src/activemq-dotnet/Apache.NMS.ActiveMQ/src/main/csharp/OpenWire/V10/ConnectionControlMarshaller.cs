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
 *  Marshaler code for OpenWire format for ConnectionControl
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
    ///  Marshalling code for Open Wire Format for ConnectionControl
    /// </summary>
    class ConnectionControlMarshaller : BaseCommandMarshaller
    {
        /// <summery>
        ///  Creates an instance of the Object that this marshaller handles.
        /// </summery>
        public override DataStructure CreateObject() 
        {
            return new ConnectionControl();
        }

        /// <summery>
        ///  Returns the type code for the Object that this Marshaller handles..
        /// </summery>
        public override byte GetDataStructureType() 
        {
            return ConnectionControl.ID_CONNECTIONCONTROL;
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void TightUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn, BooleanStream bs) 
        {
            base.TightUnmarshal(wireFormat, o, dataIn, bs);

            ConnectionControl info = (ConnectionControl)o;
            info.Close = bs.ReadBoolean();
            info.Exit = bs.ReadBoolean();
            info.FaultTolerant = bs.ReadBoolean();
            info.Resume = bs.ReadBoolean();
            info.Suspend = bs.ReadBoolean();
            info.ConnectedBrokers = TightUnmarshalString(dataIn, bs);
            info.ReconnectTo = TightUnmarshalString(dataIn, bs);
            info.RebalanceConnection = bs.ReadBoolean();
            info.Token = ReadBytes(dataIn, bs.ReadBoolean());
        }

        //
        // Write the booleans that this object uses to a BooleanStream
        //
        public override int TightMarshal1(OpenWireFormat wireFormat, Object o, BooleanStream bs)
        {
            ConnectionControl info = (ConnectionControl)o;

            int rc = base.TightMarshal1(wireFormat, o, bs);
            bs.WriteBoolean(info.Close);
            bs.WriteBoolean(info.Exit);
            bs.WriteBoolean(info.FaultTolerant);
            bs.WriteBoolean(info.Resume);
            bs.WriteBoolean(info.Suspend);
            rc += TightMarshalString1(info.ConnectedBrokers, bs);
            rc += TightMarshalString1(info.ReconnectTo, bs);
            bs.WriteBoolean(info.RebalanceConnection);
            bs.WriteBoolean(info.Token!=null);
            rc += info.Token==null ? 0 : info.Token.Length+4;

            return rc + 0;
        }

        // 
        // Write a object instance to data output stream
        //
        public override void TightMarshal2(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut, BooleanStream bs)
        {
            base.TightMarshal2(wireFormat, o, dataOut, bs);

            ConnectionControl info = (ConnectionControl)o;
            bs.ReadBoolean();
            bs.ReadBoolean();
            bs.ReadBoolean();
            bs.ReadBoolean();
            bs.ReadBoolean();
            TightMarshalString2(info.ConnectedBrokers, dataOut, bs);
            TightMarshalString2(info.ReconnectTo, dataOut, bs);
            bs.ReadBoolean();
            if(bs.ReadBoolean()) {
                dataOut.Write(info.Token.Length);
                dataOut.Write(info.Token);
            }
        }

        // 
        // Un-marshal an object instance from the data input stream
        // 
        public override void LooseUnmarshal(OpenWireFormat wireFormat, Object o, BinaryReader dataIn) 
        {
            base.LooseUnmarshal(wireFormat, o, dataIn);

            ConnectionControl info = (ConnectionControl)o;
            info.Close = dataIn.ReadBoolean();
            info.Exit = dataIn.ReadBoolean();
            info.FaultTolerant = dataIn.ReadBoolean();
            info.Resume = dataIn.ReadBoolean();
            info.Suspend = dataIn.ReadBoolean();
            info.ConnectedBrokers = LooseUnmarshalString(dataIn);
            info.ReconnectTo = LooseUnmarshalString(dataIn);
            info.RebalanceConnection = dataIn.ReadBoolean();
            info.Token = ReadBytes(dataIn, dataIn.ReadBoolean());
        }

        // 
        // Write a object instance to data output stream
        //
        public override void LooseMarshal(OpenWireFormat wireFormat, Object o, BinaryWriter dataOut)
        {

            ConnectionControl info = (ConnectionControl)o;

            base.LooseMarshal(wireFormat, o, dataOut);
            dataOut.Write(info.Close);
            dataOut.Write(info.Exit);
            dataOut.Write(info.FaultTolerant);
            dataOut.Write(info.Resume);
            dataOut.Write(info.Suspend);
            LooseMarshalString(info.ConnectedBrokers, dataOut);
            LooseMarshalString(info.ReconnectTo, dataOut);
            dataOut.Write(info.RebalanceConnection);
            dataOut.Write(info.Token!=null);
            if(info.Token!=null) {
               dataOut.Write(info.Token.Length);
               dataOut.Write(info.Token);
            }
        }
    }
}
