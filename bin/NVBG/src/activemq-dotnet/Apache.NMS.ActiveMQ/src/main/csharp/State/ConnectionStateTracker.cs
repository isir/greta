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

using System;
using System.Collections.Generic;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Transport;
using Apache.NMS.ActiveMQ.Util;
using System.Collections;

namespace Apache.NMS.ActiveMQ.State
{
    /// <summary>
    /// Tracks the state of a connection so a newly established transport can be
    /// re-initialized to the state that was tracked.
    /// </summary>
    public class ConnectionStateTracker : CommandVisitorAdapter
    {
        private static readonly Tracked TRACKED_RESPONSE_MARKER = new Tracked(null);

        protected readonly Dictionary<ConnectionId, ConnectionState> connectionStates =
            new Dictionary<ConnectionId, ConnectionState>();

        private bool isTrackTransactions;
        private bool isTrackTransactionProducers = true;
        private bool isRestoreSessions = true;
        private bool isRestoreConsumers = true;
        private bool isRestoreProducers = true;
        private bool isRestoreTransaction = true;
        private bool isTrackMessages = true;
        private int maxCacheSize = 256;
        private readonly LRUCache<Object, Command> messageCache = new LRUCache<Object, Command>(256);

        private class RemoveTransactionAction : ResponseHandler
        {
            private readonly TransactionInfo info;
            private readonly ConnectionStateTracker cst;

            public RemoveTransactionAction(TransactionInfo info, ConnectionStateTracker aCst)
            {
                this.info = info;
                this.cst = aCst;
            }

            public override void OnResponse()
            {
                ConnectionState cs;

                if(cst.connectionStates.TryGetValue(info.ConnectionId, out cs))
                {
                    cs.RemoveTransactionState(info.TransactionId);
                }
            }
        }

        /// <summary>
        /// </summary>
        /// <param name="command"></param>
        /// <returns>null if the command is not state tracked.</returns>
        public Tracked Track(Command command)
        {
            try
            {
                return (Tracked) command.Visit(this);
            }
            catch(IOException)
            {
                throw;
            }
            catch(Exception e)
            {
                throw new IOException(e.Message);
            }
        }

        public void TrackBack(Command command)
        {
        }

        public void DoRestore(ITransport transport)
        {
            // Restore the connections.
            foreach(ConnectionState connectionState in connectionStates.Values)
            {
                ConnectionInfo info = connectionState.Info;
                info.FailoverReconnect = true;
                if (Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("conn: " + connectionState.Info.ConnectionId);
                }
                transport.Oneway(info);

                DoRestoreTempDestinations(transport, connectionState);

                if(RestoreSessions)
                {
                    DoRestoreSessions(transport, connectionState);
                }

                if(RestoreTransaction)
                {
                    DoRestoreTransactions(transport, connectionState);
                }
            }

            // Now flush messages
            foreach(Command command in messageCache.Values)
            {
                if (Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("Replaying command: " + command);
                }

                transport.Oneway(command);
            }
        }

