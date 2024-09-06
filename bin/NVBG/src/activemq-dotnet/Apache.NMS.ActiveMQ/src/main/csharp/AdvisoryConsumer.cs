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

using Apache.NMS.ActiveMQ.Util;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ
{
    /// <summary>
    /// Consumes Advisory Messages for Temp Destination creation on deletion so that
    /// the connection can track valid destinations for its sessions, and session resources.
    /// </summary>
    internal class AdvisoryConsumer : IDispatcher
    {
        private readonly Connection connection;
        private readonly ConsumerInfo info;

        private bool closed = false;
        private int deliveredCounter = 0;

        internal AdvisoryConsumer(Connection connection, ConsumerId consumerId) : base()
        {
            this.connection = connection;
            this.info = new ConsumerInfo();
            this.info.ConsumerId = consumerId;
            this.info.Destination = AdvisorySupport.TEMP_DESTINATION_COMPOSITE_ADVISORY_TOPIC;
            this.info.PrefetchSize = 1000;
            this.info.NoLocal = true;

            this.connection.AddDispatcher(consumerId, this);
            this.connection.SyncRequest(this.info);
        }

        internal void Dispose()
        {
            if(!closed)
            {
                this.closed = true;
                try
                {
                    RemoveInfo removeIt = new RemoveInfo();
                    removeIt.ObjectId = this.info.ConsumerId;
                    this.connection.Oneway(removeIt);
                }
                catch(Exception e)
                {
                    Tracer.Debug("Failed to send remove for AdvisoryConsumer: " + e.Message);
                }
                this.connection.RemoveDispatcher(this.info.ConsumerId);
            }
        }

        public void Dispatch(MessageDispatch messageDispatch)
        {
            // Auto ack messages when we reach 75% of the prefetch
            deliveredCounter++;

            if(deliveredCounter > (0.75 * this.info.PrefetchSize))
            {
                try
                {
                    MessageAck ack = new MessageAck();
                    ack.AckType = (byte)AckType.ConsumedAck;
					ack.ConsumerId = messageDispatch.ConsumerId;
					ack.Destination = messageDispatch.Destination;
                    ack.LastMessageId = messageDispatch.Message.MessageId;
                    ack.MessageCount = deliveredCounter;

                    this.connection.Oneway(ack);
                    this.deliveredCounter = 0;
                }
                catch(Exception e)
                {
                    this.connection.OnAsyncException(e);
                }
            }

            DestinationInfo destInfo = messageDispatch.Message.DataStructure as DestinationInfo;
            if(destInfo != null)
            {
                ProcessDestinationInfo(destInfo);
            }
            else
            {
                // This can happen across networks
                Tracer.Debug("Unexpected message was dispatched to the AdvisoryConsumer: " + messageDispatch);
            }
        }

        private void ProcessDestinationInfo(DestinationInfo destInfo)
        {
            ActiveMQDestination dest = destInfo.Destination;
            if(!dest.IsTemporary)
            {
                return;
            }
    
            ActiveMQTempDestination tempDest = dest as ActiveMQTempDestination;
            if(destInfo.OperationType == DestinationInfo.ADD_OPERATION_TYPE)
            {
                if(Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("AdvisoryConsumer adding: " + tempDest);
                }
                this.connection.AddTempDestination(tempDest);
            }
            else if(destInfo.OperationType == DestinationInfo.REMOVE_OPERATION_TYPE)
            {
                if(Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("AdvisoryConsumer removing: " + tempDest);
                }
                this.connection.RemoveTempDestination(tempDest);
            }
        }
    }
}

