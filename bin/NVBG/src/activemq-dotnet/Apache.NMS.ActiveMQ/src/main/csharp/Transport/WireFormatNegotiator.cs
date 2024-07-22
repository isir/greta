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
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.OpenWire;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport
{
    /// <summary>
    /// A Transport which negotiates the wire format
    /// </summary>
    public class WireFormatNegotiator : TransportFilter
    {
        private readonly OpenWireFormat wireFormat;
        private readonly TimeSpan negotiateTimeout = TimeSpan.FromSeconds(15);

        private readonly Atomic<bool> firstStart=new Atomic<bool>(true);
        private readonly CountDownLatch readyCountDownLatch = new CountDownLatch(1);
        private readonly CountDownLatch wireInfoSentDownLatch = new CountDownLatch(1);

        public WireFormatNegotiator(ITransport next, OpenWireFormat wireFormat)
            : base(next)
        {
            this.wireFormat = wireFormat;
        }

        public override void Start()
        {
            base.Start();
            if (firstStart.CompareAndSet(true, false))
            {
                try
                {
                    next.Oneway(wireFormat.PreferredWireFormatInfo);
                }
                finally
                {
                    wireInfoSentDownLatch.countDown();
                }
            }
        }

        protected override void Dispose(bool disposing)
        {
            base.Dispose(disposing);
            readyCountDownLatch.countDown();
        }

        public override void Oneway(Command command)
        {
            if (!readyCountDownLatch.await(negotiateTimeout))
                throw new IOException("Wire format negotiation timeout: peer did not send his wire format.");
            next.Oneway(command);
        }

        protected override void OnCommand(ITransport sender, Command command)
        {
            if ( command.IsWireFormatInfo )
            {
                WireFormatInfo info = (WireFormatInfo)command;
                try
                {
                    if (!info.Valid)
                    {
                        throw new IOException("Remote wire format magic is invalid");
                    }
                    wireInfoSentDownLatch.await(negotiateTimeout);
                    wireFormat.RenegotiateWireFormat(info);
                }
                catch (Exception e)
                {
                    OnException(this, e);
                }
                finally
                {
                    readyCountDownLatch.countDown();
                }
            }
            this.commandHandler(sender, command);
        }

        protected override void OnException(ITransport sender, Exception command)
        {
            readyCountDownLatch.countDown();
            this.exceptionHandler(sender, command);
        }
    }
}

