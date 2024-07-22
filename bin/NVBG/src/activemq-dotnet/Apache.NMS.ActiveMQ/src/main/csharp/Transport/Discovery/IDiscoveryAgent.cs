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
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Discovery
{
    public delegate void ServiceAddHandler(DiscoveryEvent addEvent);
    public delegate void ServiceRemoveHandler(DiscoveryEvent removeEvent);

    public interface IDiscoveryAgent : IStartable, IStoppable
    {
        /// <summary>
        /// Gets or sets the service add event handler
        /// </summary>
        ServiceAddHandler ServiceAdd
        {
            get;
            set;
        }

        /// <summary>
        /// Gets or sets the service remove event handler.
        /// </summary>
        ServiceRemoveHandler ServiceRemove
        {
            get;
            set;
        }

        /// <summary>
        /// Registers the service with the given name.
        /// </summary>
        void RegisterService(String name);

        /// <summary>
        /// A process actively using a service may see it go down before the DiscoveryAgent notices 
        /// the service's failure.  That process can use this method to notify the IDiscoveryAgent 
        /// of the failure so that other listeners of this IDiscoveryAgent can also be made aware 
        /// of the failure.
        /// </summary>
        void ServiceFailed(DiscoveryEvent failedEvent);
    
    }
}

