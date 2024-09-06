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
using System.Net;
using System.Net.Sockets;
using System.Threading;
using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Threads;
using Apache.NMS.ActiveMQ.Transport.Tcp;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Discovery.Multicast
{
	internal class MulticastDiscoveryAgent : AbstractDiscoveryAgent
	{
        public const string DEFAULT_DISCOVERY_URI_STRING = "multicast://239.255.2.3:6155";
        public const string DEFAULT_HOST_STR = "default"; 
        public const string DEFAULT_HOST_IP = "239.255.2.3";
        public const int DEFAULT_PORT = 6155;

		private const string TYPE_SUFFIX = "ActiveMQ-4.";
		private const string ALIVE = "alive";
		private const string DEAD = "dead";
		private const char DELIMITER = '%';
		private const int BUFF_SIZE = 8192;
		private const int DEFAULT_IDLE_TIME = 500;
        private const string DEFAULT_GROUP = "default";

        private const int MAX_SOCKET_CONNECTION_RETRY_ATTEMPS = 3;
        private const int SOCKET_CONNECTION_BACKOFF_TIME = 500;

        private int timeToLive = 1;
        private string group = DEFAULT_GROUP;
        private bool loopBackMode;
        private long keepAliveInterval = DEFAULT_IDLE_TIME;
        private string mcInterface;
        private string mcNetworkInterface;
        private string mcJoinNetworkInterface;

		private Socket multicastSocket;
		private IPEndPoint endPoint;

        #region Property Setters and Getters

        public bool LoopBackMode
        {
            get { return this.loopBackMode; }
            set { this.loopBackMode = value; }
        }

        public int TimeToLive
        {
            get { return this.timeToLive; }
            set { this.timeToLive = value; }
        }

        public string Interface
        {
            get { return this.mcInterface; }
            set { this.mcInterface = value; }
        }

        public string NetworkInterface
        {
            get { return this.mcNetworkInterface; }
            set { this.mcNetworkInterface = value; }
        }

        public string JoinNetworkInterface
        {
            get { return this.mcJoinNetworkInterface; }
            set { this.mcJoinNetworkInterface = value; }
        }

        public string Type
        {
            get { return this.group + "." + TYPE_SUFFIX; }
        }

        public override long KeepAliveInterval
        {
            get { return this.keepAliveInterval; }
            set { this.keepAliveInterval = value; }
        }

        #endregion

        public override String ToString()
        {
            return "MulticastDiscoveryAgent-" + (SelfService != null ? "advertise:" + SelfService : "");
        }

		protected override void DoStartAgent()
		{
            if (String.IsNullOrEmpty(group)) 
            {
                throw new IOException("You must specify a group to discover");
            }
            String type = Type;
            if (!type.EndsWith(".")) 
            {
                Tracer.Warn("The type '" + type + "' should end with '.' to be a valid Discovery type");
                type += ".";
            }
            
            if (DiscoveryURI == null) 
            {
                DiscoveryURI = new Uri(DEFAULT_DISCOVERY_URI_STRING);
            }

            if (Tracer.IsDebugEnabled) 
            {
                Tracer.Debug("start - discoveryURI = " + DiscoveryURI);                              
            }

            String targetHost = DiscoveryURI.Host;
            int targetPort = DiscoveryURI.Port;
                 
            if (DEFAULT_HOST_STR.Equals(targetHost)) 
            {
                targetHost = DEFAULT_HOST_IP;                     
            }

            if (targetPort < 0) 
            {
                targetPort = DEFAULT_PORT;              
            }
              
            if (Tracer.IsDebugEnabled) 
            {
                Tracer.DebugFormat("start - myHost = {0}", targetHost); 
                Tracer.DebugFormat("start - myPort = {0}", targetPort);    
                Tracer.DebugFormat("start - group  = {0}", group);                    
                Tracer.DebugFormat("start - interface  = {0}", mcInterface);
                Tracer.DebugFormat("start - network interface  = {0}", mcNetworkInterface);
                Tracer.DebugFormat("start - join network interface  = {0}", mcJoinNetworkInterface);
            } 

            int numFailedAttempts = 0;
            int backoffTime = SOCKET_CONNECTION_BACKOFF_TIME;

            Tracer.Info("Connecting to multicast discovery socket.");
            while (!TryToConnectSocket(targetHost, targetPort))
            {
                numFailedAttempts++;
                if (numFailedAttempts > MAX_SOCKET_CONNECTION_RETRY_ATTEMPS)
                {
                    throw new ApplicationException(
                        "Could not open the socket in order to discover advertising brokers.");
                }

                Thread.Sleep(backoffTime);
                backoffTime = (int)(backoffTime * BackOffMultiplier);
            }
		}

		protected override void DoStopAgent()
		{
            if (multicastSocket != null)
            {
                multicastSocket.Close();
            }
		}

		private bool TryToConnectSocket(string targetHost, int targetPort)
		{
			bool hasSucceeded = false;

			try
			{
				multicastSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
				endPoint = new IPEndPoint(IPAddress.Any, targetPort);

				// We have to allow reuse in the multicast socket. Otherwise, we would be unable to
                // use multiple clients on the same machine.
				multicastSocket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, 1);
				multicastSocket.Bind(endPoint);

				IPAddress ipaddress;

				if(!TcpTransportFactory.TryParseIPAddress(targetHost, out ipaddress))
				{
					ipaddress = TcpTransportFactory.GetIPAddress(targetHost, AddressFamily.InterNetwork);
					if(null == ipaddress)
					{
						throw new NMSConnectionException("Invalid host address.");
					}
				}

                if (LoopBackMode)
                {
                    multicastSocket.MulticastLoopback = true;
                }
                if (TimeToLive != 0)
                {
				    multicastSocket.SetSocketOption(SocketOptionLevel.IP, 
                                                    SocketOptionName.MulticastTimeToLive, timeToLive);
                }
                if (!String.IsNullOrEmpty(mcJoinNetworkInterface))
                {
                    // TODO figure out how to set this.
                    throw new NotSupportedException("McJoinNetworkInterface not yet implemented.");
                }
                else 
                {
                    multicastSocket.SetSocketOption(SocketOptionLevel.IP, SocketOptionName.AddMembership,
                                                     new MulticastOption(ipaddress, IPAddress.Any));
                }

                if (!String.IsNullOrEmpty(mcNetworkInterface))
                {
                    // TODO figure out how to set this.
                    throw new NotSupportedException("McNetworkInterface not yet implemented.");
                }

				multicastSocket.ReceiveTimeout = (int)keepAliveInterval;

				hasSucceeded = true;
			}
			catch(SocketException)
			{
			}

			return hasSucceeded;
		}

		protected override void DoDiscovery()
		{
			byte[] buffer = new byte[BUFF_SIZE];
			string receivedInfoRaw;
			string receivedInfo;

			try
			{
				int numBytes = multicastSocket.Receive(buffer);
				receivedInfoRaw = System.Text.Encoding.UTF8.GetString(buffer, 0, numBytes);
				// We have to remove all of the null bytes if there are any otherwise we just
				// take the whole string as is.
				if (receivedInfoRaw.IndexOf("\0") != -1)
				{
					receivedInfo = receivedInfoRaw.Substring(0, receivedInfoRaw.IndexOf("\0"));
				}
				else
				{
					receivedInfo = receivedInfoRaw;
				}

				ProcessServiceAdvertisement(receivedInfo);
			}
			catch (SocketException)
			{
				// There was no multicast message sent before the timeout expired...Let us try again.
			}
		}

        protected override void DoAdvertizeSelf()
        {
            if (!String.IsNullOrEmpty(SelfService)) 
            {
                String payload = Type;
                payload += started.Value ? ALIVE : DEAD;
                payload += DELIMITER + "localhost" + DELIMITER;
                payload += SelfService;

                byte[] data = System.Text.Encoding.UTF8.GetBytes(payload);
                multicastSocket.Send(data);
            }
        }

        private void ProcessServiceAdvertisement(string message)
        {
            string payload;
            string brokerName;
            string serviceName;

            if (message.StartsWith(Type))
            {
                payload = message.Substring(Type.Length);
                brokerName = GetBrokerName(payload);
                serviceName = GetServiceName(payload);

                if (payload.StartsWith(ALIVE))
                {
                    ProcessLiveService(brokerName, serviceName);
                }
                else if (payload.StartsWith(DEAD))
                {
                    ProcessDeadService(serviceName);
                }
                else
                {
                    // Malformed Payload
                }
            }
        }

		private static string GetBrokerName(string payload)
		{
			string[] results = payload.Split(DELIMITER);
            if (results.Length >= 2)
            {
			    return results[1];
            }
            return null;
		}

		private static string GetServiceName(string payload)
		{
			string[] results = payload.Split(DELIMITER);
            if (results.Length >= 3)
            {
                return results[2];
            }
            return null;
		}
	}
}
