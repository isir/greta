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
using System.Collections;
using System.Threading;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport
{
    /// <summary>
    /// A Transport that correlates asynchronous send/receive messages into single request/response.
    /// </summary>
    public class ResponseCorrelator : TransportFilter
    {
        private readonly IDictionary requestMap = Hashtable.Synchronized(new Hashtable());
        private int nextCommandId;
		private Exception error;

        public ResponseCorrelator(ITransport next) : base(next)
        {
        }

        protected override void OnException(ITransport sender, Exception command)
        {
			Dispose(command);
            base.OnException(sender, command);
        }

        internal int GetNextCommandId()
        {
            return Interlocked.Increment(ref nextCommandId);
        }

        public override void Oneway(Command command)
        {
            command.CommandId = GetNextCommandId();
			command.ResponseRequired = false;
            next.Oneway(command);
        }

        public override FutureResponse AsyncRequest(Command command)
        {
            int commandId = GetNextCommandId();

            command.CommandId = commandId;
            command.ResponseRequired = true;
            FutureResponse future = new FutureResponse();
	        Exception priorError = null;
	        lock(requestMap.SyncRoot) 
			{
	            priorError = this.error;
	            if(priorError == null) 
				{
		            requestMap[commandId] = future;
	            }
	        }
	
	        if(priorError != null) 
			{
				BrokerError brError = new BrokerError();
				brError.Message = priorError.Message;
				ExceptionResponse response = new ExceptionResponse();
				response.Exception = brError;
	            future.Response = response;
                return future;
	        }
			
            next.Oneway(command);

			return future;
        }

        public override Response Request(Command command, TimeSpan timeout)
        {
            FutureResponse future = AsyncRequest(command);
            future.ResponseTimeout = timeout;
            Response response = future.Response;
            return response;
        }

        protected override void OnCommand(ITransport sender, Command command)
        {
            if(command.IsResponse)
            {
                Response response = (Response) command;
                int correlationId = response.CorrelationId;
                FutureResponse future = (FutureResponse) requestMap[correlationId];

                if(future != null)
                {
                    requestMap.Remove(correlationId);
                    future.Response = response;
                }
                else
                {
                    if(Tracer.IsDebugEnabled)
                    {
                        Tracer.Debug("Unknown response ID: " + response.CorrelationId + " for response: " + response);
                    }
                }
            }
            else
            {
                this.commandHandler(sender, command);
            }
        }
		
		public override void Stop()
		{
			this.Dispose(new IOException("Stopped"));
			base.Stop();
		}
		
		private void Dispose(Exception error)
		{
			ArrayList requests = null;
			
	        lock(requestMap.SyncRoot) 
			{
	            if(this.error == null) 
				{
	                this.error = error;
	                requests = new ArrayList(requestMap.Values);
	                requestMap.Clear();
	            }
	        }
			
	        if(requests != null)
			{
				foreach(FutureResponse future in requests)
				{
					BrokerError brError = new BrokerError();
					brError.Message = error.Message;
					ExceptionResponse response = new ExceptionResponse();
					response.Exception = brError;
		            future.Response = response;
				}
	        }
		}
		
    }
}