        private void DoRestoreTransactions(ITransport transport, ConnectionState connectionState)
        {
            AtomicCollection<TransactionState> transactionStates = connectionState.TransactionStates;
            List<TransactionInfo> toRollback = new List<TransactionInfo>();

            foreach(TransactionState transactionState in transactionStates)
            {
                // rollback any completed transactions - no way to know if commit got there
                // or if reply went missing
                if (transactionState.Commands.Count != 0)
                {
                    Command lastCommand = transactionState.Commands[transactionState.Commands.Count - 1];
                    if (lastCommand.IsTransactionInfo)
                    {
                        TransactionInfo transactionInfo = lastCommand as TransactionInfo;
                        if (transactionInfo.Type == TransactionInfo.COMMIT_ONE_PHASE)
                        {
                            if (Tracer.IsDebugEnabled)
                            {
                                Tracer.Debug("rolling back potentially completed tx: " +
                                             transactionState.Id);
                            }
                            toRollback.Add(transactionInfo);
                            continue;
                        }
                    }
                }

                // replay the add and remove of short lived producers that may have been
                // involved in the transaction
                foreach (ProducerState producerState in transactionState.ProducerStates)
                {
                    if (Tracer.IsDebugEnabled)
                    {
                        Tracer.Debug("tx replay producer :" + producerState.Info);
                    }
                    transport.Oneway(producerState.Info);
                }

                foreach (Command command in transactionState.Commands)
                {
                    if (Tracer.IsDebugEnabled)
                    {
                        Tracer.Debug("tx replay: " + command);
                    }
                    transport.Oneway(command);
                }

                foreach (ProducerState producerState in transactionState.ProducerStates)
                {
                    if (Tracer.IsDebugEnabled)
                    {
                        Tracer.Debug("tx remove replayed producer :" + producerState.Info);
                    }

                    RemoveInfo producerRemove = new RemoveInfo();
                    producerRemove.ObjectId = producerState.Info.ProducerId;
                    transport.Oneway(producerRemove);
                }
            }

            foreach (TransactionInfo command in toRollback)
            {
                // respond to the outstanding commit
                ExceptionResponse response = new ExceptionResponse();
                response.Exception = new BrokerError();
                response.Exception.Message =
                    "Transaction completion in doubt due to failover. Forcing rollback of " + command.TransactionId;
                response.Exception.ExceptionClass = (new TransactionRolledBackException()).GetType().FullName;
                response.CorrelationId = command.CommandId;
                transport.Command(transport, response);
            }
        }

        /// <summary>
        /// </summary>
        /// <param name="transport"></param>
        /// <param name="connectionState"></param>
        protected void DoRestoreSessions(ITransport transport, ConnectionState connectionState)
        {
            // Restore the connection's sessions
            foreach(SessionState sessionState in connectionState.SessionStates)
            {
                if (Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("Restoring session: " + sessionState.Info.SessionId);
                }
                transport.Oneway(sessionState.Info);

                if(RestoreProducers)
                {
                    DoRestoreProducers(transport, sessionState);
                }

                if(RestoreConsumers)
                {
                    DoRestoreConsumers(transport, sessionState);
                }
            }
        }

        /// <summary>
        /// </summary>
        /// <param name="transport"></param>
        /// <param name="sessionState"></param>
        protected void DoRestoreConsumers(ITransport transport, SessionState sessionState)
        {
            // Restore the session's consumers but possibly in pull only (prefetch 0 state) till
            // recovery completes.

            ConnectionState connectionState = null;
            bool connectionInterruptionProcessingComplete = false;

            if(connectionStates.TryGetValue(sessionState.Info.SessionId.ParentId, out connectionState))
            {
                connectionInterruptionProcessingComplete = connectionState.ConnectionInterruptProcessingComplete;
            }

            // Restore the session's consumers
            foreach(ConsumerState consumerState in sessionState.ConsumerStates)
            {
                ConsumerInfo infoToSend = consumerState.Info;

                if(!connectionInterruptionProcessingComplete && infoToSend.PrefetchSize > 0 && transport.WireFormat.Version > 5)
                {
                    infoToSend = consumerState.Info.Clone() as ConsumerInfo;
                    lock(((ICollection) connectionState.RecoveringPullConsumers).SyncRoot)
                    {
                        if(!connectionState.RecoveringPullConsumers.ContainsKey(infoToSend.ConsumerId))
                        {
                            connectionState.RecoveringPullConsumers.Add(infoToSend.ConsumerId, consumerState.Info);
                        }
                    }
                    infoToSend.PrefetchSize = 0;
                    if(Tracer.IsDebugEnabled)
                    {
                        Tracer.Debug("restore consumer: " + infoToSend.ConsumerId +
                                     " in pull mode pending recovery, overriding prefetch: " +
                                     consumerState.Info.PrefetchSize);
                    }
                }

                if(Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("restore consumer: " + infoToSend.ConsumerId);
                }

                transport.Oneway(infoToSend);
            }
        }

