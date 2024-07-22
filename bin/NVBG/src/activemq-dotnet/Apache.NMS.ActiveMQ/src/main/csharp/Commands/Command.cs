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
using Apache.NMS.ActiveMQ.State;

namespace Apache.NMS.ActiveMQ.Commands
{

    /// <summary>
    /// An OpenWire command
    /// </summary>
    public interface Command : DataStructure, ICloneable
    {
        int CommandId
        {
            get;
            set;
        }

        bool ResponseRequired
        {
            get;
            set;
        }

        bool IsBrokerInfo
        {
            get;
        }

        bool IsConnectionControl
        {
            get;
        }

        bool IsConnectionInfo
        {
            get;
        }

        bool IsConnectionError
        {
            get;
        }

        bool IsConsumerControl
        {
            get;
        }

        bool IsConsumerInfo
        {
            get;
        }

        bool IsControlCommand
        {
            get;
        }

        bool IsDestinationInfo
        {
            get;
        }

        bool IsFlushCommand
        {
            get;
        }

        bool IsKeepAliveInfo
        {
            get;
        }

        bool IsMessage
        {
            get;
        }

        bool IsMessageAck
        {
            get;
        }

        bool IsMessageDispatch
        {
            get;
        }

        bool IsMessageDispatchNotification
        {
            get;
        }

        bool IsMessagePull
        {
            get;
        }

        bool IsProducerAck
        {
            get;
        }

        bool IsProducerInfo
        {
            get;
        }

        bool IsRemoveInfo
        {
            get;
        }

        bool IsRemoveSubscriptionInfo
        {
            get;
        }

        bool IsReplayCommand
        {
            get;
        }

        bool IsResponse
        {
            get;
        }

        bool IsSessionInfo
        {
            get;
        }

        bool IsShutdownInfo
        {
            get;
        }

        bool IsTransactionInfo
        {
            get;
        }

        bool IsWireFormatInfo
        {
           get;
        }

        Response Visit(ICommandVisitor visitor);
    }
}

