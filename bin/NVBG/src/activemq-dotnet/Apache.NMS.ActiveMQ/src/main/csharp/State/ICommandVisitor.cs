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
    public interface ICommandVisitor
    {
        Response ProcessAddConnection(ConnectionInfo info);

        Response ProcessAddSession(SessionInfo info);

        Response ProcessAddProducer(ProducerInfo info);

        Response ProcessAddConsumer(ConsumerInfo info);

        Response ProcessRemoveConnection(ConnectionId id);

        Response ProcessRemoveSession(SessionId id);

        Response ProcessRemoveProducer(ProducerId id);

        Response ProcessRemoveConsumer(ConsumerId id);

        Response ProcessAddDestination(DestinationInfo info);

        Response ProcessRemoveDestination(DestinationInfo info);

        Response ProcessRemoveSubscriptionInfo(RemoveSubscriptionInfo info);

        Response ProcessMessage(Message send);

        Response ProcessMessageAck(MessageAck ack);

        Response ProcessMessagePull(MessagePull pull);

        Response ProcessBeginTransaction(TransactionInfo info);

        Response ProcessPrepareTransaction(TransactionInfo info);

        Response ProcessCommitTransactionOnePhase(TransactionInfo info);

        Response ProcessCommitTransactionTwoPhase(TransactionInfo info);

        Response ProcessRollbackTransaction(TransactionInfo info);

        Response ProcessWireFormat(WireFormatInfo info);

        Response ProcessKeepAliveInfo(KeepAliveInfo info);

        Response ProcessShutdownInfo(ShutdownInfo info);

        Response ProcessFlushCommand(FlushCommand command);

        Response ProcessBrokerInfo(BrokerInfo info);

        Response ProcessRecoverTransactions(TransactionInfo info);

        Response ProcessForgetTransaction(TransactionInfo info);

        Response ProcessEndTransaction(TransactionInfo info);

        Response ProcessMessageDispatchNotification(MessageDispatchNotification notification);

        Response ProcessProducerAck(ProducerAck ack);

        Response ProcessMessageDispatch(MessageDispatch dispatch);

        Response ProcessControlCommand(ControlCommand command);

        Response ProcessConnectionError(ConnectionError error);

        Response ProcessConnectionControl(ConnectionControl control);

        Response ProcessConsumerControl(ConsumerControl control);

        Response ProcessResponse(Response response);

        Response ProcessReplayCommand(ReplayCommand replayCommand);

    }
}
