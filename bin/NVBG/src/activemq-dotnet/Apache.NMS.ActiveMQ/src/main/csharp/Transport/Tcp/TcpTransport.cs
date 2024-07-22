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
using System.IO;
using System.Net.Sockets;
using System.Threading;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport.Tcp
{
	/// <summary>
	/// An implementation of ITransport that uses sockets to communicate with the broker
	/// </summary>
	public class TcpTransport : ITransport
	{
		protected readonly object myLock = new object();
		protected readonly Socket socket;
		private IWireFormat wireformat;
		private BinaryReader socketReader;
		private BinaryWriter socketWriter;
		private Thread readThread;
		private bool started;
		private bool disposed = false;
		private readonly Atomic<bool> closed = new Atomic<bool>(false);
		private volatile bool seenShutdown;
		private readonly Uri connectedUri;
		private int timeout = -1;
		private int asynctimeout = -1;

		private CommandHandler commandHandler;
		private ExceptionHandler exceptionHandler;
		private InterruptedHandler interruptedHandler;
		private ResumedHandler resumedHandler;
		private TimeSpan MAX_THREAD_WAIT = TimeSpan.FromMilliseconds(5000);

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

		public TcpTransport(Uri uri, Socket socket, IWireFormat wireformat)
		{
			this.connectedUri = uri;
			this.socket = socket;
			this.wireformat = wireformat;
		}

		~TcpTransport()
		{
			Dispose(false);
		}

		protected virtual Stream CreateSocketStream()
		{
			return new NetworkStream(socket);
		}

		/// <summary>
		/// Method Start
		/// </summary>
		public void Start()
		{
			lock(myLock)
			{
				if(!started)
				{
					if(null == commandHandler)
					{
						throw new InvalidOperationException(
								"command cannot be null when Start is called.");
					}

					if(null == exceptionHandler)
					{
						throw new InvalidOperationException(
								"exception cannot be null when Start is called.");
					}

					started = true;

					// Initialize our Read and Writer instances.  Its not actually necessary
					// to have two distinct NetworkStream instances but for now the TcpTransport
					// will continue to do so for legacy reasons.
					socketWriter = new EndianBinaryWriter(new BufferedStream(CreateSocketStream(), sendBufferSize));
					socketReader = new EndianBinaryReader(new BufferedStream(CreateSocketStream(), receiveBufferSize));

					// now lets create the background read thread
					readThread = new Thread(new ThreadStart(ReadLoop)) { IsBackground = true };
					readThread.Start();
				}
			}
		}

		/// <summary>
		/// Property IsStarted
		/// </summary>
		public bool IsStarted
		{
			get
			{
				lock(myLock)
				{
					return started;
				}
			}
		}

		public virtual void Oneway(Command command)
		{
			lock(myLock)
			{
				if(closed.Value)
				{
					this.exceptionHandler(this, new InvalidOperationException("Error writing to broker.  Transport connection is closed."));
					return;
				}

				if(command is ShutdownInfo)
				{
					seenShutdown = true;
				}

				WireFormat.Marshal(command, socketWriter);
			}
		}

		public FutureResponse AsyncRequest(Command command)
		{
			throw new NotImplementedException("Use a ResponseCorrelator if you want to issue AsyncRequest calls");
		}

		public bool TcpNoDelayEnabled
		{
#if !NETCF
			get { return this.socket.NoDelay; }
			set { this.socket.NoDelay = value; }
#else
			get { return false; }
			set { }
#endif
		}

		public Response Request(Command command)
		{
			throw new NotImplementedException("Use a ResponseCorrelator if you want to issue Request calls");
		}

		public Response Request(Command command, TimeSpan timeout)
		{
			throw new NotImplementedException("Use a ResponseCorrelator if you want to issue Request calls");
		}

		public void Stop()
		{
			Close();
		}

		public void Close()
		{
			Thread theReadThread = null;

			lock(myLock)
			{
				if(closed.CompareAndSet(false, true))
				{
					try
					{
						socket.Shutdown(SocketShutdown.Both);
					}
					catch
					{
					}

					try
					{
						if(null != socketWriter)
						{
							socketWriter.Close();
						}
					}
					catch
					{
					}
					finally
					{
						socketWriter = null;
					}

					try
					{
						if(null != socketReader)
						{
							socketReader.Close();
						}
					}
					catch
					{
					}
					finally
					{
						socketReader = null;
					}

					try
					{
						socket.Close();
					}
					catch
					{
					}

					theReadThread = this.readThread;
					this.readThread = null;
					this.started = false;
				}
			}

			// Don't block on closing the read thread within the lock scope.
			if(null != theReadThread)
			{
				try
				{
					if(Thread.CurrentThread != theReadThread && theReadThread.IsAlive)
					{
						if(!theReadThread.Join((int) MAX_THREAD_WAIT.TotalMilliseconds))
						{
							theReadThread.Abort();
						}
					}
				}
				catch
				{
				}
			}
		}

		public void Dispose()
		{
			Dispose(true);
			GC.SuppressFinalize(this);
		}

		protected void Dispose(bool disposing)
		{
			Close();
			disposed = true;
		}

		public bool IsDisposed
		{
			get
			{
				return disposed;
			}
		}

		public void ReadLoop()
		{
			// This is the thread function for the reader thread. This runs continuously
			// performing a blokcing read on the socket and dispatching all commands
			// received.
			//
			// Exception Handling
			// ------------------
			// If an Exception occurs during the reading/marshalling, then the connection
			// is effectively broken because position cannot be re-established to the next
			// message.  This is reported to the app via the exceptionHandler and the socket
			// is closed to prevent further communication attempts.
			//
			// An exception in the command handler may not be fatal to the transport, so
			// these are simply reported to the exceptionHandler.
			//
			while(!closed.Value)
			{
				Command command = null;

				try
				{
					command = (Command) WireFormat.Unmarshal(socketReader);
				}
				catch(Exception ex)
				{
					command = null;
					if(!closed.Value)
					{
						// Close the socket as there's little that can be done with this transport now.
						Close();
						if(!seenShutdown)
						{
							this.exceptionHandler(this, ex);
						}
					}

					break;
				}

				try
				{
					if(command != null)
					{
						this.commandHandler(this, command);
					}
				}
				catch(Exception e)
				{
					this.exceptionHandler(this, e);
				}
			}
		}

		// Implementation methods

		/// <summary>
		/// Timeout in milliseconds to wait for sending synchronous messages or commands.
		/// Set to -1 for infinite timeout.
		/// </summary>
		public int Timeout
		{
			get { return this.timeout; }
			set { this.timeout = value; }
		}

		/// <summary>
		/// Timeout in milliseconds to wait for sending asynchronous messages or commands.
		/// Set to -1 for infinite timeout.
		/// </summary>
		public int AsyncTimeout
		{
			get { return this.asynctimeout; }
			set { this.asynctimeout = value; }
		}

		public CommandHandler Command
		{
			get { return commandHandler; }
			set { this.commandHandler = value; }
		}

		public ExceptionHandler Exception
		{
			get { return exceptionHandler; }
			set { this.exceptionHandler = value; }
		}

		public InterruptedHandler Interrupted
		{
			get { return interruptedHandler; }
			set { this.interruptedHandler = value; }
		}

		public ResumedHandler Resumed
		{
			get { return resumedHandler; }
			set { this.resumedHandler = value; }
		}

		public IWireFormat WireFormat
		{
			get { return wireformat; }
			set { wireformat = value; }
		}

		public bool IsFaultTolerant
		{
			get { return false; }
		}

		public bool IsConnected
		{
			get { return socket.Connected; }
		}

		public Uri RemoteAddress
		{
			get { return connectedUri; }
		}

		public Object Narrow(Type type)
		{
			return this.GetType().Equals(type) ? this : null;
		}

		public bool IsReconnectSupported
		{
			get { return false; }
		}

		public bool IsUpdateURIsSupported
		{
			get { return false; }
		}

		public void UpdateURIs(bool rebalance, Uri[] updatedURIs)
		{
			throw new IOException();
		}
	}
}



