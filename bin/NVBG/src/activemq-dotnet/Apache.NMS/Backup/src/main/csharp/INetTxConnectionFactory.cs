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

namespace Apache.NMS
{
    /// <summary>
    /// Some application servers provide support for use in a .NET transactions (optional).
    ///
    /// To include NMS API transactions in a MSDTC transaction, an application server requires a
    /// .NET Transaction aware NMS provider that is capable of mapping the MSDTC transaction model
    /// into operations that are supported by the application server. An NMS provider exposes its
    /// .NET Transaction support using an INetTxConnectionFactory object, which an application
    /// server uses to create INetTxConnection objects.
    ///
    /// The INetTxConnectionFactory interface is optional. NMS providers are not required to support this
    /// interface. This interface is for use by NMS providers to support transactional environments.
    /// </summary>
    public interface INetTxConnectionFactory : IConnectionFactory
    {
        /// <summary>
        /// Creates a new connection
        /// </summary>
        INetTxConnection CreateNetTxConnection();

        /// <summary>
        /// Creates a new connection with the given user name and password
        /// </summary>
        INetTxConnection CreateNetTxConnection(string userName, string password);
    }
}

