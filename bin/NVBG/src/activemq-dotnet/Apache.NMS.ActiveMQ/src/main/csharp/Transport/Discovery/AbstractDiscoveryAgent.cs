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
using System.Threading;
using System.Collections.Generic;
using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Threads;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Discovery
{
    public abstract class AbstractDiscoveryAgent : IDiscoveryAgent, IDisposable
    {
        public const int DEFAULT_INITIAL_RECONNECT_DELAY = 1000 * 5;
        public const int DEFAULT_BACKOFF_MULTIPLIER = 2;
        public const int DEFAULT_MAX_RECONNECT_DELAY = 1000 * 30;

        private const int WORKER_KILL_TIME_SECONDS = 1000;
        private const int HEARTBEAT_MISS_BEFORE_DEATH = 10;

        private long initialReconnectDelay = DEFAULT_INITIAL_RECONNECT_DELAY;
        private long maxReconnectDelay = DEFAULT_MAX_RECONNECT_DELAY;
        private long backOffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;
        private bool useExponentialBackOff;
        private int maxReconnectAttempts;

        protected readonly Atomic<bool> started = new Atomic<bool>(false);
        protected Thread worker;
        protected readonly ThreadPoolExecutor executor = new ThreadPoolExecutor();

        protected Dictionary<String, DiscoveredServiceData> discoveredServices = 
            new Dictionary<String, DiscoveredServiceData>();
        protected readonly object discoveredServicesLock = new object();

        private Uri discoveryUri;
        private String selfService;
        private String group;
        private ServiceAddHandler serviceAddHandler;
        private ServiceRemoveHandler serviceRemoveHandler;
        private DateTime lastAdvertizeTime;
        private bool reportAdvertizeFailed = true;

        #region Property Getters and Setters

        internal string SelfService
        {
            get { return this.selfService; }
        }

        internal DateTime LastAdvertizeTime
        {
            get { return this.lastAdvertizeTime; }
            set { this.lastAdvertizeTime = value; }
        }

        public long BackOffMultiplier
        {
            get { return this.backOffMultiplier; }
            set { this.backOffMultiplier = value; }
        }

        public long InitialReconnectDelay
        {
            get { return this.initialReconnectDelay; }
            set { this.initialReconnectDelay = value; }
        }

        public int MaxReconnectAttempts
        {
            get { return this.maxReconnectAttempts; }
            set { this.maxReconnectAttempts = value; }
        }

        public long MaxReconnectDelay
        {
            get { return this.maxReconnectDelay; }
            set { this.maxReconnectDelay = value; }
        }

        public bool UseExponentialBackOff
        {
            get { return this.useExponentialBackOff; }
            set { this.useExponentialBackOff = value; }
        }

        public string Group
        {
            get { return this.group; }
            set { this.group = value; }
        }

        public ServiceAddHandler ServiceAdd
        {
            get { return serviceAddHandler; }
            set { this.serviceAddHandler = value; }
        }

        public ServiceRemoveHandler ServiceRemove
        {
            get { return serviceRemoveHandler; }
            set { this.serviceRemoveHandler = value; }
        }

        public Uri DiscoveryURI
        {
            get { return discoveryUri; }
            set { discoveryUri = value; }
        }

        public bool IsStarted
        {
            get { return started.Value; }
        }

        #endregion

        #region Abstract methods

        /// <summary>
        /// Gets or sets the keep alive interval.  This interval controls the amount 
        /// of time that a service is kept before being considered idle and removed from
        /// the list of discovered services.  This value is also used to control the
        /// period of time that this service will wait before advertising itself.
        /// </summary>
        public abstract long KeepAliveInterval
        {
            get;
            set;
        }

        /// <summary>
        /// Overriden by the actual agent class to handle the publish of this service
        /// if supported by the agent.
        /// </summary>
        protected abstract void DoAdvertizeSelf();

        /// <summary>
        /// Overriden by the agent class to handle starting any agent related services
        /// or opening resources needed for the agent.
        /// </summary>
        protected abstract void DoStartAgent();

        /// <summary>
        /// Overriden by the agent to handle shutting down any agent created resources.
        /// </summary>
        protected abstract void DoStopAgent();

        /// <summary>
        /// Called from the Agent background thread to allow the concrete agent implementation
        /// to perform its discovery of new services.  
        /// </summary>
        protected abstract void DoDiscovery();

        #endregion

        public void Start()
        {
            if (started.CompareAndSet(false, true)) {                                      
                DoStartAgent();

                if (worker == null)
                {
                    Tracer.Info("Starting multicast discovery agent worker thread");
                    worker = new Thread(new ThreadStart(DiscoveryAgentRun));
                    worker.IsBackground = true;
                    worker.Start();
                }

                DoAdvertizeSelf();
            }
        }

        public void Stop()
        {
            // Changing the isStarted flag will signal the thread that it needs to shut down.
            if (started.CompareAndSet(true, false))
            {
                DoStopAgent();
                if(worker != null)
                {
                    // wait for the worker to stop.
                    if(!worker.Join(WORKER_KILL_TIME_SECONDS))
                    {
                        Tracer.Info("!! Timeout waiting for discovery agent localThread to stop");
                        worker.Abort();
                    }
                    worker = null;
                    Tracer.Debug("Multicast discovery agent worker thread stopped");
                }
                executor.Shutdown();
                if (!executor.AwaitTermination(TimeSpan.FromMinutes(1)))
                {
                    Tracer.DebugFormat("Failed to properly shutdown agent executor {0}", this);
                }
            }
        }

        public void Dispose()
        {
            if (started.Value)
            {
                Stop();
            }
        }

        public void RegisterService(String name)
        {
            this.selfService = name;
            if (started.Value)
            {
                try 
                {
                    DoAdvertizeSelf();
                } 
                catch (Exception e) 
                {
                    // If a the advertise fails, chances are all subsequent sends will fail
                    // too.. No need to keep reporting the same error over and over.
                    if (reportAdvertizeFailed) 
                    {
                        reportAdvertizeFailed = false;
                        Tracer.ErrorFormat("Failed to advertise our service: {0} cause: {1}", selfService, e.Message);
                    }
                }
            }
        }

        public void ServiceFailed(DiscoveryEvent failedEvent)
        {
            DiscoveredServiceData data = null;
            discoveredServices.TryGetValue(failedEvent.ServiceName, out data);
            if (data != null && MarkFailed(data)) 
            {
                FireServiceRemoveEvent(data);
            }
        }

        protected void FireServiceRemoveEvent(DiscoveryEvent data) 
        {
            if (serviceRemoveHandler != null && started.Value) 
            {
                // Have the listener process the event async so that
                // he does not block this thread since we are doing time sensitive
                // processing of events.
                executor.QueueUserWorkItem(ServiceRemoveCallback, data);
            }
        }

        private void ServiceRemoveCallback(object data)
        {
            DiscoveryEvent serviceData = data as DiscoveryEvent;
            this.serviceRemoveHandler(serviceData);
        }

        protected void FireServiceAddEvent(DiscoveryEvent data) 
        {
            if (serviceAddHandler != null && started.Value) 
            {
                // Have the listener process the event async so that
                // he does not block this thread since we are doing time sensitive
                // processing of events.
                executor.QueueUserWorkItem(ServiceAddCallback, data);
            }
        }

        private void ServiceAddCallback(object data)
        {
            DiscoveryEvent serviceData = data as DiscoveryEvent;
            this.serviceAddHandler(serviceData);
        }

        private void DiscoveryAgentRun()
        {
            Thread.CurrentThread.Name = "Discovery Agent Thread.";
            while (started.Value)
            {
                DoTimeKeepingServices();
                try
                {
                    DoDiscovery();
                }
                catch (ThreadInterruptedException)
                {
                    return;
                }
                catch (Exception)
                {
                }
            }
        }

        private void DoTimeKeepingServices() 
        {
            if (started.Value)
            {
                DateTime currentTime = DateTime.Now;
                if (currentTime < LastAdvertizeTime || 
                    ((currentTime - TimeSpan.FromMilliseconds(KeepAliveInterval)) > LastAdvertizeTime))
                {
                    DoAdvertizeSelf();
                    LastAdvertizeTime = currentTime;
                }
                DoExpireOldServices();
            }
        }

        private void DoExpireOldServices() 
        {
            DateTime expireTime = DateTime.Now - TimeSpan.FromMilliseconds(KeepAliveInterval * HEARTBEAT_MISS_BEFORE_DEATH); 

            DiscoveredServiceData[] services = null;
            lock (discoveredServicesLock)
            {
                services = new DiscoveredServiceData[this.discoveredServices.Count];
                this.discoveredServices.Values.CopyTo(services, 0);
            }

            foreach(DiscoveredServiceData service in services)
            {
                if (service.LastHeartBeat < expireTime) 
                {
                    ProcessDeadService(service.ServiceName);
                }
            }
        }

        protected void ProcessLiveService(string brokerName, string service)
        {
            if (SelfService == null || !service.Equals(SelfService)) 
            {
                DiscoveredServiceData remoteBroker = null;
                lock (discoveredServicesLock)
                {
                    discoveredServices.TryGetValue(service, out remoteBroker);
                }
                if (remoteBroker == null) 
                {
                    remoteBroker = new DiscoveredServiceData(brokerName, service);
                    discoveredServices.Add(service, remoteBroker);      
                    FireServiceAddEvent(remoteBroker);
                    DoAdvertizeSelf();
                } 
                else 
                {
                    UpdateHeartBeat(remoteBroker);
                    if (IsTimeForRecovery(remoteBroker)) 
                    {
                        FireServiceAddEvent(remoteBroker);
                    }
                }
            }
        }

        protected void ProcessDeadService(string service) 
        {
            if (!service.Equals(SelfService)) 
            {
                DiscoveredServiceData remoteBroker = null;
                lock (discoveredServicesLock)
                {
                    discoveredServices.TryGetValue(service, out remoteBroker);
                    if (remoteBroker != null)
                    {
                        discoveredServices.Remove(service);
                    }
                }
                if (remoteBroker != null && !remoteBroker.Failed) 
                {
                    FireServiceRemoveEvent(remoteBroker);
                }
            }
        }

        #region DiscoveredServiceData maintenance methods

        /// <summary>
        /// Returns true if this Broker has been marked as failed and it is now time to
        /// start a recovery attempt.
        /// </summary>
        public bool IsTimeForRecovery(DiscoveredServiceData service) 
        {
            lock (service.SyncRoot)
            {
                if (!service.Failed) 
                {
                    return false;
                }

                int maxReconnectAttempts = MaxReconnectAttempts;

                // Are we done trying to recover this guy?
                if (maxReconnectAttempts > 0 && service.FailureCount > maxReconnectAttempts) 
                {
                    Tracer.DebugFormat("Max reconnect attempts of the {0} service has been reached.", service.ServiceName);
                    return false;
                }

                // Is it not yet time?
                if (DateTime.Now < service.RecoveryTime) 
                {
                    return false;
                }

                Tracer.DebugFormat("Resuming event advertisement of the {0} service.", service.ServiceName);

                service.Failed = false;
                return true;
            }
        }

        internal void UpdateHeartBeat(DiscoveredServiceData service)
        {
            lock (service.SyncRoot)
            {
                service.LastHeartBeat = DateTime.Now;

                // Consider that the broker recovery has succeeded if it has not failed in 60 seconds.
                if (!service.Failed && service.FailureCount > 0 && 
                    (service.LastHeartBeat - service.RecoveryTime) > TimeSpan.FromMilliseconds(1000 * 60)) {

                    Tracer.DebugFormat("I now think that the {0} service has recovered.", service.ServiceName);

                    service.FailureCount = 0;
                    service.RecoveryTime = DateTime.MinValue;
                }
            }
        }

        internal bool MarkFailed(DiscoveredServiceData service)
        {
            lock (service.SyncRoot)
            {
                if (!service.Failed) 
                {
                    service.Failed = true;
                    service.FailureCount++;

                    long reconnectDelay = 0;
                    if (!UseExponentialBackOff) 
                    {
                        reconnectDelay = InitialReconnectDelay;
                    } 
                    else 
                    {
                        reconnectDelay = (long)Math.Pow(BackOffMultiplier, service.FailureCount);
                        reconnectDelay = Math.Min(reconnectDelay, MaxReconnectDelay);
                    }

                    Tracer.DebugFormat("Remote failure of {0} while still receiving multicast advertisements.  " +
                                       "Advertising events will be suppressed for {1} ms, the current " +
                                       "failure count is: {2}", 
                                       service.ServiceName, reconnectDelay, service.FailureCount);

                    service.RecoveryTime = DateTime.Now + TimeSpan.FromMilliseconds(reconnectDelay);
                    return true;
                }
            }
            return false;            
        }

        #endregion
    }
}

