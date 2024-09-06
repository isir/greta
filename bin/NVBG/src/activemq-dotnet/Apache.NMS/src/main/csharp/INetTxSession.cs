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

#if !NETCF
using System.Transactions;
#endif

namespace Apache.NMS
{
    /// <summary>
    /// The INetTxSession interface extends the capability of Session by adding access to a NMS
    /// provider's support for the Distributed Transactions (optional).  The transaction support
    /// leverages the .NET Frameworks System.Transactions API.
    ///
    /// The NMS Provider implements this interface by participating in the current ambient transaction
    /// as defined by the System.Transactions.Transaction.Current static member.  Whenever a new
    /// Transaction is entered the NMS provider should enlist in that transaction.  When there is no
    /// ambient transaction then the NMS Provider should allow the INetTxSession instance to behave
    /// as a session that is in Auto Acknowledge mode.
    ///
    /// Calling the Commit or Rollback methods on a INetTxSession instance should throw an exception
    /// as those operations are controlled by the Transaction Manager.
    ///
    /// The INetTxSession interface is optional. NMS providers are not required to support this
    /// interface. This interface is for use by NMS providers to support transactional environments.
    /// </summary>
    public interface INetTxSession : ISession
    {
#if !NETCF
        /// <summary>
        /// Enlist the Session in the specified Transaction.
        /// 
        /// If the Session is already enlisted in a Transaction or there is an Ambient
        /// Transaction and the given TX is not that Transaction then an exception should
        /// be thrown.
        /// </summary>
        void Enlist(Transaction tx);

        bool EnlistsMsDtcNativeResource { get; set; }
#endif
    }
}

