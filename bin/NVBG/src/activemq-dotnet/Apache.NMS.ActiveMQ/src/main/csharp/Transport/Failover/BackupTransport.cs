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
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Failover
{
    class BackupTransport
    {
        private readonly FailoverTransport failoverTransport;
        private ITransport transport;
        private Uri uri;
        private bool disposed;

        public BackupTransport(FailoverTransport ft)
        {
            this.failoverTransport = ft;
        }

        public void OnCommand(ITransport t, Command c)
        {
        }

        public void OnException(ITransport t, Exception error)
        {
            this.disposed = true;
            if(failoverTransport != null)
            {
                this.failoverTransport.Reconnect(false);
            }
        }

        public ITransport Transport
        {
            get
            {
                return transport;
            }
            set
            {
                transport = value;
            }
        }

        public Uri Uri
        {
            get
            {
                return uri;
            }
            set
            {
                uri = value;
            }
        }

        public bool Disposed
        {
            get
            {
                return disposed || (transport != null && transport.IsDisposed);
            }
            set
            {
                disposed = value;
                if(disposed && transport != null)
                {
                    transport.Dispose();
                }
            }
        }

        public int hashCode()
        {
            return uri != null ? uri.GetHashCode() : -1;
        }

        public bool equals(Object obj)
        {
            if(obj is BackupTransport)
            {
                BackupTransport other = obj as BackupTransport;
                return uri == null && other.uri == null ||
                    (uri != null && other.uri != null && uri.Equals(other.uri));
            }
            return false;
        }
    }
}
