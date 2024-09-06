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
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Transactions;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Transactions;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ
{
    public sealed class NetTxTransactionContext : TransactionContext, ISinglePhaseNotification
    {
        private const int XA_OK = 0;
        private const int XA_READONLY = 3;

        private Enlistment currentEnlistment;
        private static readonly Dictionary<string, bool> recoveredResourceManagerIds = new Dictionary<string, bool>();

        public NetTxTransactionContext(Session session) : base(session)
        {
        }

        /// <summary>
        /// DTC recovery is performed once for each AppDomain per default. In case you want to perform
        /// it again during execution of the application you can call this method before.
        /// But ensure in this case that no connection is active anymore.
        /// </summary>
        public static void ResetDtcRecovery()
        {
            recoveredResourceManagerIds.Clear();
        }

        public override bool InLocalTransaction
        {
            get { return this.TransactionId != null && this.currentEnlistment == null; }
        }

        public override void Begin()
        {
            throw new IllegalStateException("Local Transactions not supported in NetTx resources");
        }

        public override void Commit()
        {
            throw new IllegalStateException("Local Transactions not supported in NetTx resources");
        }

        public override void Rollback()
        {
            throw new IllegalStateException("Local Transactions not supported in NetTx resources");
        }

        #region Transaction Members used when dealing with .NET System Transactions.

        // When DTC calls prepare we must then wait for either the TX to commit, rollback or
        // be canceled because its in doubt.
        private readonly ManualResetEvent dtcControlEvent = new ManualResetEvent(true);

        // Once the DTC calls prepare we lock this object and don't unlock it again until
        // the TX has either completed or terminated, the users of this class should use
        // this sync point when the TX is a DTC version as opposed to a local one.
        private readonly object syncObject = new Mutex();

        public enum TxState
        {
            None = 0, Active = 1, Pending = 2
        }

        private TxState netTxState = TxState.None;

        public object SyncRoot
        {
            get { return this.syncObject; }
        }

        public bool InNetTransaction
        {
            get { return this.TransactionId != null && this.TransactionId is XATransactionId; }
        }

        public TxState NetTxState
        {
            get
            {
                return this.netTxState;
            }
        }

        public WaitHandle DtcWaitHandle
        {
            get { return dtcControlEvent; }
        }

        public void Begin(Transaction transaction)
        {
            lock (syncObject)
            {
                dtcControlEvent.Reset();

                Tracer.Debug("Begin notification received");

                if (InNetTransaction)
                {
                    throw new TransactionInProgressException("A Transaction is already in Progress");
                }

                try
                {
                    Guid rmId = ResourceManagerGuid;

                    // Enlist this object in the transaction.
                    this.currentEnlistment =
                        transaction.EnlistDurable(rmId, this, EnlistmentOptions.None);

                    // In case of a exception in the current method the transaction will be rolled back.
                    // Until Begin Transaction is completed we consider to be in a rollback scenario.
                    this.netTxState = TxState.Pending;

                    Tracer.Debug("Enlisted in Durable Transaction with RM Id: " + rmId);

                    TransactionInformation txInfo = transaction.TransactionInformation;

                    XATransactionId xaId = new XATransactionId();
                    this.TransactionId = xaId;

                    if (txInfo.DistributedIdentifier != Guid.Empty)
                    {
                        xaId.GlobalTransactionId = txInfo.DistributedIdentifier.ToByteArray();
                        xaId.BranchQualifier = Encoding.UTF8.GetBytes(Guid.NewGuid().ToString());
                    }
                    else
                    {
                        xaId.GlobalTransactionId = Encoding.UTF8.GetBytes(txInfo.LocalIdentifier);
                        xaId.BranchQualifier = Encoding.UTF8.GetBytes(Guid.NewGuid().ToString());
                    }

                    // Now notify the broker that a new XA'ish transaction has started.
                    TransactionInfo info = new TransactionInfo();
                    info.ConnectionId = this.connection.ConnectionId;
                    info.TransactionId = this.TransactionId;
                    info.Type = (int)TransactionType.Begin;

                    this.session.Connection.Oneway(info);

                    // Begin Transaction is completed successfully. Change to transaction active state now.
                    this.netTxState = TxState.Active;

                    SignalTransactionStarted();

                    if (Tracer.IsDebugEnabled)
                    {
                        Tracer.Debug("Began XA'ish Transaction:" + xaId);
                    }
                }
                catch (Exception)
                {
                    // When in pending state the rollback will signal that a new transaction can be started. Otherwise do it here.
                    if (netTxState != TxState.Pending)
                    {
                        netTxState = TxState.None;
                        dtcControlEvent.Set();
                    }
                    throw;
                }
            }
        }

        public void Prepare(PreparingEnlistment preparingEnlistment)
        {
            lock (this.syncObject)
            {
                this.netTxState = TxState.Pending;

                try
                {
                    Tracer.Debug("Prepare notification received for TX id: " + this.TransactionId);

                    BeforeEnd();

                    // Before sending the request to the broker, log the recovery bits, if
                    // this fails we can't prepare and the TX should be rolled back.
                    RecoveryLogger.LogRecoveryInfo(this.TransactionId as XATransactionId,
                                                   preparingEnlistment.RecoveryInformation());

                    // Inform the broker that work on the XA'sh TX Branch is complete.
                    TransactionInfo info = new TransactionInfo();
                    info.ConnectionId = this.connection.ConnectionId;
                    info.TransactionId = this.TransactionId;
                    info.Type = (int)TransactionType.End;

                    this.connection.CheckConnected();
                    this.connection.SyncRequest((TransactionInfo) info.Clone());

                    // Prepare the Transaction for commit.
                    info.Type = (int)TransactionType.Prepare;
                    IntegerResponse response = (IntegerResponse)this.connection.SyncRequest(info);
                    if (response.Result == XA_READONLY)
                    {
                        Tracer.Debug("Transaction Prepare done and doesn't need a commit, TX id: " + this.TransactionId);

                        this.TransactionId = null;
                        this.currentEnlistment = null;

                        // Read Only means there's nothing to recover because there was no
                        // change on the broker.
                        RecoveryLogger.LogRecovered(this.TransactionId as XATransactionId);

                        // if server responds that nothing needs to be done, then reply done.
                        // otherwise the DTC will call Commit or Rollback but another transaction
                        // can already be in progress and this one would be commited or rolled back
                        // immediately.
                        preparingEnlistment.Done();

                        // Done so commit won't be called.
                        AfterCommit();

                        // A Read-Only TX is considered closed at this point, DTC won't call us again.
                        this.dtcControlEvent.Set();
                    }
                    else
                    {
                        Tracer.Debug("Transaction Prepare succeeded TX id: " + this.TransactionId);

                        // If work finished correctly, reply prepared
                        preparingEnlistment.Prepared();
                    }
                }
                catch (Exception ex)
                {
                    Tracer.DebugFormat("Transaction[{0}] Prepare failed with error: {1}",
                                       this.TransactionId, ex.Message);

                    AfterRollback();
                    preparingEnlistment.ForceRollback();
                    try
                    {
                        this.connection.OnAsyncException(ex);
                    }
                    catch (Exception error)
                    {
                        Tracer.Error(error.ToString());
                    }

                    this.currentEnlistment = null;
                    this.TransactionId = null;
                    this.netTxState = TxState.None;
                    this.dtcControlEvent.Set();
                }
            }
        }

        public void Commit(Enlistment enlistment)
        {
            lock (this.syncObject)
            {
                try
                {
                    Tracer.Debug("Commit notification received for TX id: " + this.TransactionId);

                    if (this.TransactionId != null)
                    {
                        // Now notify the broker that a new XA'ish transaction has completed.
                        TransactionInfo info = new TransactionInfo();
                        info.ConnectionId = this.connection.ConnectionId;
                        info.TransactionId = this.TransactionId;
                        info.Type = (int)TransactionType.CommitTwoPhase;

                        this.connection.CheckConnected();
                        this.connection.SyncRequest(info);

                        Tracer.Debug("Transaction Commit Done TX id: " + this.TransactionId);

                        RecoveryLogger.LogRecovered(this.TransactionId as XATransactionId);

                        // if server responds that nothing needs to be done, then reply done.
                        enlistment.Done();

                        AfterCommit();
                    }
                }
                catch (Exception ex)
                {
                    Tracer.DebugFormat("Transaction[{0}] Commit failed with error: {1}",
                                       this.TransactionId, ex.Message);
                    try
                    {
                        this.connection.OnAsyncException(ex);
                    }
                    catch (Exception error)
                    {
                        Tracer.Error(error.ToString());
                    }
                }
                finally
                {
                    this.currentEnlistment = null;
                    this.TransactionId = null;
                    this.netTxState = TxState.None;

                    CountDownLatch latch = this.recoveryComplete;
                    if (latch != null)
                    {
                        latch.countDown();
                    }

                    this.dtcControlEvent.Set();
                }
            }
        }

        public void SinglePhaseCommit(SinglePhaseEnlistment enlistment)
        {
            lock (this.syncObject)
            {
                try
                {
                    Tracer.Debug("Single Phase Commit notification received for TX id: " + this.TransactionId);

                    if (this.TransactionId != null)
                    {
                        BeforeEnd();

                        // Now notify the broker that a new XA'ish transaction has completed.
                        TransactionInfo info = new TransactionInfo();
                        info.ConnectionId = this.connection.ConnectionId;
                        info.TransactionId = this.TransactionId;
                        info.Type = (int)TransactionType.CommitOnePhase;

                        this.connection.CheckConnected();
                        this.connection.SyncRequest(info);

                        Tracer.Debug("Transaction Single Phase Commit Done TX id: " + this.TransactionId);

                        // if server responds that nothing needs to be done, then reply done.
                        enlistment.Done();

                        AfterCommit();
                    }
                }
                catch (Exception ex)
                {
                    Tracer.DebugFormat("Transaction[{0}] Single Phase Commit failed with error: {1}",
                                       this.TransactionId, ex.Message);
                    AfterRollback();
                    enlistment.Done();
                    try
                    {
                        this.connection.OnAsyncException(ex);
                    }
                    catch (Exception error)
                    {
                        Tracer.Error(error.ToString());
                    }
                }
                finally
                {
                    this.currentEnlistment = null;
                    this.TransactionId = null;
                    this.netTxState = TxState.None;

                    this.dtcControlEvent.Set();
                }
            }
        }

        public void Rollback(Enlistment enlistment)
        {
            lock (this.syncObject)
            {
                try
                {
                    Tracer.Debug("Rollback notification received for TX id: " + this.TransactionId);

                    if (this.TransactionId != null)
                    {
                        BeforeEnd();

                        // Now notify the broker that a new XA'ish transaction has started.
                        TransactionInfo info = new TransactionInfo();
                        info.ConnectionId = this.connection.ConnectionId;
                        info.TransactionId = this.TransactionId;
                        info.Type = (int)TransactionType.End;

                        this.connection.CheckConnected();
                        this.connection.SyncRequest((TransactionInfo) info.Clone());

                        info.Type = (int)TransactionType.Rollback;
                        this.connection.CheckConnected();
                        this.connection.SyncRequest(info);

                        Tracer.Debug("Transaction Rollback Done TX id: " + this.TransactionId);

                        RecoveryLogger.LogRecovered(this.TransactionId as XATransactionId);

                        // if server responds that nothing needs to be done, then reply done.
                        enlistment.Done();

                        AfterRollback();
                    }
                }
                catch (Exception ex)
                {
                    Tracer.DebugFormat("Transaction[{0}] Rollback failed with error: {1}",
                                       this.TransactionId, ex.Message);
                    AfterRollback();
                    try
                    {
                        this.connection.OnAsyncException(ex);
                    }
                    catch (Exception error)
                    {
                        Tracer.Error(error.ToString());
                    }
                }
                finally
                {
                    this.currentEnlistment = null;
                    this.TransactionId = null;
                    this.netTxState = TxState.None;

                    CountDownLatch latch = this.recoveryComplete;
                    if (latch != null)
                    {
                        latch.countDown();
                    }

                    this.dtcControlEvent.Set();
                }
            }
        }

        public void InDoubt(Enlistment enlistment)
        {
            lock (syncObject)
            {
                try
                {
                    Tracer.Debug("In Doubt notification received for TX id: " + this.TransactionId);

                    BeforeEnd();

                    // Now notify the broker that Rollback should be performed.
                    TransactionInfo info = new TransactionInfo();
                    info.ConnectionId = this.connection.ConnectionId;
                    info.TransactionId = this.TransactionId;
                    info.Type = (int)TransactionType.End;

                    this.connection.CheckConnected();
                    this.connection.SyncRequest((TransactionInfo) info.Clone());

                    info.Type = (int)TransactionType.Rollback;
                    this.connection.CheckConnected();
                    this.connection.SyncRequest(info);

                    Tracer.Debug("InDoubt Transaction Rollback Done TX id: " + this.TransactionId);

                    RecoveryLogger.LogRecovered(this.TransactionId as XATransactionId);

                    // if server responds that nothing needs to be done, then reply done.
                    enlistment.Done();

                    AfterRollback();
                }
                finally
                {
                    this.currentEnlistment = null;
                    this.TransactionId = null;
                    this.netTxState = TxState.None;

                    CountDownLatch latch = this.recoveryComplete;
                    if (latch != null)
                    {
                        latch.countDown();
                    }

                    this.dtcControlEvent.Set();
                }
            }
        }

        #endregion

        #region Distributed Transaction Recovery Bits

        private volatile CountDownLatch recoveryComplete;

        /// <summary>
        /// Should be called from NetTxSession when created to check if any TX
        /// data is stored for recovery and whether the Broker has matching info
        /// stored.  If an Transaction is found that belongs to this client and is
        /// still alive on the Broker it will be recovered, otherwise the stored
        /// data should be cleared.
        /// </summary>
        public void InitializeDtcTxContext()
        {
            string resourceManagerId = ResourceManagerId;

            // initialize the logger with the current Resource Manager Id
            RecoveryLogger.Initialize(resourceManagerId);

            lock (recoveredResourceManagerIds)
            {
                if (recoveredResourceManagerIds.ContainsKey(resourceManagerId))
                {
                    return;
                }

                recoveredResourceManagerIds[resourceManagerId] = true;

                KeyValuePair<XATransactionId, byte[]>[] localRecoverables = RecoveryLogger.GetRecoverables();
                if (localRecoverables.Length == 0)
                {
                    Tracer.Debug("Did not detect any open DTC transaction records on disk.");
                    // No local data so anything stored on the broker can't be recovered here.
                    return;
                }

                XATransactionId[] recoverables = TryRecoverBrokerTXIds();
                if (recoverables.Length == 0)
                {
                    Tracer.Debug("Did not detect any recoverable transactions at Broker.");
                    // Broker has no recoverable data so nothing to do here, delete the 
                    // old recovery log as its stale.
                    RecoveryLogger.Purge();
                    return;
                }

                List<KeyValuePair<XATransactionId, byte[]>> matches = new List<KeyValuePair<XATransactionId, byte[]>>();

                foreach (XATransactionId recoverable in recoverables)
                {
                    foreach (KeyValuePair<XATransactionId, byte[]> entry in localRecoverables)
                    {
                        if (entry.Key.Equals(recoverable))
                        {
                            Tracer.DebugFormat("Found a matching TX on Broker to stored Id: {0} reenlisting.", entry.Key);
                            matches.Add(entry);
                        }
                    }
                }

                if (matches.Count != 0)
                {
                    this.recoveryComplete = new CountDownLatch(matches.Count);

                    foreach (KeyValuePair<XATransactionId, byte[]> recoverable in matches)
                    {
                        this.TransactionId = recoverable.Key;
                        Tracer.Info("Reenlisting recovered TX with Id: " + this.TransactionId);
                        this.currentEnlistment =
                            TransactionManager.Reenlist(ResourceManagerGuid, recoverable.Value, this);
                    }

                    this.recoveryComplete.await();
                    Tracer.Debug("All Recovered TX enlistments Reports complete, Recovery Complete.");
                    TransactionManager.RecoveryComplete(ResourceManagerGuid);
                    return;
                }

                // The old recovery information doesn't match what's on the broker so we
                // should discard it as its stale now.
                RecoveryLogger.Purge();
            }
        }

        private XATransactionId[] TryRecoverBrokerTXIds()
        {
            Tracer.Debug("Checking for Recoverable Transactions on Broker.");

            TransactionInfo info = new TransactionInfo();
            info.ConnectionId = this.session.Connection.ConnectionId;
            info.Type = (int)TransactionType.Recover;

            this.connection.CheckConnected();
            DataArrayResponse response = this.connection.SyncRequest(info) as DataArrayResponse;

            if (response != null && response.Data.Length > 0)
            {
                Tracer.DebugFormat("Broker reports there are {0} recoverable XA Transactions", response.Data.Length);

                List<XATransactionId> recovered = new List<XATransactionId>();

                foreach (DataStructure ds in response.Data)
                {
                    XATransactionId xid = ds as XATransactionId;
                    if (xid != null)
                    {
                        recovered.Add(xid);
                    }
                }

                return recovered.ToArray();
            }

            return new XATransactionId[0];
        }

        #endregion

        internal IRecoveryLogger RecoveryLogger
        {
            get { return (this.connection as NetTxConnection).RecoveryPolicy.RecoveryLogger; }
        }

        internal string ResourceManagerId
        {
            get { return (this.connection as NetTxConnection).ResourceManagerGuid.ToString(); }
        }

        internal Guid ResourceManagerGuid
        {
            get { return (this.connection as NetTxConnection).ResourceManagerGuid; }
        }

    }
}
