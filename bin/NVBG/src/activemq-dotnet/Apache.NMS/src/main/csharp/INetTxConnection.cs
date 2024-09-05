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
    /// The INetTxConnection extends the functionality of the IConnection interface by
    /// adding the createNetTxSession method (optional).
    ///
    /// The INetTxConnection interface is optional. NMS providers are not required to support this
    /// interface. This interface is for use by NMS providers to support transactional environments.
    /// </summary>
    public interface INetTxConnection : IConnection
    {
        /// <summary>
        /// Creates a INetTxSession object.
        /// </summary>
        INetTxSession CreateNetTxSession();

#if !NETCF
        /// <summary>
        /// Creates a INetTxSession object and enlists in the specified Transaction.
        /// </summary>
        INetTxSession CreateNetTxSession(Transaction tx);

        INetTxSession CreateNetTxSession(bool enlistsNativeMsDtcResource);

        INetTxSession CreateNetTxSession(Transaction tx, bool enlistsNativeMsDtcResource);
#endif
    }
}

