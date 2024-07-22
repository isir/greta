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

using System;
using System.IO;
using System.Reflection;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Transport;
using Apache.NMS.ActiveMQ.Transport.Tcp;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.OpenWire
{
    /// <summary>
    /// Implements the <a href="http://activemq.apache.org/openwire.html">OpenWire</a> protocol.
    /// </summary>
    public class OpenWireFormat : IWireFormat
    {
        private readonly BaseDataStreamMarshaller[] dataMarshallers;
        private const byte NULL_TYPE = 0;

        private int version;
        private bool cacheEnabled = false;
        private bool stackTraceEnabled = false;
        private bool tcpNoDelayEnabled = false;
        private bool sizePrefixDisabled = false;
        private bool tightEncodingEnabled = false;
        private long maxInactivityDuration = 0;
        private long maxInactivityDurationInitialDelay = 0;
        private int cacheSize = 0;
        private const int minimumVersion = 1;

        private WireFormatInfo preferredWireFormatInfo = new WireFormatInfo();
        private ITransport transport;

        public OpenWireFormat()
        {
            // See the following link for defaults: http://activemq.apache.org/configuring-wire-formats.html
            // See also the following link for OpenWire format info: http://activemq.apache.org/openwire-version-2-specification.html
            PreferredWireFormatInfo.CacheEnabled = false;
            PreferredWireFormatInfo.StackTraceEnabled = false;
            PreferredWireFormatInfo.TcpNoDelayEnabled = true;
            PreferredWireFormatInfo.SizePrefixDisabled = false;
            PreferredWireFormatInfo.TightEncodingEnabled = false;
            PreferredWireFormatInfo.MaxInactivityDuration = 30000;
            PreferredWireFormatInfo.MaxInactivityDurationInitialDelay = 10000;
            PreferredWireFormatInfo.CacheSize = 0;
            PreferredWireFormatInfo.Version = 10;

            dataMarshallers = new BaseDataStreamMarshaller[256];
            Version = 1;
        }

        public ITransport Transport
        {
            get { return transport; }
            set { transport = value; }
        }

        public int Version
        {
            get { return version; }
            set
            {
                Assembly dll = Assembly.GetExecutingAssembly();
                Type type = dll.GetType("Apache.NMS.ActiveMQ.OpenWire.V" + value + ".MarshallerFactory", false);
                IMarshallerFactory factory = (IMarshallerFactory) Activator.CreateInstance(type);
                factory.configure(this);
                version = value;
            }
        }

        public bool CacheEnabled
        {
            get { return cacheEnabled; }
            set { cacheEnabled = value; }
        }

        public bool StackTraceEnabled
        {
            get { return stackTraceEnabled; }
            set { stackTraceEnabled = value; }
        }

        public bool TcpNoDelayEnabled
        {
            get { return tcpNoDelayEnabled; }
            set { tcpNoDelayEnabled = value; }
        }

        public bool SizePrefixDisabled
        {
            get { return sizePrefixDisabled; }
            set { sizePrefixDisabled = value; }
        }

        public bool TightEncodingEnabled
        {
            get { return tightEncodingEnabled; }
            set { tightEncodingEnabled = value; }
        }

        public long MaxInactivityDuration
        {
            get { return maxInactivityDuration; }
            set { maxInactivityDuration = value; }
        }

        public long MaxInactivityDurationInitialDelay
        {
            get { return maxInactivityDurationInitialDelay; }
            set { maxInactivityDurationInitialDelay = value; }
        }

        public int CacheSize
        {
            get { return cacheSize; }
            set { cacheSize = value; }
        }

        public WireFormatInfo PreferredWireFormatInfo
        {
            get { return preferredWireFormatInfo; }
            set { preferredWireFormatInfo = value; }
        }

        public void clearMarshallers()
        {
            for(int i = 0; i < dataMarshallers.Length; i++)
            {
                dataMarshallers[i] = null;
            }
        }

        public void addMarshaller(BaseDataStreamMarshaller marshaller)
        {
            byte type = marshaller.GetDataStructureType();
            dataMarshallers[type & 0xFF] = marshaller;
        }

        private BaseDataStreamMarshaller GetDataStreamMarshallerForType(byte dataType)
        {
            BaseDataStreamMarshaller dsm = this.dataMarshallers[dataType & 0xFF];
            if(null == dsm)
            {
                throw new IOException("Unknown data type: " + dataType);
            }
            return dsm;
        }

        public void Marshal(Object o, BinaryWriter ds)
        {
            int size = 1;
            if(o != null)
            {
                DataStructure c = (DataStructure) o;
                byte type = c.GetDataStructureType();
                BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(type);

                if(tightEncodingEnabled)
                {
                    BooleanStream bs = new BooleanStream();
                    size += dsm.TightMarshal1(this, c, bs);
                    size += bs.MarshalledSize();

                    if(!sizePrefixDisabled)
                    {
                        ds.Write(size);
                    }

                    ds.Write(type);
                    bs.Marshal(ds);
                    dsm.TightMarshal2(this, c, ds, bs);
                }
                else
                {
                    BinaryWriter looseOut = ds;
                    MemoryStream ms = null;

                    // If we are prefixing then we need to first write it to memory,
                    // otherwise we can write direct to the stream.
                    if(!sizePrefixDisabled)
                    {
                        ms = new MemoryStream();
                        looseOut = new EndianBinaryWriter(ms);
                        looseOut.Write(size);
                    }

                    looseOut.Write(type);
                    dsm.LooseMarshal(this, c, looseOut);

                    if(!sizePrefixDisabled)
                    {
                        ms.Position = 0;
                        looseOut.Write((int) ms.Length - 4);
                        ds.Write(ms.GetBuffer(), 0, (int) ms.Length);
                    }
                }
            }
            else
            {
                ds.Write(size);
                ds.Write(NULL_TYPE);
            }

            ds.Flush();
        }

        public Object Unmarshal(BinaryReader dis)
        {
            // lets ignore the size of the packet
            if(!sizePrefixDisabled)
            {
                dis.ReadInt32();
            }

            // first byte is the type of the packet
            byte dataType = dis.ReadByte();

            if(dataType != NULL_TYPE)
            {
                BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(dataType);

                Object data = dsm.CreateObject();

                if(tightEncodingEnabled)
                {
                    BooleanStream bs = new BooleanStream();
                    bs.Unmarshal(dis);
                    dsm.TightUnmarshal(this, data, dis, bs);
                    return data;
                }
                else
                {
                    dsm.LooseUnmarshal(this, data, dis);
                    return data;
                }
            }

            return null;
        }

        public int TightMarshalNestedObject1(DataStructure o, BooleanStream bs)
        {
            bs.WriteBoolean(o != null);
            if(null == o)
            {
                return 0;
            }

            if(o.IsMarshallAware())
            {
                MarshallAware ma = (MarshallAware) o;
                byte[] sequence = ma.GetMarshalledForm(this);
                bs.WriteBoolean(sequence != null);
                if(sequence != null)
                {
                    return 1 + sequence.Length;
                }
            }

            byte type = o.GetDataStructureType();
            if(type == 0)
            {
                throw new IOException("No valid data structure type for: " + o + " of type: " + o.GetType());
            }

            BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(type);

            Tracer.Debug("Marshalling type: " + type + " with structure: " + o);
            return 1 + dsm.TightMarshal1(this, o, bs);
        }

        public void TightMarshalNestedObject2(DataStructure o, BinaryWriter ds, BooleanStream bs)
        {
            if(!bs.ReadBoolean())
            {
                return;
            }

            byte type = o.GetDataStructureType();
            ds.Write(type);

            if(o.IsMarshallAware() && bs.ReadBoolean())
            {
                MarshallAware ma = (MarshallAware) o;
                byte[] sequence = ma.GetMarshalledForm(this);
                ds.Write(sequence, 0, sequence.Length);
            }
            else
            {
                BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(type);
                dsm.TightMarshal2(this, o, ds, bs);
            }
        }

        public DataStructure TightUnmarshalNestedObject(BinaryReader dis, BooleanStream bs)
        {
            if(bs.ReadBoolean())
            {
                byte dataType = dis.ReadByte();

                BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(dataType);
                DataStructure data = dsm.CreateObject();

                if(data.IsMarshallAware() && bs.ReadBoolean())
                {
                    dis.ReadInt32();
                    dis.ReadByte();

                    BooleanStream bs2 = new BooleanStream();
                    bs2.Unmarshal(dis);
                    dsm.TightUnmarshal(this, data, dis, bs2);
                }
                else
                {
                    dsm.TightUnmarshal(this, data, dis, bs);
                }

                return data;
            }

            return null;
        }

        public void LooseMarshalNestedObject(DataStructure o, BinaryWriter dataOut)
        {
            dataOut.Write(o != null);
            if(o != null)
            {
                byte type = o.GetDataStructureType();
                dataOut.Write(type);

                BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(type);
                dsm.LooseMarshal(this, o, dataOut);
            }
        }

        public DataStructure LooseUnmarshalNestedObject(BinaryReader dis)
        {
            if(dis.ReadBoolean())
            {
                byte dataType = dis.ReadByte();

                BaseDataStreamMarshaller dsm = GetDataStreamMarshallerForType(dataType);
                DataStructure data = dsm.CreateObject();
                dsm.LooseUnmarshal(this, data, dis);
                return data;
            }

            return null;
        }

        public void RenegotiateWireFormat(WireFormatInfo info)
        {
            if(info.Version < minimumVersion)
            {
                throw new IOException("Remote wire format (" + info.Version + ") is lower than the minimum version required (" + minimumVersion + ")");
            }

            this.Version = Math.Min(PreferredWireFormatInfo.Version, info.Version);
            this.cacheEnabled = info.CacheEnabled && PreferredWireFormatInfo.CacheEnabled;
            this.stackTraceEnabled = info.StackTraceEnabled && PreferredWireFormatInfo.StackTraceEnabled;
            this.tcpNoDelayEnabled = info.TcpNoDelayEnabled && PreferredWireFormatInfo.TcpNoDelayEnabled;
            this.sizePrefixDisabled = info.SizePrefixDisabled && PreferredWireFormatInfo.SizePrefixDisabled;
            this.tightEncodingEnabled = info.TightEncodingEnabled && PreferredWireFormatInfo.TightEncodingEnabled;
            this.maxInactivityDuration = info.MaxInactivityDuration;
            this.maxInactivityDurationInitialDelay = info.MaxInactivityDurationInitialDelay;
            this.cacheSize = info.CacheSize;

            TcpTransport tcpTransport = this.transport as TcpTransport;
            if(null != tcpTransport)
            {
                tcpTransport.TcpNoDelayEnabled = this.tcpNoDelayEnabled;
            }
        }
    }
}
