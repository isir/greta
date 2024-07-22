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

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// Implements the basic IDictionary interface and adds functionality for controlling
    /// the maximum number of entries that can be contained in the Map.  When the maximum
    /// value is reached the oldest entry is removed so that the max size is never exceeded.
    /// </summary>
	public class LRUCache<TKey, TValue> : IEnumerable<KeyValuePair<TKey, TValue>>
	{
        public const int DEFAULT_MAX_CACHE_SIZE = 10000;

        private readonly Dictionary<TKey, TValue> dictionary = new Dictionary<TKey, TValue>();
        private readonly LinkedList<KeyValuePair<TKey, TValue>> entries =
            new LinkedList<KeyValuePair<TKey, TValue>>();
		private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;

        public LRUCache() : base()
        {
        }

        public LRUCache(int maxCacheSize) : base()
        {
            this.maxCacheSize = maxCacheSize;
        }

		public void Clear()
		{
			dictionary.Clear();
            entries.Clear();
		}

        public int Count
        {
            get { return this.dictionary.Count; }
        }
		
		public int MaxCacheSize
		{
			get { return maxCacheSize; }
			set { this.maxCacheSize = value; }
		}

		public TValue this[TKey key]
		{
			get { return dictionary[key]; }
			set 
			{ 
				TValue currentValue;
				// Moved used item to end of list since it been used again.
				if (dictionary.TryGetValue(key, out currentValue))
				{
					KeyValuePair<TKey, TValue> entry = 
                        new KeyValuePair<TKey, TValue>(key, currentValue);
					entries.Remove(entry);
				}

				dictionary[key] = value;
	            entries.AddLast(new KeyValuePair<TKey, TValue>(key, value));

	            KeyValuePair<TKey, TValue> eldest = entries.First.Value;

			    if(this.RemoveEldestEntry(eldest))
	            {
	                this.dictionary.Remove(eldest.Key);
	                this.entries.RemoveFirst();
	            }
			}
		}

		public bool TryGetValue(TKey key, out TValue val)
		{
			return dictionary.TryGetValue(key, out val);
		}

		public ICollection<TKey> Keys
		{
			get { return dictionary.Keys; }
		}

		public ICollection<TValue> Values
		{
			get { return dictionary.Values; }
		}

		public void Add(TKey key, TValue val)
        {
    		dictionary.Add(key, val);
            entries.AddLast(new KeyValuePair<TKey, TValue>(key, val));

            KeyValuePair<TKey, TValue> eldest = entries.First.Value;

		    if(this.RemoveEldestEntry(eldest))
            {
                this.dictionary.Remove(eldest.Key);
                this.entries.RemoveFirst();
            }
		}

		public bool Remove(TKey v)
		{
			return dictionary.Remove(v);
		}

        public bool ContainsKey(TKey key)
        {
            return this.dictionary.ContainsKey(key);
        }

        public bool ContainsValue(TValue theValue)
        {
            return this.dictionary.ContainsValue(theValue);
        }

        public IEnumerator<KeyValuePair<TKey, TValue>> GetEnumerator()
        {
            return dictionary.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return dictionary.GetEnumerator();
        }

		protected virtual bool RemoveEldestEntry(KeyValuePair<TKey, TValue> eldest)
		{
			return this.dictionary.Count > this.maxCacheSize;
		}

		public void PutAll(LRUCache<TKey, TValue> source)
		{
			if (Object.Equals(source, this))
			{
				return;
			}

			foreach(KeyValuePair<TKey, TValue> entry in source.entries)
			{
				this.Add(entry.Key, entry.Value);
			}
		}
	}

}

