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
using System.Web;
using System.Net.Sockets;

namespace Apache.NMS.ActiveMQ.Transport.Tcp
{
    [ActiveMQTransportFactory("ssl")]
	public class SslTransportFactory : TcpTransportFactory
	{
        private string serverName;
        private string clientCertSubject;
        private string clientCertFilename;
        private string clientCertPassword;
        private string brokerCertFilename;
        private string keyStoreName;
        private string keyStoreLocation;
        private string sslProtocol;
        private bool acceptInvalidBrokerCert = false;
        
        public SslTransportFactory() : base()
        {
        }

        public string ServerName
        {
            get { return this.serverName; }
            set { this.serverName = value; }
        }

        public string ClientCertSubject
        {
            get { return this.clientCertSubject; }
            set { this.clientCertSubject = value; }
        }

        public string ClientCertFilename
        {
            get { return this.clientCertFilename; }
            set { this.clientCertFilename = value; }
        }

        public string ClientCertPassword
        {
            get { return this.clientCertPassword; }
            set { this.clientCertPassword = value; }
        }        

        public string BrokerCertFilename
        {
            get { return this.brokerCertFilename; }
            set { this.brokerCertFilename = value; }
        }

        public bool AcceptInvalidBrokerCert
        {
            get { return this.acceptInvalidBrokerCert; }
            set { this.acceptInvalidBrokerCert = value; }
        }

        public string KeyStoreName
        {
            get { return this.keyStoreName; }
            set { this.keyStoreName = value; }
        }

        public string KeyStoreLocation
        {
            get { return this.keyStoreLocation; }
            set { this.keyStoreLocation = value; }
        }

        public string SslProtocol
        {
            get { return this.sslProtocol; }
            set { this.sslProtocol = value; }
        }

		protected override ITransport DoCreateTransport(Uri location, Socket socket, IWireFormat wireFormat )
		{
            Tracer.Debug("Creating new instance of the SSL Transport.");
			SslTransport transport = new SslTransport(location, socket, wireFormat);

            transport.ClientCertSubject = HttpUtility.UrlDecode(this.clientCertSubject);
            transport.ClientCertFilename = this.clientCertFilename;
            transport.ClientCertPassword = this.clientCertPassword;
            transport.BrokerCertFilename = this.brokerCertFilename;
            transport.ServerName = this.serverName;
            transport.KeyStoreLocation = this.keyStoreLocation;
            transport.KeyStoreName = this.keyStoreName;
            transport.AcceptInvalidBrokerCert = this.acceptInvalidBrokerCert;
            transport.SslProtocol = this.sslProtocol;
            
            return transport;
		}		
	}
}
