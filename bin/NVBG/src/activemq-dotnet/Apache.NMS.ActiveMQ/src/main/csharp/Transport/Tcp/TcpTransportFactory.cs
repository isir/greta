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
using System.Collections.Specialized;
using System.Net;
using System.Net.Sockets;
using Apache.NMS.ActiveMQ.Util;
using Apache.NMS.ActiveMQ.OpenWire;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport.Tcp
{
    [ActiveMQTransportFactory("tcp")]
    public class TcpTransportFactory : ITransportFactory
    {
        public TcpTransportFactory()
        {
        }

        #region Properties

        private bool useLogging = false;
        public bool UseLogging
        {
            get { return useLogging; }
            set { useLogging = value; }
        }

        /// <summary>
        /// Should the Inactivity Monitor be enabled on this Transport.
        /// </summary>
        private bool useInactivityMonitor = true;
        public bool UseInactivityMonitor
        {
           get { return this.useInactivityMonitor; }
           set { this.useInactivityMonitor = value; }
        }

        /// <summary>
        /// Size in bytes of the receive buffer.
        /// </summary>
        private int receiveBufferSize = 8192;
        public int ReceiveBufferSize
        {
            get { return receiveBufferSize; }
            set { receiveBufferSize = value; }
        }

        /// <summary>
        /// Size in bytes of send buffer.
        /// </summary>
        private int sendBufferSize = 8192;
        public int SendBufferSize
        {
            get { return sendBufferSize; }
            set { sendBufferSize = value; }
        }

        /// <summary>
        /// The time-out value, in milliseconds. The default value is 0, which indicates
        /// an infinite time-out period. Specifying -1 also indicates an infinite time-out period.
        /// </summary>
        private int receiveTimeout = 0;
        public int ReceiveTimeout
        {
            get { return receiveTimeout; }
            set { receiveTimeout = value; }
        }

        /// <summary>
        /// The time-out value, in milliseconds. If you set the property with a value between 1 and 499,
        /// the value will be changed to 500. The default value is 0, which indicates an infinite
        /// time-out period. Specifying -1 also indicates an infinite time-out period.
        /// </summary>
        private int sendTimeout = 0;
        public int SendTimeout
        {
            get { return sendTimeout; }
            set { sendTimeout = value; }
        }

		private int connectTimeout = 30000;
		public int ConnectTimeout
		{
			get { return connectTimeout; }
			set { this.connectTimeout = value; }
		}

        #endregion

        #region ITransportFactory Members

        public ITransport CompositeConnect(Uri location)
        {
            // Extract query parameters from broker Uri
            StringDictionary map = URISupport.ParseQuery(location.Query);

            // Set transport. properties on this (the factory)
            URISupport.SetProperties(this, map, "transport.");

			// See if there is a local address and port specified
			string localAddress = null;
			int localPort = -1;
			
			if(!String.IsNullOrEmpty(location.AbsolutePath) && !location.AbsolutePath.Equals("/"))
			{
				int index = location.AbsolutePath.IndexOf(':');
				try
				{
					localPort = Int16.Parse(location.AbsolutePath.Substring(index + 1));					
					localAddress = location.AbsolutePath.Substring(1, index - 1);
					Tracer.DebugFormat("Binding Socket to {0} on port: {1}", localAddress, localPort);
				}
				catch
				{
            		Tracer.Warn("Invalid Port value on URI for local bind option, ignoring.");
				}
			}
			
            Tracer.Debug("Opening socket to: " + location.Host + " on port: " + location.Port);            
			Socket socket = DoConnect(location.Host, location.Port, localAddress, localPort );
			
#if !NETCF
            socket.ReceiveBufferSize = ReceiveBufferSize;
            socket.SendBufferSize = SendBufferSize;
            socket.ReceiveTimeout = ReceiveTimeout;
            socket.SendTimeout = SendTimeout;
#endif

			OpenWireFormat wireformat = new OpenWireFormat();
			// Set wireformat. properties on the wireformat owned by the tcpTransport
			URISupport.SetProperties(wireformat.PreferredWireFormatInfo, map, "wireFormat.");
            ITransport transport = DoCreateTransport(location, socket, wireformat);

            wireformat.Transport = transport;

            if(UseLogging)
            {
                transport = new LoggingTransport(transport);
            }

            if(UseInactivityMonitor)
            {
               transport = new InactivityMonitor(transport);
            }

            transport = new WireFormatNegotiator(transport, wireformat);

            return transport;
        }

        public ITransport CreateTransport(Uri location)
        {
            ITransport transport = CompositeConnect(location);

            transport = new MutexTransport(transport);
            transport = new ResponseCorrelator(transport);

            return transport;
        }

        #endregion

		/// <summary>
		/// Override in a subclass to create the specific type of transport that is
		/// being implemented.
		/// </summary>
		protected virtual ITransport DoCreateTransport(Uri location, Socket socket, IWireFormat wireFormat )
		{
			TcpTransport transport = new TcpTransport(location, socket, wireFormat);

			// Apply the buffer sizes to the transport also so that it can buffer above the
			// TCP level which can eagerly send causing sparse packets.
			transport.SendBufferSize = SendBufferSize;
			transport.ReceiveBufferSize = ReceiveBufferSize;

			return transport;
		}
		
        // DISCUSSION: Caching host entries may not be the best strategy when using the
        // failover protocol.  The failover protocol needs to be very dynamic when looking
        // up hostnames at runtime.  If old hostname->IP mappings are kept around, this may
        // lead to runtime failures that could have been avoided by dynamically looking up
        // the new hostname IP.
#if CACHE_HOSTENTRIES
        private static IDictionary<string, IPHostEntry> CachedIPHostEntries = new Dictionary<string, IPHostEntry>();
        private static readonly object _syncLock = new object();
#endif
        public static IPHostEntry GetIPHostEntry(string host)
        {
            IPHostEntry ipEntry;

#if CACHE_HOSTENTRIES
            string hostUpperName = host.ToUpper();

            lock (_syncLock)
            {
                if (!CachedIPHostEntries.TryGetValue(hostUpperName, out ipEntry))
                {
                    try
                    {
                        ipEntry = Dns.GetHostEntry(hostUpperName);
                        CachedIPHostEntries.Add(hostUpperName, ipEntry);
                    }
                    catch
                    {
                        ipEntry = null;
                    }
                }
            }
#else
            try
            {
                ipEntry = Dns.GetHostEntry(host);
            }
            catch
            {
                ipEntry = null;
            }
#endif

            return ipEntry;
        }

        private Socket TryConnectSocket(IPAddress address, int port, string localAddress, int localPort)
        {
            if(null != address)
            {
                try
                {
                    Socket socket = new Socket(address.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

                    if(null != socket)
                    {
						if(!String.IsNullOrEmpty(localAddress))
						{
							DoBind(socket, localAddress, localPort);
						}

						IAsyncResult result = socket.BeginConnect(new IPEndPoint(address, port), null, null);
						result.AsyncWaitHandle.WaitOne(ConnectTimeout, true);
						if(!socket.Connected)
						{
				            socket.Close();
						}
						else
						{
							return socket;
						}
                    }
                }
                catch
                {
                }
            }

            return null;
        }

        public static bool TryParseIPAddress(string host, out IPAddress ipaddress)
        {
#if !NETCF
            return IPAddress.TryParse(host, out ipaddress);
#else
            try
            {
                ipaddress = IPAddress.Parse(host);
            }
            catch
            {
                ipaddress = null;
            }

            return (null != ipaddress);
#endif
        }

        public static IPAddress GetIPAddress(string hostname, AddressFamily addressFamily)
        {
            IPAddress ipaddress = null;
            IPHostEntry hostEntry = GetIPHostEntry(hostname);

            if(null != hostEntry)
            {
                ipaddress = GetIPAddress(hostEntry, addressFamily);
            }

            return ipaddress;
        }

        public static IPAddress GetIPAddress(IPHostEntry hostEntry, AddressFamily addressFamily)
        {
            if(null != hostEntry)
            {
                foreach(IPAddress address in hostEntry.AddressList)
                {
                    if(address.AddressFamily == addressFamily)
                    {
                        return address;
                    }
                }
            }

            return null;
        }

        protected Socket DoConnect(string host, int port, string localAddress, int localPort)
        {
            Socket socket = null;
            IPAddress ipaddress;

            try
            {
                if(TryParseIPAddress(host, out ipaddress))
                {
                    socket = TryConnectSocket(ipaddress, port, localAddress, localPort);
                }
                else
                {
                    // Looping through the AddressList allows different type of connections to be tried
                    // (IPv6, IPv4 and whatever else may be available).
                    IPHostEntry hostEntry = GetIPHostEntry(host);

                    if(null != hostEntry)
                    {
                        // Prefer IPv6 first.
                        ipaddress = GetIPAddress(hostEntry, AddressFamily.InterNetworkV6);
                        socket = TryConnectSocket(ipaddress, port, localAddress, localPort);
                        if(null == socket)
                        {
                            // Try IPv4 next.
                            ipaddress = GetIPAddress(hostEntry, AddressFamily.InterNetwork);
                            socket = TryConnectSocket(ipaddress, port, localAddress, localPort);
                            if(null == socket)
                            {
                                // Try whatever else there is.
                                foreach(IPAddress address in hostEntry.AddressList)
                                {
                                    if(AddressFamily.InterNetworkV6 == address.AddressFamily
                                        || AddressFamily.InterNetwork == address.AddressFamily)
                                    {
                                        // Already tried these protocols.
                                        continue;
                                    }

                            		socket = TryConnectSocket(ipaddress, port, localAddress, localPort);
                                    if(null != socket)
                                    {
                                        ipaddress = address;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if(null == socket)
                {
                    const int RTSSL_HANDSHAKE_FAILURE = -2;
                    throw new SocketException(RTSSL_HANDSHAKE_FAILURE);
                }
            }
            catch(Exception ex)
            {
                throw new NMSConnectionException(String.Format("Error connecting to {0}:{1}.", host, port), ex);
            }

            Tracer.DebugFormat("Connected to {0}:{1} using {2} protocol.", host, port, ipaddress.AddressFamily.ToString());
            return socket;
        }
		
        protected void DoBind(Socket socket, string host, int port)
        {
            IPAddress ipaddress;

            try
            {
                if(TryParseIPAddress(host, out ipaddress))
                {
                    TryBindSocket(socket, ipaddress, port);
                }
                else
                {
                    // Looping through the AddressList allows different type of connections to be tried
                    // (IPv6, IPv4 and whatever else may be available).
                    IPHostEntry hostEntry = GetIPHostEntry(host);

                    if(null != hostEntry)
                    {
                        // Prefer IPv6 first.
                        ipaddress = GetIPAddress(hostEntry, AddressFamily.InterNetworkV6);
                        if(!TryBindSocket(socket, ipaddress, port))
                        {
                            // Try IPv4 next.
                            ipaddress = GetIPAddress(hostEntry, AddressFamily.InterNetwork);
	                        if(!TryBindSocket(socket, ipaddress, port))
                            {
                                // Try whatever else there is.
                                foreach(IPAddress address in hostEntry.AddressList)
                                {
                                    if(AddressFamily.InterNetworkV6 == address.AddressFamily
                                        || AddressFamily.InterNetwork == address.AddressFamily)
                                    {
                                        // Already tried these protocols.
                                        continue;
                                    }

                        			if(TryBindSocket(socket, ipaddress, port))
                                    {
                                        ipaddress = address;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if(!socket.IsBound)
                {
                    throw new SocketException();
                }
            }
            catch(Exception ex)
            {
                throw new NMSConnectionException(String.Format("Error binding to {0}:{1}.", host, port), ex);
            }

            Tracer.DebugFormat("Bound to {0}:{1} using.", host, port);
        }		
		
        private bool TryBindSocket(Socket socket, IPAddress address, int port)
        {
            if(null != socket && null != address)
            {
                try
                {
                    socket.Bind(new IPEndPoint(address, port));
                    if(socket.IsBound)
                    {
                        return true;
                    }
                }
                catch
                {
                }
            }

            return false;
        }
		
    }
}
