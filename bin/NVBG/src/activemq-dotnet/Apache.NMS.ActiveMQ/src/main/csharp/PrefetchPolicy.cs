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

namespace Apache.NMS.ActiveMQ
{
    /// <summary>
    /// Class used to define the various limits that should be used for the Prefetch
    /// limit on destination based on the type of Destination in use.
    /// </summary>
    public class PrefetchPolicy : ICloneable
    {
        public const int MAX_PREFETCH_SIZE = Int16.MaxValue - 1;
        public const int DEFAULT_QUEUE_PREFETCH = 1000;
        public const int DEFAULT_QUEUE_BROWSER_PREFETCH = 500;
        public const int DEFAULT_DURABLE_TOPIC_PREFETCH = 100;
        public const int DEFAULT_TOPIC_PREFETCH = MAX_PREFETCH_SIZE;
        
        private int queuePrefetch;
        private int queueBrowserPrefetch;
        private int topicPrefetch;
        private int durableTopicPrefetch;
        private int maximumPendingMessageLimit;
        
        public PrefetchPolicy()
        {
            this.queuePrefetch = DEFAULT_QUEUE_PREFETCH;
            this.queueBrowserPrefetch = DEFAULT_QUEUE_BROWSER_PREFETCH;
            this.topicPrefetch = DEFAULT_TOPIC_PREFETCH;
            this.durableTopicPrefetch = DEFAULT_DURABLE_TOPIC_PREFETCH;
        }

        public int QueuePrefetch
        {
            get { return this.queuePrefetch; }
            set { this.queuePrefetch = RestrictToMaximum(value); }
        }

        public int QueueBrowserPrefetch
        {
            get { return this.queueBrowserPrefetch; }
            set { this.queueBrowserPrefetch = RestrictToMaximum(value); }
        }

        public int TopicPrefetch
        {
            get { return this.topicPrefetch; }
            set { this.topicPrefetch = RestrictToMaximum(value); }
        }

        public int DurableTopicPrefetch
        {
            get { return this.durableTopicPrefetch; }
            set { this.durableTopicPrefetch = RestrictToMaximum(value); }
        }

        public int MaximumPendingMessageLimit
        {
            get { return this.maximumPendingMessageLimit; }
            set { this.maximumPendingMessageLimit = value; }
        }
		
		public int All
		{
			set { this.SetAll(value); }
		}
        
        public void SetAll(int value)
        {
            this.queuePrefetch = value;
            this.queueBrowserPrefetch = value;
            this.topicPrefetch = value;
            this.durableTopicPrefetch = value;
        }

        private static int RestrictToMaximum(int value)
        {
            return System.Math.Min(value, MAX_PREFETCH_SIZE);
        }

        public Object Clone()
        {
            return this.MemberwiseClone();
        }
    }
}
