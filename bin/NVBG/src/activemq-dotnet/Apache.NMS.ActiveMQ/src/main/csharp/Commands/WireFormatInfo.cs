/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

using System.Text;
using Apache.NMS.ActiveMQ.OpenWire;
using Apache.NMS.ActiveMQ.State;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Commands
{
    //
    //  Marshalling code for Open Wire Format for WireFormatInfo
    //
    //
    public class WireFormatInfo : BaseCommand, Command, MarshallAware
    {
        public const byte ID_WIREFORMATINFO = 1;
        static private readonly byte[] MAGIC = new byte[] {
                        'A'&0xFF,
                        'c'&0xFF,
                        't'&0xFF,
                        'i'&0xFF,
                        'v'&0xFF,
                        'e'&0xFF,
                        'M'&0xFF,
                        'Q'&0xFF };

        byte[] magic = MAGIC;
        int version;
        byte[] marshalledProperties;

        private PrimitiveMap properties;

        public override string ToString()
        {
            return GetType().Name + "["
                    + " Magic=" + Encoding.ASCII.GetString(magic)
                    + " Version=" + Version
                    + " MarshalledProperties=" + Properties.ToString()
                    + " ]";

        }

        public override byte GetDataStructureType()
        {
            return ID_WIREFORMATINFO;
        }


        // Properties
        public byte[] Magic
        {
            get { return magic; }
            set { this.magic = value; }
        }

        public bool Valid
        {
            get
            {
                if(null == magic)
                {
                    return false;
                }

                if(magic.Length != MAGIC.Length)
                {
                    return false;
                }

                for(int i = 0; i < magic.Length; i++)
                {
                    if(magic[i] != MAGIC[i])
                    {
                        return false;
                    }
                }

                return true;
            }
        }

        public int Version
        {
            get { return version; }
            set { this.version = value; }
        }

        public byte[] MarshalledProperties
        {
            get { return marshalledProperties; }
            set { this.marshalledProperties = value; }
        }

        public IPrimitiveMap Properties
        {
            get
            {
                if(null == properties)
                {
                    properties = PrimitiveMap.Unmarshal(MarshalledProperties);
                }

                return properties;
            }
        }

        public bool CacheEnabled
        {
            get { return true.Equals(Properties["CacheEnabled"]); }
            set { Properties["CacheEnabled"] = false; }
        }
        public bool StackTraceEnabled
        {
            get { return true.Equals(Properties["StackTraceEnabled"]); }
            set { Properties["StackTraceEnabled"] = value; }
        }
        public bool TcpNoDelayEnabled
        {
            get { return true.Equals(Properties["TcpNoDelayEnabled"]); }
            set { Properties["TcpNoDelayEnabled"] = value; }
        }
        public bool SizePrefixDisabled
        {
            get { return true.Equals(Properties["SizePrefixDisabled"]); }
            set { Properties["SizePrefixDisabled"] = value; }
        }
        public bool TightEncodingEnabled
        {
            get { return true.Equals(Properties["TightEncodingEnabled"]); }
            set { Properties["TightEncodingEnabled"] = value; }
        }
        public long MaxInactivityDuration
        {
            get
            {
                object prop = Properties["MaxInactivityDuration"];
                return (null != prop
                                        ? (long) prop
                                        : 0);
            }
            set { Properties["MaxInactivityDuration"] = value; }
        }
        public long MaxInactivityDurationInitialDelay
        {
            get
            {
                object prop = Properties["MaxInactivityDurationInitialDelay"];
                return (null != prop
                                        ? (long) prop
                                        : 0);
            }
            set { Properties["MaxInactivityDurationInitialDelay"] = value; }
        }
        public int CacheSize
        {
            get
            {
                object prop = Properties["CacheSize"];
                return (null != prop
                                        ? (int) prop
                                        : 0);
            }
            set { Properties.SetInt("CacheSize", value); }
        }

        // MarshallAware interface
        public override bool IsMarshallAware()
        {
            return true;
        }

        public override void BeforeMarshall(OpenWireFormat wireFormat)
        {
            MarshalledProperties = null;

            if(properties != null)
            {
                MarshalledProperties = properties.Marshal();
            }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the IsWireFormatInfo() query.
        /// </summery>
        ///
        public override bool IsWireFormatInfo
        {
            get
            {
                return true;
            }
        }

        public override Response Visit(ICommandVisitor visitor)
        {
            return visitor.ProcessWireFormat(this);
        }
    }
}
