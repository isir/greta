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
using System.Threading;
using System.Transactions;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ
{
    public sealed class NetTxSession : Session, INetTxSession
    {
        private readonly NetTxTransactionContext transactionContext;
        private string currentTransactionId;

        public NetTxSession(Connection connection, SessionId id)
            : base(connection, id, AcknowledgementMode.AutoAcknowledge)
        {
            this.transactionContext = TransactionContext as NetTxTransactionContext;
            this.transactionContext.InitializeDtcTxContext();
            this.enlistMsDtcNativeResources = false;
        }

        /// <summary>
        /// Manually Enlists in the given Transaction.  This can be used to when the
        /// client is using the Session in Asynchronous listener mode since the Session
        /// cannot atuomatically join in this case as there is no Ambient transaction in
        /// the Message Dispatch thread.  This also allows for clients to use the explicit
        /// exception model when necessary.  
        /// </summary>
        public void Enlist(Transaction tx)
        {
            if(tx == null)
            {
                throw new NullReferenceException("Specified Transaction cannot be null");
            }

            this.EnrollInSpecifiedTransaction(tx);
        }

        private bool enlistMsDtcNativeResources;

        public bool EnlistsMsDtcNativeResource
        {
            get { return enlistMsDtcNativeResources; }
            set { enlistMsDtcNativeResources = value; }
        }

        /// <summary>
        /// Reports Transacted whenever there is an Ambient Transaction or the internal
        /// TransactionContext is still involed in a .NET Transaction beyond the lifetime
        /// of an ambient transaction (can happen during a scoped transaction disposing
        /// without Complete being called and a Rollback is in progress.)
        /// </summary>
        public override bool IsTransacted
        {
            get { return Transaction.Current != null || transactionContext.InNetTransaction; }
        }

        public override bool IsAutoAcknowledge
        {
            // When not in a .NET Transaction we assume Auto Ack.
            get { return true; }
        }

        public override void Close()
        {
            if (this.closed)
            {
                return;
            }

            try
            {
                if (transactionContext.InNetTransaction)
                {
                    lock (transactionContext.SyncRoot)
                    {
                        if (transactionContext.InNetTransaction)
                        {
                            // Must wait for all the DTC operations to complete before
                            // moving on from this close call.
                            Monitor.Exit(transactionContext.SyncRoot);
                            this.transactionContext.DtcWaitHandle.WaitOne();
                            Monitor.Enter(transactionContext.SyncRoot);
                        }
                    }
                }

                base.Close();
            }
            catch (Exception ex)
            {
                Tracer.ErrorFormat("Error during session close: {0}", ex);
            }
        }

        internal override MessageConsumer DoCreateMessageConsumer(
            ConsumerId id, ActiveMQDestination destination, string name, string selector,
            int prefetch, int maxPending, bool noLocal)
        {
            return new NetTxMessageConsumer(this, id, destination, name, selector, prefetch,
                                            maxPending, noLocal, false, this.DispatchAsync);
        }

        protected override TransactionContext CreateTransactionContext()
        {
            return new NetTxTransactionContext(this);
        }

        internal override void DoRollback()
        {
            // Only the Transaction Manager can do this when in a .NET Transaction.
            throw new TransactionInProgressException("Cannot Rollback() inside an NetTxSession");
        }

        internal override void DoCommit()
        {
            // Only the Transaction Manager can do this when in a .NET Transaction.
            throw new TransactionInProgressException("Cannot Commit() inside an NetTxSession");
        }

        internal override void DoStartTransaction()
        {
            lock (transactionContext.SyncRoot)
            {
                while (transactionContext.InNetTransaction &&
                       (transactionContext.NetTxState == NetTxTransactionContext.TxState.Pending ||
                       (Transaction.Current != null && 
                        this.currentTransactionId != Transaction.Current.TransactionInformation.LocalIdentifier)))
                {
                    if (Tracer.IsDebugEnabled)
                    {
                        Tracer.DebugFormat("NetTxSession awaiting completion of TX:{0}", transactionContext.TransactionId);
                    }
                    // To late to participate in this TX, we have to wait for it to complete then
                    // we can create a new TX and start from there.
                    Monitor.Exit(transactionContext.SyncRoot);
                    transactionContext.DtcWaitHandle.WaitOne();
                    Monitor.Enter(transactionContext.SyncRoot);
                }
 
                if (!transactionContext.InNetTransaction && Transaction.Current != null)
                {
                    Tracer.Debug("NetTxSession detected Ambient Transaction, start new TX with broker");
                    EnrollInSpecifiedTransaction(Transaction.Current);
                }
            }
        }

        private void EnrollInSpecifiedTransaction(Transaction tx)
        {
            if(transactionContext.InNetTransaction)
            {
                Tracer.Warn("Enlist attempted while a Net TX was Active.");
                throw new InvalidOperationException("Session is Already enlisted in a Transaction");
            }

            if(Transaction.Current != null && !Transaction.Current.Equals(tx))
            {
                Tracer.Warn("Enlist attempted with a TX that doesn't match the Ambient TX.");
                throw new ArgumentException("Specified TX must match the ambient TX if set.");
            }
            
            // Start a new .NET style transaction, this could be distributed
            // or it could just be a Local transaction that could become
            // distributed later.
            this.currentTransactionId = tx.TransactionInformation.LocalIdentifier; 
            transactionContext.Begin(tx);
        }
        
    }
}

