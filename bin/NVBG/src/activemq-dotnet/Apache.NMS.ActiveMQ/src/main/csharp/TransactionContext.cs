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
using System.Collections;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ
{
    public enum TransactionType
    {
        Begin = 0, Prepare = 1, CommitOnePhase = 2, CommitTwoPhase = 3, Rollback = 4, Recover=5, Forget = 6, End = 7
    }
}

namespace Apache.NMS.ActiveMQ
{
    public class TransactionContext
    {
        protected readonly Session session;
        protected readonly Connection connection;
        protected readonly ArrayList synchronizations = ArrayList.Synchronized(new ArrayList());

        public TransactionContext(Session session)
        {
            this.session = session;
            this.connection = session.Connection;
        }

        public bool InTransaction
        {
            get{ return this.TransactionId != null; }
        }

        public virtual bool InLocalTransaction
        {
            get{ return this.TransactionId != null; }
        }

        public TransactionId TransactionId
        {
            get;
            protected set;
        }

        public void AddSynchronization(ISynchronization synchronization)
        {
            synchronizations.Add(synchronization);
        }

        public void RemoveSynchronization(ISynchronization synchronization)
        {
            synchronizations.Remove(synchronization);
        }

        public virtual void Begin()
        {
            if(!InTransaction)
            {
                this.TransactionId = this.session.Connection.CreateLocalTransactionId();

                TransactionInfo info = new TransactionInfo();
                info.ConnectionId = this.session.Connection.ConnectionId;
                info.TransactionId = this.TransactionId;
                info.Type = (int) TransactionType.Begin;

                this.session.Connection.Oneway(info);

                SignalTransactionStarted();

                if(Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("Begin:" + this.TransactionId);
                }
            }
        }

        public virtual void Rollback()
        {
            if(InTransaction)
            {
                try
                {
                    this.BeforeEnd();
                }
                catch (TransactionRolledBackException canOccurOnFailover)
                {
                    Tracer.WarnFormat("Rollback processing error {0}", canOccurOnFailover.Message);
                }

                if(Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("Rollback: "  + this.TransactionId +
                                 " syncCount: " +
                                 (synchronizations != null ? synchronizations.Count : 0));
                }

                TransactionInfo info = new TransactionInfo();
                info.ConnectionId = this.session.Connection.ConnectionId;
                info.TransactionId = this.TransactionId;
                info.Type = (int) TransactionType.Rollback;

                this.TransactionId = null;
                this.session.Connection.SyncRequest(info);

                this.AfterRollback();
            }
        }

        public virtual void Commit()
        {
            if(InTransaction)
            {
                try
                {
                    this.BeforeEnd();
                }
                catch
                {
                    Rollback();
                    throw;
                }

                if(Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("Commit: "  + this.TransactionId +
                                 " syncCount: " +
                                 (synchronizations != null ? synchronizations.Count : 0));
                }

                TransactionInfo info = new TransactionInfo();
                info.ConnectionId = this.session.Connection.ConnectionId;
                info.TransactionId = this.TransactionId;
                info.Type = (int) TransactionType.CommitOnePhase;

                try
                {
                    this.TransactionId = null;
                    this.session.Connection.SyncRequest(info);
                    this.AfterCommit();
                }
                catch (Exception e)
                {
                    Tracer.InfoFormat("Commit failed for transaction {0} - {1}",
                                      info.TransactionId, e.Message);
                    AfterRollback();
                    throw;
                }
            }
        }

        internal void BeforeEnd()
        {
            lock(this.synchronizations.SyncRoot)
            {
                foreach(ISynchronization synchronization in this.synchronizations)
                {
                    synchronization.BeforeEnd();
                }
            }
        }

        internal void AfterCommit()
        {
            try
            {
                lock(this.synchronizations.SyncRoot)
                {
                    foreach(ISynchronization synchronization in this.synchronizations)
                    {
                        synchronization.AfterCommit();
                    }

                    SignalTransactionCommitted();
                }
            }
            finally
            {
                synchronizations.Clear();
            }
        }

        internal void AfterRollback()
        {
            try
            {
                lock(this.synchronizations.SyncRoot)
                {
                    foreach(ISynchronization synchronization in this.synchronizations)
                    {
                        synchronization.AfterRollback();
                    }

                    SignalTransactionRolledBack();
                }
            }
            finally
            {
                synchronizations.Clear();
            }
        }

        #region Transaction State Events

        public event SessionTxEventDelegate TransactionStartedListener;
        public event SessionTxEventDelegate TransactionCommittedListener;
        public event SessionTxEventDelegate TransactionRolledBackListener;

        protected void SignalTransactionStarted()
        {
            if (this.TransactionStartedListener != null)
            {
                this.TransactionStartedListener(this.session);
            }
        }

        protected void SignalTransactionCommitted()
        {
            if (this.TransactionCommittedListener != null)
            {
                this.TransactionCommittedListener(this.session);
            }
        }

        protected void SignalTransactionRolledBack()
        {
            if (this.TransactionRolledBackListener != null)
            {
                this.TransactionRolledBackListener(this.session);
            }
        }

        #endregion

    }
}
