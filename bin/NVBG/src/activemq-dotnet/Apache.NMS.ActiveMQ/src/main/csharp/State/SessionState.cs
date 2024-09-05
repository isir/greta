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
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.State
{
	public class SessionState
	{
	    readonly SessionInfo info;

		private readonly AtomicDictionary<ProducerId, ProducerState> producers = 
			new AtomicDictionary<ProducerId, ProducerState>();
		private readonly AtomicDictionary<ConsumerId, ConsumerState> consumers = 
			new AtomicDictionary<ConsumerId, ConsumerState>();
		private readonly Atomic<bool> isShutdown = new Atomic<bool>(false);

		public SessionState(SessionInfo info)
		{
			this.info = info;
		}

		public override String ToString()
		{
			return info.ToString();
		}

		public void AddProducer(ProducerInfo info)
		{
			CheckShutdown();
			ProducerState producerState = new ProducerState(info);

			if(producers.ContainsKey(info.ProducerId))
			{
				producers[info.ProducerId] = producerState;
			}
			else
			{
				producers.Add(info.ProducerId, producerState);
			}
		}

		public ProducerState RemoveProducer(ProducerId id)
		{
			CheckShutdown();
			ProducerState ret = null;

			if(producers.TryGetValue(id, out ret))
			{
				producers.Remove(id);
				if(null != ret && ret.TransactionState != null)
				{
					ret.TransactionState.AddProducer(ret);
				}
			}

			return ret;
		}

		public void AddConsumer(ConsumerInfo info)
		{
			CheckShutdown();
			ConsumerState consumerState = new ConsumerState(info);

			if(consumers.ContainsKey(info.ConsumerId))
			{
				consumers.Add(info.ConsumerId, consumerState);
			}
			else
			{
				consumers.Add(info.ConsumerId, consumerState);
			}
		}

		public ConsumerState RemoveConsumer(ConsumerId id)
		{
			CheckShutdown();
			ConsumerState ret = null;

			if(consumers.TryGetValue(id, out ret))
			{
				consumers.Remove(id);
			}

			return ret;
		}

		public SessionInfo Info
		{
            get { return info; }
		}

		public AtomicCollection<ConsumerId> ConsumerIds
		{
			get { return consumers.Keys; }
		}

		public AtomicCollection<ProducerId> ProducerIds
		{
			get { return producers.Keys; }
		}

		public AtomicCollection<ProducerState> ProducerStates
		{
			get { return producers.Values; }
		}

		public ProducerState getProducerState(ProducerId producerId)
		{
			return producers[producerId];
		}

		public ProducerState this[ProducerId producerId]
		{
			get { return producers[producerId]; }
		}

		public AtomicCollection<ConsumerState> ConsumerStates
		{
			get { return consumers.Values; }
		}

		public ConsumerState getConsumerState(ConsumerId consumerId)
		{
			return consumers[consumerId];
		}

		public ConsumerState this[ConsumerId consumerId]
		{
            get { return consumers[consumerId]; }
		}

		private void CheckShutdown()
		{
			if(isShutdown.Value)
			{
				throw new ApplicationException("Disposed");
			}
		}

		public void Shutdown()
		{
			isShutdown.Value = true;
			producers.Clear();
			consumers.Clear();
		}
	}
}
