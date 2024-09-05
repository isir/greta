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
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Util
{
	public class ConnectionAudit
	{
		private readonly object mutex = new object();
	    
	    private readonly Dictionary<ActiveMQDestination, ActiveMQMessageAudit> destinations = 
			new Dictionary<ActiveMQDestination, ActiveMQMessageAudit>();
	    private readonly Dictionary<IDispatcher, ActiveMQMessageAudit> dispatchers =
			new Dictionary<IDispatcher, ActiveMQMessageAudit>();

		private bool checkForDuplicates = true;
		public bool CheckForDuplicates
		{
			get { return this.checkForDuplicates; }
			set { this.checkForDuplicates = value; }
		}

		private int auditDepth = ActiveMQMessageAudit.DEFAULT_WINDOW_SIZE;
		public int AuditDepth
		{
			get { return this.auditDepth; }
			set { this.auditDepth = value; }
		}

		private int auditMaximumProducerNumber = ActiveMQMessageAudit.MAXIMUM_PRODUCER_COUNT;
		public int AuditMaximumProducerNumber
		{
			get { return this.auditMaximumProducerNumber; }
			set { this.auditMaximumProducerNumber = value; }
		}

		public ConnectionAudit()
		{
		}

		public ConnectionAudit(int auditDepth, int auditMaximumProducerNumber)
		{
			this.auditDepth = auditDepth;
			this.auditMaximumProducerNumber = auditMaximumProducerNumber;
		}

	    public void RemoveDispatcher(IDispatcher dispatcher) 
		{
			lock(this.mutex)
			{
	        	dispatchers.Remove(dispatcher);
			}
	    }

	    public bool IsDuplicate(IDispatcher dispatcher, Message message)
		{
			bool result = false;

			lock(this.mutex) 
			{
		        if (checkForDuplicates && message != null) 
				{
		            ActiveMQDestination destination = message.Destination;
		            if (destination != null) 
					{
		                if (destination.IsQueue) 
						{
		                    ActiveMQMessageAudit audit = null;
		                    if (!destinations.TryGetValue(destination, out audit)) 
							{
		                        audit = new ActiveMQMessageAudit(auditDepth, auditMaximumProducerNumber);
		                        destinations.Add(destination, audit);
		                    }
		                    result = audit.IsDuplicate(message.MessageId);
		                }
						else 
						{
	                    	ActiveMQMessageAudit audit = null;
	                    	if (!dispatchers.TryGetValue(dispatcher, out audit)) 
							{
			                    audit = new ActiveMQMessageAudit(auditDepth, auditMaximumProducerNumber);
		                    	dispatchers.Add(dispatcher, audit);
		                	}
		                	result = audit.IsDuplicate(message.MessageId);
						}
		            }
		        }
			}
	        return result;
	    }

	    public void RollbackDuplicate(IDispatcher dispatcher, Message message) 
		{
			lock(this.mutex) 
			{
		        if (checkForDuplicates && message != null) 
				{
		            ActiveMQDestination destination = message.Destination;
		            if (destination != null)
					{
		                if (destination.IsQueue) 
						{
		                    ActiveMQMessageAudit audit = null;
		                    if (destinations.TryGetValue(destination, out audit)) 
							{
		                        audit.Rollback(message.MessageId);
		                    }
		                }
						else
						{
	                    	ActiveMQMessageAudit audit = null;
	                    	if (dispatchers.TryGetValue(dispatcher, out audit)) 
							{
		                        audit.Rollback(message.MessageId);
		                    }
		                }
		            }
		        }
			}
	    }
	}
}

