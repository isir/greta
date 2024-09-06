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

namespace Apache.NMS.ActiveMQ.Util
{
	public class ActiveMQMessageAudit
	{
	    public const int DEFAULT_WINDOW_SIZE = 2048;
	    public const int MAXIMUM_PRODUCER_COUNT = 64;

        private readonly object mutex = new object();

	    private int auditDepth;
	    private int maximumNumberOfProducersToTrack;
		private LRUCache<Object, BitArrayBin> map;

		public int AuditDepth
		{
			get { return this.auditDepth; }
			set { this.auditDepth = value; }
		}

		public int MaximumNumberOfProducersToTrack
		{
			get { return this.maximumNumberOfProducersToTrack; }
			set 
			{ 
	            lock(this.mutex)
				{
	        		if (value < this.maximumNumberOfProducersToTrack)
					{
	            		LRUCache<Object, BitArrayBin> newMap = new LRUCache<Object, BitArrayBin>(value);
		            	
		             	// As putAll will access the entries in the right order,
		             	// this shouldn't result in wrong cache entries being removed
		            	newMap.PutAll(this.map);
		            	this.map.Clear();
		            	this.map.PutAll(newMap);
		        	}
	        		this.map.MaxCacheSize = value;
	        		this.maximumNumberOfProducersToTrack = value;
				}
			}
		}

	    public ActiveMQMessageAudit()
		{
	        this.auditDepth = DEFAULT_WINDOW_SIZE;
	        this.maximumNumberOfProducersToTrack = MAXIMUM_PRODUCER_COUNT;
	        this.map = new LRUCache<Object, BitArrayBin>(MAXIMUM_PRODUCER_COUNT);
	    }

	    public ActiveMQMessageAudit(int auditDepth, int maximumNumberOfProducersToTrack)
		{
	        this.auditDepth = auditDepth;
	        this.maximumNumberOfProducersToTrack = maximumNumberOfProducersToTrack;
	        this.map = new LRUCache<Object, BitArrayBin>(maximumNumberOfProducersToTrack);
	    }

	    public bool IsDuplicate(MessageId id) 
		{
	        bool answer = false;

	        if (id != null) 
			{
	            ProducerId pid = id.ProducerId;
	            if (pid != null)
				{
            		lock(this.mutex)
					{
		                BitArrayBin bab = null;
		                if (!map.TryGetValue(pid, out bab)) 
						{
		                    bab = new BitArrayBin(auditDepth);
		                    map[pid] = bab;
		                }
		                answer = bab.SetBit(id.ProducerSequenceId, true);
					}
	            }
	        }
	        return answer;
	    }

	    public void Rollback(MessageId id) 
		{
	        if (id != null)
			{
	            ProducerId pid = id.ProducerId;
	            if (pid != null)
				{
            		lock(this.mutex)
					{
		                BitArrayBin bab = null;
		                if (map.TryGetValue(pid, out bab)) 
						{
		                    bab.SetBit(id.ProducerSequenceId, false);
		                }
					}
	            }
	        }
	    }

	    public bool IsInOrder(MessageId id) 
		{
	        bool answer = false;

	        if (id != null) 
			{
	            ProducerId pid = id.ProducerId;
	            if (pid != null) 
				{
            		lock(this.mutex)
					{
		                BitArrayBin bab = null;
		                if (!map.TryGetValue(pid, out bab)) 
						{
		                    bab = new BitArrayBin(auditDepth);
		                    map[pid] = bab;
		                }
		                answer = bab.IsInOrder(id.ProducerSequenceId);
					}
	            }
	        }
	        return answer;
	    }

	    public long GetLastSeqId(ProducerId id) 
		{
	        long result = -1;
            BitArrayBin bab = null;

    		lock(this.mutex)
			{
				if (map.TryGetValue(id, out bab)) 
				{
		            result = bab.GetLastSetIndex();
		        }
			}
	        return result;
	    }

	    public void Clear() 
		{
    		lock(this.mutex)
			{
	        	map.Clear();
			}
	    }
	}
}

