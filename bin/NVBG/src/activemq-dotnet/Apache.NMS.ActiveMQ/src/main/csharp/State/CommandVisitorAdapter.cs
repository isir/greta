/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.State
{
    public class CommandVisitorAdapter : ICommandVisitor
    {
        public virtual Response ProcessAddConnection(ConnectionInfo info)
        {
            return null;
        }

        public virtual Response ProcessAddConsumer(ConsumerInfo info)
        {
            return null;
        }

        public virtual Response ProcessAddDestination(DestinationInfo info)
        {
            return null;
        }

        public virtual Response ProcessAddProducer(ProducerInfo info)
        {
            return null;
        }

        public virtual Response ProcessAddSession(SessionInfo info)
        {
            return null;
        }

        public virtual Response ProcessBeginTransaction(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessBrokerInfo(BrokerInfo info)
        {
            return null;
        }

        public virtual Response ProcessCommitTransactionOnePhase(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessCommitTransactionTwoPhase(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessEndTransaction(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessFlushCommand(FlushCommand command)
        {
            return null;
        }

        public virtual Response ProcessForgetTransaction(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessKeepAliveInfo(KeepAliveInfo info)
        {
            return null;
        }

        public virtual Response ProcessMessage(Message send)
        {
            return null;
        }

        public virtual Response ProcessMessageAck(MessageAck ack)
        {
            return null;
        }

        public virtual Response ProcessMessageDispatchNotification(MessageDispatchNotification notification)
        {
            return null;
        }

        public virtual Response ProcessMessagePull(MessagePull pull)
        {
            return null;
        }

        public virtual Response ProcessPrepareTransaction(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessProducerAck(ProducerAck ack)
        {
            return null;
        }

        public virtual Response ProcessRecoverTransactions(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessRemoveConnection(ConnectionId id)
        {
            return null;
        }

        public virtual Response ProcessRemoveConsumer(ConsumerId id)
        {
            return null;
        }

        public virtual Response ProcessRemoveDestination(DestinationInfo info)
        {
            return null;
        }

        public virtual Response ProcessRemoveProducer(ProducerId id)
        {
            return null;
        }

        public virtual Response ProcessRemoveSession(SessionId id)
        {
            return null;
        }

        public virtual Response ProcessRemoveSubscriptionInfo(RemoveSubscriptionInfo info)
        {
            return null;
        }

        public virtual Response ProcessRollbackTransaction(TransactionInfo info)
        {
            return null;
        }

        public virtual Response ProcessShutdownInfo(ShutdownInfo info)
        {
            return null;
        }

        public virtual Response ProcessWireFormat(WireFormatInfo info)
        {
            return null;
        }

        public virtual Response ProcessMessageDispatch(MessageDispatch dispatch)
        {
            return null;
        }

        public virtual Response ProcessControlCommand(ControlCommand command)
        {
            return null;
        }

        public virtual Response ProcessConnectionControl(ConnectionControl control)
        {
            return null;
        }

        public virtual Response ProcessConnectionError(ConnectionError error)
        {
            return null;
        }

        public virtual Response ProcessConsumerControl(ConsumerControl control)
        {
            return null;
        }

        public virtual Response ProcessResponse(Response response)
        {
            return null;
        }

        public virtual Response ProcessReplayCommand(ReplayCommand replayCommand)
        {
           return null;
        }
    }
}