        /// <summary>
        /// </summary>
        /// <param name="transport"></param>
        /// <param name="sessionState"></param>
        protected void DoRestoreProducers(ITransport transport, SessionState sessionState)
        {
            // Restore the session's producers
            foreach(ProducerState producerState in sessionState.ProducerStates)
            {
                if (Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("Restoring producer: " + producerState.Info.ProducerId);
                }
                transport.Oneway(producerState.Info);
            }
        }

        /// <summary>
        /// </summary>
        /// <param name="transport"></param>
        /// <param name="connectionState"></param>
        protected void DoRestoreTempDestinations(ITransport transport, ConnectionState connectionState)
        {
            // Restore the connection's temp destinations.
            foreach(DestinationInfo destinationInfo in connectionState.TempDestinations)
            {
                transport.Oneway(destinationInfo);
            }
        }

        public override Response ProcessAddDestination(DestinationInfo info)
        {
            if(info != null && info.Destination.IsTemporary)
            {
                ConnectionState cs;

                if(connectionStates.TryGetValue(info.ConnectionId, out cs))
                {
                    cs.AddTempDestination(info);
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessRemoveDestination(DestinationInfo info)
        {
            if(info != null && info.Destination.IsTemporary)
            {
                ConnectionState cs;
                if(connectionStates.TryGetValue(info.ConnectionId, out cs))
                {
                    cs.RemoveTempDestination(info.Destination);
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessAddProducer(ProducerInfo info)
        {
            if(info != null && info.ProducerId != null)
            {
                SessionId sessionId = info.ProducerId.ParentId;
                if(sessionId != null)
                {
                    ConnectionId connectionId = sessionId.ParentId;
                    if(connectionId != null)
                    {
                        ConnectionState cs;

                        if(connectionStates.TryGetValue(connectionId, out cs))
                        {
                            SessionState ss = cs[sessionId];
                            if(ss != null)
                            {
                                ss.AddProducer(info);
                            }
                        }
                    }
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessRemoveProducer(ProducerId id)
        {
            if(id != null)
            {
                SessionId sessionId = id.ParentId;
                if(sessionId != null)
                {
                    ConnectionId connectionId = sessionId.ParentId;
                    if(connectionId != null)
                    {
                        ConnectionState cs = null;

                        if(connectionStates.TryGetValue(connectionId, out cs))
                        {
                            SessionState ss = cs[sessionId];
                            if(ss != null)
                            {
                                ss.RemoveProducer(id);
                            }
                        }
                    }
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessAddConsumer(ConsumerInfo info)
        {
            if(info != null)
            {
                SessionId sessionId = info.ConsumerId.ParentId;
                if(sessionId != null)
                {
                    ConnectionId connectionId = sessionId.ParentId;
                    if(connectionId != null)
                    {
                        ConnectionState cs = null;

                        if(connectionStates.TryGetValue(connectionId, out cs))
                        {
                            SessionState ss = cs[sessionId];
                            if(ss != null)
                            {
                                ss.AddConsumer(info);
                            }
                        }
                    }
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessRemoveConsumer(ConsumerId id)
        {
            if(id != null)
            {
                SessionId sessionId = id.ParentId;
                if(sessionId != null)
                {
                    ConnectionId connectionId = sessionId.ParentId;
                    if(connectionId != null)
                    {
                        ConnectionState cs = null;

                        if(connectionStates.TryGetValue(connectionId, out cs))
                        {
                            SessionState ss = cs[sessionId];
                            if(ss != null)
                            {
                                ss.RemoveConsumer(id);
                            }

                            cs.RecoveringPullConsumers.Remove(id);
                        }
                    }
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessAddSession(SessionInfo info)
        {
            if(info != null)
            {
                ConnectionId connectionId = info.SessionId.ParentId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        cs.AddSession(info);
                    }
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessRemoveSession(SessionId id)
        {
            if(id != null)
            {
                ConnectionId connectionId = id.ParentId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        cs.RemoveSession(id);
                    }
                }
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessAddConnection(ConnectionInfo info)
        {
            if(info != null)
            {
                ConnectionState connState = new ConnectionState(info);

                if(connectionStates.ContainsKey(info.ConnectionId))
                {
                    connectionStates[info.ConnectionId] = connState;
                }
                else
                {
                    connectionStates.Add(info.ConnectionId, connState);
                }
            }

            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessRemoveConnection(ConnectionId id)
        {
            if(id != null)
            {
                connectionStates.Remove(id);
            }
            return TRACKED_RESPONSE_MARKER;
        }

        public override Response ProcessMessage(Message send)
        {
            if(send != null)
            {
                if(TrackTransactions && send.TransactionId != null)
                {
                    ProducerId producerId = send.ProducerId;
                    ConnectionId connectionId = producerId.ParentId.ParentId;
                    if(connectionId != null)
                    {
                        ConnectionState cs = null;

                        if(connectionStates.TryGetValue(connectionId, out cs))
                        {
                            TransactionState transactionState = cs[send.TransactionId];
                            if(transactionState != null)
                            {
                                transactionState.AddCommand(send);

                                if (isTrackTransactionProducers)
                                {
                                    SessionState ss = cs[producerId.ParentId];
                                    ProducerState producerState = ss[producerId];
                                    producerState.TransactionState = transactionState;
                                }
                            }
                        }
                    }
                    return TRACKED_RESPONSE_MARKER;
                }
                else if(TrackMessages)
                {
                    messageCache.Add(send.MessageId, (Message) send.Clone());
                }
            }
            return null;
        }

        public override Response ProcessMessageAck(MessageAck ack)
        {
            if(TrackTransactions && ack != null && ack.TransactionId != null)
            {
                ConnectionId connectionId = ack.ConsumerId.ParentId.ParentId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        TransactionState transactionState = cs[ack.TransactionId];
                        if(transactionState != null)
                        {
                            transactionState.AddCommand(ack);
                        }
                    }
                }
                return TRACKED_RESPONSE_MARKER;
            }
            return null;
        }

        public override Response ProcessBeginTransaction(TransactionInfo info)
        {
            if(TrackTransactions && info != null && info.TransactionId != null)
            {
                ConnectionId connectionId = info.ConnectionId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        cs.AddTransactionState(info.TransactionId);
                        TransactionState state = cs[info.TransactionId];
                        state.AddCommand(info);
                    }
                }
                return TRACKED_RESPONSE_MARKER;
            }
            return null;
        }

        public override Response ProcessPrepareTransaction(TransactionInfo info)
        {
            if(TrackTransactions && info != null)
            {
                ConnectionId connectionId = info.ConnectionId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        TransactionState transactionState = cs[info.TransactionId];
                        if(transactionState != null)
                        {
                            transactionState.AddCommand(info);
                        }
                    }
                }
                return TRACKED_RESPONSE_MARKER;
            }
            return null;
        }

        public override Response ProcessCommitTransactionOnePhase(TransactionInfo info)
        {
            if(TrackTransactions && info != null)
            {
                ConnectionId connectionId = info.ConnectionId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        TransactionState transactionState = cs[info.TransactionId];
                        if(transactionState != null)
                        {
                            transactionState.AddCommand(info);
                            return new Tracked(new RemoveTransactionAction(info, this));
                        }
                    }
                }
            }
            return null;
        }

        public override Response ProcessCommitTransactionTwoPhase(TransactionInfo info)
        {
            if(TrackTransactions && info != null)
            {
                ConnectionId connectionId = info.ConnectionId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        TransactionState transactionState = cs[info.TransactionId];
                        if(transactionState != null)
                        {
                            transactionState.AddCommand(info);
                            return new Tracked(new RemoveTransactionAction(info, this));
                        }
                    }
                }
            }
            return null;
        }

        public override Response ProcessRollbackTransaction(TransactionInfo info)
        {
            if(TrackTransactions && info != null)
            {
                ConnectionId connectionId = info.ConnectionId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        TransactionState transactionState = cs[info.TransactionId];
                        if(transactionState != null)
                        {
                            transactionState.AddCommand(info);
                            return new Tracked(new RemoveTransactionAction(info, this));
                        }
                    }
                }
            }
            return null;
        }

        public override Response ProcessEndTransaction(TransactionInfo info)
        {
            if(TrackTransactions && info != null)
            {
                ConnectionId connectionId = info.ConnectionId;
                if(connectionId != null)
                {
                    ConnectionState cs = null;

                    if(connectionStates.TryGetValue(connectionId, out cs))
                    {
                        TransactionState transactionState = cs[info.TransactionId];
                        if(transactionState != null)
                        {
                            transactionState.AddCommand(info);
                        }
                    }
                }
                return TRACKED_RESPONSE_MARKER;
            }
            return null;
        }

        public override Response ProcessMessagePull(MessagePull pull)
        {
            if (pull != null)
            {
                // leave a single instance in the cache
                String id = pull.Destination + "::" + pull.ConsumerId;
                messageCache[id] = pull;
            }
            return null;
        }

        public bool RestoreConsumers
        {
            get { return isRestoreConsumers; }
            set { isRestoreConsumers = value; }
        }

        public bool RestoreProducers
        {
            get { return isRestoreProducers; }
            set { isRestoreProducers = value; }
        }

        public bool RestoreSessions
        {
            get { return isRestoreSessions; }
            set { isRestoreSessions = value; }
        }

        public bool TrackTransactions
        {
            get { return isTrackTransactions; }
            set { isTrackTransactions = value; }
        }

        public bool TrackTransactionProducers
        {
            get { return isTrackTransactionProducers; }
            set { isTrackTransactionProducers = value; }
        }

        public bool RestoreTransaction
        {
            get { return isRestoreTransaction; }
            set { isRestoreTransaction = value; }
        }

        public bool TrackMessages
        {
            get { return isTrackMessages; }
            set { isTrackMessages = value; }
        }

        public int MaxCacheSize
        {
            get { return maxCacheSize; }
            set
            {
                this.maxCacheSize = value;
                this.messageCache.MaxCacheSize = maxCacheSize;
            }
        }

        public void ConnectionInterruptProcessingComplete(ITransport transport, ConnectionId connectionId)
        {
            ConnectionState connectionState = null;

            if(connectionStates.TryGetValue(connectionId, out connectionState))
            {
                connectionState.ConnectionInterruptProcessingComplete = true;

                Dictionary<ConsumerId, ConsumerInfo> consumersToRestorePrefetchOn;

                lock(((ICollection) connectionState.RecoveringPullConsumers).SyncRoot)
                {
                    consumersToRestorePrefetchOn = new Dictionary<ConsumerId, ConsumerInfo>(connectionState.RecoveringPullConsumers);
                    connectionState.RecoveringPullConsumers.Clear();
                }

                foreach(KeyValuePair<ConsumerId, ConsumerInfo> entry in consumersToRestorePrefetchOn)
                {
                    ConsumerControl control = new ConsumerControl();
                    control.ConsumerId = entry.Key;
                    control.Prefetch = entry.Value.PrefetchSize;
                    control.Destination = entry.Value.Destination;
                    try
                    {
                        if(Tracer.IsDebugEnabled)
                        {
                            Tracer.Debug("restored recovering consumer: " + control.ConsumerId +
                                         " with: " + control.Prefetch);
                        }
                        transport.Oneway(control);
                    }
                    catch(Exception ex)
                    {
                        if(Tracer.IsDebugEnabled)
                        {
                            Tracer.Debug("Failed to submit control for consumer: " + control.ConsumerId +
                                         " with: " + control.Prefetch + "Error: " + ex.Message);
                        }
                    }
                }
            }
        }

        public void TransportInterrupted(ConnectionId id)
        {
            ConnectionState connection = null;

            if(connectionStates.TryGetValue(id, out connection))
            {
                connection.ConnectionInterruptProcessingComplete = false;
            }
        }
    }
}
