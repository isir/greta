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
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using System.Text;
using System.Net;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.State;
using Apache.NMS.ActiveMQ.Threads;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport.Failover
{
    /// <summary>
    /// A Transport that is made reliable by being able to fail over to another
    /// transport when a transport failure is detected.
    /// </summary>
    public class FailoverTransport : ICompositeTransport, IComparable
    {
		private static int DEFAULT_INITIAL_RECONNECT_DELAY = 10;
		private static int INFINITE = -1;

        private static int idCounter = 0;
        private readonly int id;

        private bool disposed;
        private bool connected;
        private readonly List<Uri> uris = new List<Uri>();
        private readonly List<Uri> updated = new List<Uri>();

        private CommandHandler commandHandler;
        private ExceptionHandler exceptionHandler;
        private InterruptedHandler interruptedHandler;
        private ResumedHandler resumedHandler;

		private readonly CountDownLatch listenerLatch = new CountDownLatch(4);
        private readonly Mutex reconnectMutex = new Mutex();
        private readonly Mutex backupMutex = new Mutex();
        private readonly Mutex sleepMutex = new Mutex();
        private readonly ConnectionStateTracker stateTracker = new ConnectionStateTracker();
        private readonly Dictionary<int, Command> requestMap = new Dictionary<int, Command>();

        private Uri connectedTransportURI;
        private Uri failedConnectTransportURI;
        private readonly AtomicReference<ITransport> connectedTransport = new AtomicReference<ITransport>(null);
        private TaskRunner reconnectTask = null;
        private bool started;
        private bool initialized;
        private int initialReconnectDelay = DEFAULT_INITIAL_RECONNECT_DELAY;
        private int maxReconnectDelay = 1000 * 30;
        private int backOffMultiplier = 2;
        private int timeout = INFINITE;
        private bool useExponentialBackOff = true;
        private bool randomize = true;
        private int maxReconnectAttempts = INFINITE;
        private int startupMaxReconnectAttempts = INFINITE;
        private int connectFailures;
        private int reconnectDelay = DEFAULT_INITIAL_RECONNECT_DELAY;
        private Exception connectionFailure;
        private bool firstConnection = true;
        private bool backup = false;
        private readonly List<BackupTransport> backups = new List<BackupTransport>();
        private int backupPoolSize = 1;
        private bool trackMessages = false;
    	private bool trackTransactionProducers = true;
        private int maxCacheSize = 256;
        private volatile Exception failure;
        private readonly object mutex = new object();
        private bool reconnectSupported = true;
        private bool updateURIsSupported = true;
    	private bool doRebalance = false;
    	private bool connectedToPriority = false;
	 	private bool priorityBackup = false;
    	private List<Uri> priorityList = new List<Uri>();
    	private bool priorityBackupAvailable = false;

		// Not Sure how to work these back in with all the changes.
		//private int asyncTimeout = 45000;
        //private bool asyncConnect = false;

        public FailoverTransport()
        {
            id = idCounter++;

            stateTracker.TrackTransactions = true;
            reconnectTask = DefaultThreadPools.DefaultTaskRunnerFactory.CreateTaskRunner(
                new FailoverTask(this), "ActiveMQ Failover Worker: " + this.GetHashCode().ToString());
        }

        ~FailoverTransport()
        {
            Dispose(false);
        }

        #region FailoverTask

        private class FailoverTask : Task
        {
            private readonly FailoverTransport parent;

            public FailoverTask(FailoverTransport p)
            {
                parent = p;
            }

            public bool Iterate()
            {
                bool result = false;
                if (!parent.IsStarted)
                {
                    return false;
                }

                bool buildBackup = true;
                lock (parent.backupMutex) 
				{
                    if ((parent.connectedTransport.Value == null || parent.doRebalance || parent.priorityBackupAvailable) && !parent.disposed)
					{
                        result = parent.DoConnect();
                        buildBackup = false;
                    }
                }
                if (buildBackup) 
				{
                    parent.BuildBackups();
                    if (parent.priorityBackup && !parent.connectedToPriority) 
					{
                        try 
						{
                            parent.DoDelay();
                            if (parent.reconnectTask == null)
							{
                                return true;
                            }
                            parent.reconnectTask.Wakeup();
                        } 
						catch (ThreadInterruptedException) 
						{
                        	Tracer.Debug("Reconnect task has been interrupted.");
                        }
                    }
                }
				else 
				{
                    try 
					{
                        if (parent.reconnectTask == null) 
						{
                            return true;
                        }
                        parent.reconnectTask.Wakeup();
                    }
					catch (ThreadInterruptedException) 
					{
                        Tracer.Debug("Reconnect task has been interrupted.");
                    }
                }
                return result;
            }
        }

        #endregion

        #region Property Accessors

        public CommandHandler Command
        {
            get { return commandHandler; }
            set 
			{ 
				commandHandler = value; 
				listenerLatch.countDown();
			}
        }

        public ExceptionHandler Exception
        {
            get { return exceptionHandler; }
            set 
			{ 
				exceptionHandler = value; 
				listenerLatch.countDown();
			}
        }

        public InterruptedHandler Interrupted
        {
            get { return interruptedHandler; }
            set 
			{ 
				this.interruptedHandler = value; 
				this.listenerLatch.countDown();
			}
        }

        public ResumedHandler Resumed
        {
            get { return resumedHandler; }
            set 
			{ 
				this.resumedHandler = value; 
				this.listenerLatch.countDown();
			}
        }

        internal Exception Failure
        {
            get { return failure; }
            set
            {
                lock(mutex)
                {
                    failure = value;
                }
            }
        }

        public int Timeout
        {
            get { return this.timeout; }
            set { this.timeout = value; }
        }

        public int InitialReconnectDelay
        {
            get { return initialReconnectDelay; }
            set { initialReconnectDelay = value; }
        }

        public int MaxReconnectDelay
        {
            get { return maxReconnectDelay; }
            set { maxReconnectDelay = value; }
        }

        public int ReconnectDelay
        {
            get { return reconnectDelay; }
            set { reconnectDelay = value; }
        }

        public int ReconnectDelayExponent
        {
            get { return backOffMultiplier; }
            set { backOffMultiplier = value; }
        }

        public ITransport ConnectedTransport
        {
            get { return connectedTransport.Value; }
            set { connectedTransport.Value = value; }
        }

        public Uri ConnectedTransportURI
        {
            get { return connectedTransportURI; }
            set { connectedTransportURI = value; }
        }

        public int MaxReconnectAttempts
        {
            get { return maxReconnectAttempts; }
            set { maxReconnectAttempts = value; }
        }

        public int StartupMaxReconnectAttempts
        {
            get { return startupMaxReconnectAttempts; }
            set { startupMaxReconnectAttempts = value; }
        }

        public bool Randomize
        {
            get { return randomize; }
            set { randomize = value; }
        }

        public bool Backup
        {
            get { return backup; }
            set { backup = value; }
        }

		public bool PriorityBackup
		{
			get { return priorityBackup; }
			set { this.priorityBackup = value; }
		}

	    public String PriorityURIs
		{
			get { return PrintableUriList(priorityList); }
			set { this.ProcessDelimitedUriList(value, priorityList); }
	    }

        public int BackupPoolSize
        {
            get { return backupPoolSize; }
            set { backupPoolSize = value; }
        }

        public bool TrackMessages
        {
            get { return trackMessages; }
            set { trackMessages = value; }
        }

		public bool TrackTransactionProducers
		{
			get { return trackTransactionProducers; }
			set { this.trackTransactionProducers = value; }
		}

        public int MaxCacheSize
        {
            get { return maxCacheSize; }
            set { maxCacheSize = value; }
        }

        public bool UseExponentialBackOff
        {
            get { return useExponentialBackOff; }
            set { useExponentialBackOff = value; }
        }

        public IWireFormat WireFormat
        {
            get
            {
                ITransport transport = ConnectedTransport;
                if(transport != null)
                {
                    return transport.WireFormat;
                }

                return null;
            }
        }

        /// <summary>
        /// Gets or sets a value indicating whether to asynchronously connect to sockets
        /// </summary>
        /// <value><c>true</c> if [async connect]; otherwise, <c>false</c>.</value>
        public bool AsyncConnect
        {
            set { }
        }

        /// <summary>
        /// If doing an asynchronous connect, the milliseconds before timing out if no connection can be made
        /// </summary>
        /// <value>The async timeout.</value>
        public int AsyncTimeout
        {
            get { return 0; }
            set { }
        }

        public ConnectionStateTracker StateTracker
        {
            get { return this.stateTracker; }
        }

        #endregion

        public bool IsFaultTolerant
        {
            get { return true; }
        }

        public bool IsDisposed
        {
            get { return disposed; }
        }

        public bool IsConnected
        {
            get { return connected; }
        }

        public bool IsConnectedToPriority
        {
            get { return connectedToPriority; }
        }

        public bool IsStarted
        {
            get { return started; }
        }

        public bool IsReconnectSupported
        {
            get { return this.reconnectSupported; }
        }

        public bool IsUpdateURIsSupported
        {
            get { return this.updateURIsSupported; }
        }

        public void OnException(ITransport sender, Exception error)
        {
            try
            {
                HandleTransportFailure(error);
            }
            catch(Exception)
            {
				this.Exception(this, new IOException("Unexpected Transport Failure."));
            }
        }

        public void DisposedOnCommand(ITransport sender, Command c)
        {
        }

        public void DisposedOnException(ITransport sender, Exception e)
        {
        }

        public void HandleTransportFailure(Exception e)
        {
            ITransport transport = connectedTransport.GetAndSet(null);
	        if (transport == null) 
			{
	            // sync with possible in progress reconnect
	            lock(reconnectMutex) 
				{
	                transport = connectedTransport.GetAndSet(null);
	            }
	        }

			if(transport != null)
            {
				DisposeTransport(transport);

	            bool reconnectOk = false;
	            lock(reconnectMutex) 
				{
	                if (CanReconnect()) 
					{
                    	Tracer.WarnFormat("Transport failed to {0}, attempting to automatically reconnect due to: {1}", 
						                  ConnectedTransportURI, e.Message);
	                    reconnectOk = true;
	                }

                    initialized = false;
                    failedConnectTransportURI = ConnectedTransportURI;
                    ConnectedTransportURI = null;
					connectedToPriority = false;
                    connected = false;

	                if (reconnectOk) 
					{
	                    if(this.Interrupted != null)
	                    {
	                        this.Interrupted(transport);
	                    }

	                    updated.Remove(failedConnectTransportURI);
	                    reconnectTask.Wakeup();
	                }
					else if (!disposed) 
					{
	                    PropagateFailureToExceptionListener(e);
	                }
	            }
            }
        }

	    private bool CanReconnect() 
		{
	        return started && 0 != CalculateReconnectAttemptLimit();
	    }

        public void Start()
        {
            lock(reconnectMutex)
            {
                if(started)
                {
                    Tracer.Debug("FailoverTransport Already Started.");
                    return;
                }

                Tracer.Debug("FailoverTransport Started.");
                started = true;
                stateTracker.MaxCacheSize = MaxCacheSize;
                stateTracker.TrackMessages = TrackMessages;
				stateTracker.TrackTransactionProducers = TrackTransactionProducers;
                if(ConnectedTransport != null)
                {
                    Tracer.Debug("FailoverTransport already connected, start is restoring.");
                    stateTracker.DoRestore(ConnectedTransport);
                }
                else
                {
                    Tracer.Debug("FailoverTransport not connected, start is reconnecting.");
                    Reconnect(false);
                }
            }
        }

        public virtual void Stop()
        {
            ITransport transportToStop = null;
	        List<ITransport> backupsToStop = new List<ITransport>(backups.Count);

			try 
			{
	            lock(reconnectMutex)
	            {
	                if(!started)
	                {
	                    Tracer.Debug("FailoverTransport Already Stopped.");
	                    return;
	                }

	                Tracer.Debug("FailoverTransport Stopped.");
	                started = false;
	                disposed = true;
	                connected = false;
	                if(ConnectedTransport != null)
	                {
	                    transportToStop = connectedTransport.GetAndSet(null);
	                }

	            }
				lock(sleepMutex)
				{
					Monitor.PulseAll(sleepMutex);
				}
			}
			finally
			{
            	if(reconnectTask != null)
            	{
	                reconnectTask.Shutdown();
            	}
			}

	        lock(backupMutex) 
			{
	            foreach (BackupTransport backup in backups) 
				{
	                backup.Disposed = true;
	                ITransport transport = backup.Transport;
	                if (transport != null) 
					{
	                    transport.Command = DisposedOnCommand;
						transport.Exception = DisposedOnException;
	                    backupsToStop.Add(transport);
	                }
	            }
	            backups.Clear();
	        }
	        
			foreach (ITransport transport in backupsToStop) 
			{
	            try 
				{
	                if (Tracer.IsDebugEnabled) 
					{
	                    Tracer.Debug("Stopped backup: " + transport);
	                }
	                DisposeTransport(transport);
	            } 
				catch (Exception) 
				{
	            }
	        }

			if(transportToStop != null)
            {
                transportToStop.Stop();
            }
        }

        public FutureResponse AsyncRequest(Command command)
        {
            throw new ApplicationException("FailoverTransport does not implement AsyncRequest(Command)");
        }

        public Response Request(Command command)
        {
            throw new ApplicationException("FailoverTransport does not implement Request(Command)");
        }

        public Response Request(Command command, TimeSpan ts)
        {
            throw new ApplicationException("FailoverTransport does not implement Request(Command, TimeSpan)");
        }

        public void OnCommand(ITransport sender, Command command)
        {
            if(command != null)
            {
                if(command.IsResponse)
                {
                    Command request = null;
                    lock(((ICollection) requestMap).SyncRoot)
                    {
                        int v = ((Response) command).CorrelationId;
                        try
                        {
                            if(requestMap.TryGetValue(v, out request))
                            {
                                requestMap.Remove(v);
                            }
                        }
                        catch
                        {
                        }
                    }

                    Tracked tracked = request as Tracked;
                    if(tracked != null)
                    {
                        tracked.OnResponse();
                    }
                }

                if(!initialized)
                {
                    initialized = true;
                }

                if(command.IsConnectionControl)
                {
                    this.HandleConnectionControl(command as ConnectionControl);
                }
            }

            this.Command(sender, command);
        }

        public void Oneway(Command command)
        {
            Exception error = null;

            lock(reconnectMutex)
            {
                if(command != null && ConnectedTransport == null)
                {
                    if(command.IsShutdownInfo)
                    {
                        // Skipping send of ShutdownInfo command when not connected.
                        return;
                    }
                    else if(command.IsRemoveInfo || command.IsMessageAck)
                    {
                        stateTracker.Track(command);
                        // Simulate response to RemoveInfo command or a MessageAck
                        // since it would be stale at this point.
                        if(command.ResponseRequired)
                        {
                            OnCommand(this, new Response() { CorrelationId = command.CommandId });
                        }
                        return;
                    }
					else if(command.IsMessagePull) 
					{
                        // Simulate response to MessagePull if timed as we can't honor that now.
                        MessagePull pullRequest = command as MessagePull;
                        if (pullRequest.Timeout != 0) 
						{
                            MessageDispatch dispatch = new MessageDispatch();
                            dispatch.ConsumerId = pullRequest.ConsumerId;
                            dispatch.Destination = pullRequest.Destination;
                            OnCommand(this, dispatch);
                        }
                        return;
                    }
                }

                // Keep trying until the message is sent.
                for(int i = 0; !disposed; i++)
                {
                    try
                    {
                        // Any Ack that was being sent when the connection dropped is now
                        // stale so we don't send it here as it would cause an unmatched ack
                        // on the broker side and probably prevent a consumer from getting
                        // any new messages.
                        if(command.IsMessageAck && i > 0)
                        {
                            Tracer.Debug("Inflight MessageAck being dropped as stale.");
                            if(command.ResponseRequired)
                            {
                                OnCommand(this, new Response() { CorrelationId = command.CommandId });
                            }
                            return;
                        }

                        // Wait for transport to be connected.
                        ITransport transport = ConnectedTransport;
                        DateTime start = DateTime.Now;
                        bool timedout = false;
                        TimeSpan timewait = TimeSpan.FromMilliseconds(-1);

                        while(transport == null && !disposed && connectionFailure == null)
                        {
                            Tracer.Debug("Waiting for transport to reconnect.");

                            int elapsed = (int) (DateTime.Now - start).TotalMilliseconds;
                            if(this.timeout > 0 && elapsed > this.timeout)
                            {
                                timedout = true;
                                Tracer.DebugFormat("FailoverTransport.oneway - timed out after {0} mills", elapsed);
                                break;
                            }

                            if(this.timeout > 0)
                            {
                                // Set the timeout for waiting to be at most 100ms past the maximum timeout length.
                                int remainingTime = (this.timeout - elapsed) + 100;
                                timewait = TimeSpan.FromMilliseconds(remainingTime);
                            }

                            // Release so that the reconnect task can run
                            try
                            {
                                // Wait for something.  The mutex will be pulsed if we connect, or are shut down.
                                Monitor.Wait(reconnectMutex, timewait);
                            }
                            catch(ThreadInterruptedException e)
                            {
                                Tracer.DebugFormat("Interrupted: {0}", e.Message);
                            }

                            transport = ConnectedTransport;
                        }

                        if(transport == null)
                        {
                            // Previous loop may have exited due to use being disposed.
                            if(disposed)
                            {
                                error = new IOException("Transport disposed.");
                            }
                            else if(connectionFailure != null)
                            {
                                error = connectionFailure;
                            }
                            else if(timedout)
                            {
                                error = new IOException("Failover oneway timed out after " + timeout + " milliseconds.");
                            }
                            else
                            {
                                error = new IOException("Unexpected failure.");
                            }
                            break;
                        }

                        // If it was a request and it was not being tracked by
                        // the state tracker, then hold it in the requestMap so
                        // that we can replay it later.
                        Tracked tracked = stateTracker.Track(command);
                        lock(((ICollection) requestMap).SyncRoot)
                        {
                            if(tracked != null && tracked.WaitingForResponse)
                            {
                                requestMap.Add(command.CommandId, tracked);
                            }
                            else if(tracked == null && command.ResponseRequired)
                            {
                                requestMap.Add(command.CommandId, command);
                            }
                        }

                        // Send the message.
                        try
                        {
                            transport.Oneway(command);
                            stateTracker.TrackBack(command);
                        }
                        catch(Exception e)
                        {
                            // If the command was not tracked.. we will retry in this method
                            // otherwise we need to trigger a reconnect before returning as
                            // the transport is failed.
                            if (tracked == null)
                            {
                                // since we will retry in this method.. take it
                                // out of the request map so that it is not
                                // sent 2 times on recovery
                                if(command.ResponseRequired)
                                {
                                    lock(((ICollection) requestMap).SyncRoot)
                                    {
                                        requestMap.Remove(command.CommandId);
                                    }
                                }

                                // Rethrow the exception so it will handled by
                                // the outer catch
                                throw;
                            }
                            else
                            {
								if (Tracer.IsDebugEnabled)
								{
                                	Tracer.DebugFormat("Send Oneway attempt: {0} failed: Message = {1}", 
									                   i, e.Message);
                                	Tracer.DebugFormat("Failed Message Was: {0}", command);
								}
                                HandleTransportFailure(e);
                            }
                        }

                        return;
                    }
                    catch(Exception e)
                    {
						if (Tracer.IsDebugEnabled)
						{
                        	Tracer.DebugFormat("Send Oneway attempt: {0} failed: Message = {1}", 
							                   i, e.Message);
                        	Tracer.DebugFormat("Failed Message Was: {0}", command);
						}
                        HandleTransportFailure(e);
                    }
                }
            }

            if(!disposed)
            {
                if(error != null)
                {
                    throw error;
                }
            }
        }

        public void Add(bool rebalance, Uri[] urisToAdd)
        {
			bool newUri = false;
            lock(uris)
            {
                foreach (Uri uri in urisToAdd)
                {
                    if(!Contains(uri))
                    {
                        uris.Add(uri);
						newUri = true;
                    }
                }
            }

			if (newUri)
			{
            	Reconnect(rebalance);
			}
        }

        public void Add(bool rebalance, String u)
        {
            try
            {
                Add(rebalance, new Uri[] { new Uri(u) });
            }
            catch(Exception e)
            {
                Tracer.ErrorFormat("Failed to parse URI '{0}': {1}", u, e.Message);
            }
        }

        public void Remove(bool rebalance, Uri[] u)
        {
            lock(uris)
            {
                for(int i = 0; i < u.Length; i++)
                {
                    uris.Remove(u[i]);
                }
            }

            Reconnect(rebalance);
        }

        public void Remove(bool rebalance, String u)
        {
            try
            {
                Remove(rebalance, new Uri[] { new Uri(u) });
            }
            catch(Exception e)
            {
                Tracer.ErrorFormat("Failed to parse URI '{0}': {1}", u, e.Message);
            }
        }

        public void Reconnect(Uri uri)
        {
            Add(true, new Uri[] { uri });
        }

	    public void Reconnect(bool rebalance)
		{
			lock(reconnectMutex) 
			{
	            if(started) 
				{
	                if (rebalance) 
					{
	                    doRebalance = true;
	                }
                    Tracer.Debug("Waking up reconnect task");
	                try 
					{
	                    reconnectTask.Wakeup();
	                } 
					catch (ThreadInterruptedException) 
					{
	                }
	            } 
				else 
				{
                    Tracer.Debug("Reconnect was triggered but transport is not started yet. Wait for start to connect the transport.");
	            }
	        }
	    }

        private List<Uri> ConnectList
        {
            get
            {
				if (updated.Count != 0)
				{
					return updated;
				}

                List<Uri> l = new List<Uri>(uris);
                bool removed = false;
                if(failedConnectTransportURI != null)
                {
                    removed = l.Remove(failedConnectTransportURI);
                }

                if(Randomize)
                {
					Shuffle(l);
                }

                if(removed)
                {
                    l.Add(failedConnectTransportURI);
                }

		        if (Tracer.IsDebugEnabled)
				{
					Tracer.DebugFormat("Uri connection list: {0} from: {1}", 
					                   PrintableUriList(l), PrintableUriList(uris));
		        }

                return l;
            }
        }

        protected void RestoreTransport(ITransport t)
        {
            Tracer.Info("Restoring previous transport connection.");
            t.Start();

            // Send information to the broker - informing it we are a fault tolerant client
            t.Oneway(new ConnectionControl() { FaultTolerant = true });
            stateTracker.DoRestore(t);

            Tracer.Info("Sending queued commands...");
            Dictionary<int, Command> tmpMap = null;
            lock(((ICollection) requestMap).SyncRoot)
            {
                tmpMap = new Dictionary<int, Command>(requestMap);
            }

            foreach(Command command in tmpMap.Values)
            {
                if(command.IsMessageAck)
                {
                    Tracer.Debug("Stored MessageAck being dropped as stale.");
                    OnCommand(this, new Response() { CorrelationId = command.CommandId });
                    continue;
                }

                t.Oneway(command);
            }
        }

        public Uri RemoteAddress
        {
            get
            {
                if(ConnectedTransport != null)
                {
                    return ConnectedTransport.RemoteAddress;
                }
                return null;
            }
        }

        public Object Narrow(Type type)
        {
            if(this.GetType().Equals(type))
            {
                return this;
            }
            else if(ConnectedTransport != null)
            {
                return ConnectedTransport.Narrow(type);
            }

            return null;
        }

        private bool DoConnect()
        {
            lock(reconnectMutex)
            {
				if (disposed || connectionFailure != null)
				{
					Monitor.PulseAll(reconnectMutex);
				}

            	if ((connectedTransport.Value != null && !doRebalance && !priorityBackupAvailable) || disposed || connectionFailure != null)
				{
                    return false;
                }
                else
                {
                    List<Uri> connectList = ConnectList;
                    if(connectList.Count == 0)
                    {
                        Failure = new NMSConnectionException("No URIs available for connection.");
                    }
                    else
                    {
	                    if (doRebalance)
						{
	                        if (connectedToPriority || CompareUris(connectList[0], connectedTransportURI))
							{
	                            // already connected to first in the list, no need to rebalance
	                            doRebalance = false;
	                            return false;
	                        } 
							else
							{
	                            if (Tracer.IsDebugEnabled)
								{
									Tracer.DebugFormat("Doing rebalance from: {0} to {1}", 
									                   connectedTransportURI, PrintableUriList(connectList));
	                            }
	                            try 
								{
	                                ITransport current = this.connectedTransport.GetAndSet(null);
	                                if (current != null) 
									{
	                                    DisposeTransport(current);
	                                }
	                            } 
								catch (Exception e) 
								{
	                            	if (Tracer.IsDebugEnabled)
									{
										Tracer.DebugFormat("Caught an exception stopping existing " + 
										                   "transport for rebalance {0}", e.Message);
	                                }
	                            }
	                        }

	                        doRebalance = false;
	                    }

	                    ResetReconnectDelay();

	                    ITransport transport = null;
	                    Uri uri = null;

	                    // If we have a backup already waiting lets try it.
	                    lock(backupMutex) 
						{
	                        if ((priorityBackup || backup) && backups.Count > 0)
							{
                            	List<BackupTransport> l = new List<BackupTransport>(backups);
	                            if (randomize) 
								{
									Shuffle(l);
	                            }
								BackupTransport bt = l[0];
								l.RemoveAt(0);
	                            backups.Remove(bt);
	                            transport = bt.Transport;
	                            uri = bt.Uri;
	                            if (priorityBackup && priorityBackupAvailable) 
								{
	                                ITransport old = this.connectedTransport.GetAndSet(null);
	                                if (old != null) 
									{
	                                    DisposeTransport(old);
	                                }
	                                priorityBackupAvailable = false;
	                            }
	                        }
	                    }

	                    // Sleep for the reconnectDelay if there's no backup and we aren't trying
	                    // for the first time, or we were disposed for some reason.
	                    if (transport == null && !firstConnection && (reconnectDelay > 0) && !disposed) 
						{
	                        lock(sleepMutex) 
							{
	                            if (Tracer.IsDebugEnabled)
								{
									Tracer.DebugFormat("Waiting {0} ms before attempting connection.", reconnectDelay);
	                            }
	                            try 
								{
	                                Monitor.Wait(sleepMutex, reconnectDelay);
	                            }
								catch (ThreadInterruptedException)
								{
	                            }
	                        }
	                    }

						IEnumerator<Uri> iter = connectList.GetEnumerator();
	                    while ((transport != null || iter.MoveNext()) && (connectedTransport.Value == null && !disposed))
						{
	                        try 
							{
	                            if (Tracer.IsDebugEnabled)
								{
									Tracer.DebugFormat("Attempting {0}th connect to: {1}",
									                   connectFailures, uri);
	                            }

								// We could be starting with a backup and if so we wait to grab a
	                            // URI from the pool until next time around.
	                            if (transport == null) 
								{
	                                uri = iter.Current;
	                                transport = TransportFactory.CompositeConnect(uri);
	                            }

                                transport.Command = OnCommand;
                                transport.Exception = OnException;
	                            transport.Start();

	                            if (started && !firstConnection) 
								{
	                                RestoreTransport(transport);
	                            }

	                            if (Tracer.IsDebugEnabled)
								{
	                                Tracer.Debug("Connection established");
	                            }
	                            reconnectDelay = initialReconnectDelay;
	                            connectedTransportURI = uri;
	                            connectedTransport.Value = transport;
								connectedToPriority = IsPriority(connectedTransportURI);
	                            Monitor.PulseAll(reconnectMutex);
	                            connectFailures = 0;

								// Try to wait long enough for client to init the event callbacks.
								listenerLatch.await(TimeSpan.FromSeconds(2));

	                            if (Resumed != null) 
								{
	                                Resumed(transport);
	                            }
								else 
								{
	                                if (Tracer.IsDebugEnabled) 
									{
	                                    Tracer.Debug("transport resumed by transport listener not set");
	                                }
	                            }

	                            if (firstConnection) 
								{
	                                firstConnection = false;
	                                Tracer.Info("Successfully connected to " + uri);
	                            }
								else 
								{
	                                Tracer.Info("Successfully reconnected to " + uri);
	                            }

	                            connected = true;
	                            return false;
	                        }
							catch (Exception e) 
							{
	                            failure = e;
                                if (Tracer.IsDebugEnabled) 
								{
	                                Tracer.Debug("Connect fail to: " + uri + ", reason: " + e.Message);
	                            }
	                            if (transport != null) 
								{
	                                try 
									{
	                                    transport.Stop();
	                                    transport = null;
	                                }
									catch (Exception ee) 
									{
	                                	if (Tracer.IsDebugEnabled) 
										{
	                                        Tracer.Debug("Stop of failed transport: " + transport +
	                                                     " failed with reason: " + ee.Message);
	                                    }
	                                }
	                            }
	                        }
	                    }
					}
				}
            
	            int reconnectLimit = CalculateReconnectAttemptLimit();

	            connectFailures++;
	            if (reconnectLimit != INFINITE && connectFailures >= reconnectLimit) 
				{
					Tracer.ErrorFormat("Failed to connect to {0} after: {1} attempt(s)", 
					                   PrintableUriList(uris), connectFailures);
	                connectionFailure = failure;

	                // Make sure on initial startup, that the transportListener has been
	                // initialized for this instance.
					listenerLatch.await(TimeSpan.FromSeconds(2));
	                PropagateFailureToExceptionListener(connectionFailure);
	                return false;
	            }
	        }

	        if(!disposed)
			{
	            DoDelay();
	        }

	        return !disposed;
        }

        private bool BuildBackups()
        {
            lock(backupMutex)
            {
            	if (!disposed && (backup || priorityBackup) && backups.Count < backupPoolSize) 
				{
	                List<Uri> backupList = new List<Uri>(priorityList);
                    List<Uri> connectList = ConnectList;
	                foreach(Uri uri in connectList) 
					{
	                    if (!backupList.Contains(uri)) 
						{
	                        backupList.Add(uri);
	                    }
	                }
                    foreach(BackupTransport bt in backups)
                    {
                        if(bt.Disposed)
                        {
                            backups.Remove(bt);
                        }
                    }

                    foreach(Uri uri in connectList)
                    {
						if (disposed)
						{
							break;
						}

                        if(ConnectedTransportURI != null && !ConnectedTransportURI.Equals(uri))
                        {
                            try
                            {
                                BackupTransport bt = new BackupTransport(this)
                                {
                                    Uri = uri
                                };

                                if(!backups.Contains(bt))
                                {
                                    ITransport t = TransportFactory.CompositeConnect(uri);
                                    t.Command = bt.OnCommand;
                                    t.Exception = bt.OnException;
                                    t.Start();
                                    bt.Transport = t;
	                                if (priorityBackup && IsPriority(uri))
									{
	                                   priorityBackupAvailable = true;
	                                   backups.Insert(0, bt);
	                                }
									else 
									{
	                                    backups.Add(bt);
	                                }
                                }
                            }
                            catch(Exception e)
                            {
                                Tracer.DebugFormat("Failed to build backup: {0}", e.Message);
                            }
                        }

                        if(backups.Count == BackupPoolSize)
                        {
                            break;
                        }
                    }
                }
            }

            return false;
        }

        public void ConnectionInterruptProcessingComplete(ConnectionId connectionId)
        {
            lock(reconnectMutex)
            {
                Tracer.Debug("Connection Interrupt Processing is complete for ConnectionId: " + connectionId);
                stateTracker.ConnectionInterruptProcessingComplete(this, connectionId);
            }
        }

        public void UpdateURIs(bool rebalance, Uri[] updatedURIs)
        {
            if(IsUpdateURIsSupported)
            {
                Dictionary<Uri, bool> copy = new Dictionary<Uri, bool>();
                foreach(Uri uri in updated)
                {
                    if(uri != null)
                    {
                        copy[uri] = true;
                    }
                }
	
				updated.Clear();

                if(updatedURIs != null && updatedURIs.Length > 0)
                {
                    Dictionary<Uri, bool> uriSet = new Dictionary<Uri, bool>();
                    for(int i = 0; i < updatedURIs.Length; i++)
                    {
                        Uri uri = updatedURIs[i];
                        if(uri != null)
                        {
                            uriSet[uri] = true;
                        }
                    }

                    foreach(Uri uri in uriSet.Keys)
                    {
                        if(!updated.Contains(uri))
                        {
							updated.Add(uri);
                        }
                    }

					if (Tracer.IsDebugEnabled)
					{
						Tracer.DebugFormat("Updated URIs list {0}", PrintableUriList(updated));
					}

	                if (!(copy.Count == 0 && updated.Count == 0) && !copy.Keys.Equals(updated))
					{
	                    BuildBackups();
	                    lock(reconnectMutex) 
						{
	                        Reconnect(rebalance);
	                    }
	                }
                }
            }
        }

        public void HandleConnectionControl(ConnectionControl control)
        {
            string reconnectStr = control.ReconnectTo;

            if(reconnectStr != null)
            {
                reconnectStr = reconnectStr.Trim();
                if(reconnectStr.Length > 0)
                {
                    try
                    {
                        Uri uri = new Uri(reconnectStr);
                        if(IsReconnectSupported)
                        {
                            Tracer.Info("Reconnecting to: " + uri.OriginalString);
                            Reconnect(uri);
                        }
                    }
                    catch(Exception e)
                    {
                        Tracer.ErrorFormat("Failed to handle ConnectionControl reconnect to {0}: {1}", reconnectStr, e);
                    }
                }
            }

            ProcessNewTransports(control.RebalanceConnection, control.ConnectedBrokers);
        }

        private void ProcessNewTransports(bool rebalance, String newTransports)
        {
            if(newTransports != null)
            {
                newTransports = newTransports.Trim();

                if(newTransports.Length > 0 && IsUpdateURIsSupported)
                {
                    List<Uri> list = new List<Uri>();
					ProcessDelimitedUriList(newTransports, list);

                    if(list.Count != 0)
                    {
                        try
                        {
                            UpdateURIs(rebalance, list.ToArray());
                        }
                        catch
                        {
                            Tracer.Error("Failed to update transport URI's from: " + newTransports);
                        }
                    }
                }
            }        
		}

		private void ProcessDelimitedUriList(String priorityUris, List<Uri> target)
		{
            String[] tokens = priorityUris.Split(new Char[] { ',' });

            foreach(String str in tokens)
            {
                try
                {
                    Uri uri = new Uri(str);
                    target.Add(uri);

					if (Tracer.IsDebugEnabled)
					{
						Tracer.DebugFormat("Adding new Uri[{0}] to list,", uri);
					}
                }
                catch (Exception e)
                {
					Tracer.ErrorFormat("Failed to parse broker address: {0} because of: {1}",
					                   str, e.Message);
                }
            }
		}

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        public void Dispose(bool disposing)
        {
            this.Stop();
            disposed = true;
        }

        public int CompareTo(Object o)
        {
            if(o is FailoverTransport)
            {
                FailoverTransport oo = o as FailoverTransport;

                return this.id - oo.id;
            }
            else
            {
                throw new ArgumentException();
            }
        }

        public override String ToString()
        {
            return ConnectedTransportURI == null ? "unconnected" : ConnectedTransportURI.ToString();
        }

	    internal bool IsPriority(Uri uri) 
		{
			if (priorityBackup)
			{
		        if (priorityList.Count > 0) 
				{
		            return priorityList.Contains(uri);
		        }

				if (this.uris.Count > 0) 
				{
		        	return uris[0].Equals(uri);
				}
			}
			return false;
	    }

		public void DisposeTransport(ITransport transport) 
		{
			transport.Command = DisposedOnCommand;
			transport.Exception = DisposedOnException;

			try 
			{
	            transport.Stop();
        	}
			catch (Exception e) 
			{
				Tracer.DebugFormat("Could not stop transport: {0]. Reason: {1}", transport, e.Message);
        	}
    	}

	    private void ResetReconnectDelay() 
		{
	        if (!useExponentialBackOff || reconnectDelay == DEFAULT_INITIAL_RECONNECT_DELAY) 
			{
	            reconnectDelay = initialReconnectDelay;
	        }
	    }

	    private void DoDelay()
		{
	        if (reconnectDelay > 0) 
			{
	            lock(sleepMutex) 
				{
	                if (Tracer.IsDebugEnabled) 
					{
						Tracer.DebugFormat("Waiting {0} ms before attempting connection", reconnectDelay);
	                }
	                try 
					{
						Monitor.Wait(sleepMutex, reconnectDelay);
	                } 
					catch (ThreadInterruptedException) 
					{
	                }
	            }
	        }

	        if (useExponentialBackOff) 
			{
	            // Exponential increment of reconnect delay.
	            reconnectDelay *= backOffMultiplier;
	            if (reconnectDelay > maxReconnectDelay) 
				{
	                reconnectDelay = maxReconnectDelay;
	            }
	        }
	    }

	    private void PropagateFailureToExceptionListener(Exception exception) 
		{
	        if (Exception != null) 
			{
                Exception(this, exception);
	        }
			else
			{
				Exception(this, new IOException());
			}
			Monitor.PulseAll(reconnectMutex);
	    }

	    private int CalculateReconnectAttemptLimit() 
		{
	        int maxReconnectValue = this.maxReconnectAttempts;
	        if (firstConnection && this.startupMaxReconnectAttempts != INFINITE) 
			{
	            maxReconnectValue = this.startupMaxReconnectAttempts;
	        }
	        return maxReconnectValue;
	    }

		public void Shuffle<T>(List<T> list)  
		{  
            Random random = new Random(DateTime.Now.Millisecond);
		    int index = list.Count;  
		    while (index > 1) 
			{  
		        index--;  
		        int k = random.Next(index + 1);  
		        T value = list[k];  
		        list[k] = list[index];  
		        list[index] = value;  
		    }  
		}

		private String PrintableUriList(List<Uri> uriList)
		{
			if (uriList.Count == 0)
			{
				return "";
			}

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < uriList.Count; ++i)
			{
				builder.Append(uriList[i]);
				if (i < (uriList.Count - 1))
				{
					builder.Append(",");
				}
			}

			return builder.ToString();
		}

		private bool CompareUris(Uri first, Uri second) 
		{
			bool result = false;
            if (first.Port == second.Port)
			{
                IPHostEntry firstAddr = null;
                IPHostEntry secondAddr = null;
                try 
				{
            		firstAddr = Dns.GetHostEntry(first.Host);
            		secondAddr = Dns.GetHostEntry(second.Host);

	                if (firstAddr.Equals(secondAddr)) 
					{
						result = true;
	                }
				} 
				catch(Exception e)
				{
                    if (firstAddr == null) 
					{
						Tracer.WarnFormat("Failed to Lookup IPHostEntry for URI[{0}] : {1}", first, e);
                    } 
					else 
					{
						Tracer.WarnFormat("Failed to Lookup IPHostEntry for URI[{0}] : {1}", second, e);
                    }

					if(String.Equals(first.Host, second.Host, StringComparison.CurrentCultureIgnoreCase))
					{
						result = true;
                    }
                }

            }

			return result;
		}

	    private bool Contains(Uri newURI) 
		{
	        bool result = false;
	        foreach (Uri uri in uris) 
			{
	            if (CompareUris(newURI, uri))
				{
					result = true;
					break;
	            }
	        }

	        return result;
	    }
    }
}
