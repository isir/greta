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

namespace Apache.NMS.Util
{
	public class AtomicReference<T>
	{
		protected T atomicValue;

		public T Value
		{
			get
			{
				lock(this)
				{
					return atomicValue;
				}
			}
			set
			{
				lock(this)
				{
					atomicValue = value;
				}
			}
		}

		public AtomicReference()
		{
			atomicValue = default(T);
		}

		public AtomicReference(T defaultValue)
		{
			atomicValue = defaultValue;
		}

		public T GetAndSet(T value)
		{
			lock(this)
			{
				T ret = atomicValue;
				atomicValue = value;
				return ret;
			}
		}
	}

	public class Atomic<T> : AtomicReference<T> where T : IComparable
	{
		public Atomic() : base()
		{
		}

		public Atomic(T defaultValue) : base(defaultValue)
		{
		}

		public bool CompareAndSet(T expected, T newValue)
		{
			lock(this)
			{
				if(0 == atomicValue.CompareTo(expected))
				{
					atomicValue = newValue;
					return true;
				}

				return false;
			}
		}
	}
}
