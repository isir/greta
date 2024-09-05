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

namespace Apache.NMS.ActiveMQ.Transport
{
	public interface ICompositeTransport : ITransport
	{
		/// <summary>
		/// Adds a new set of Uris to the list of Uris that this Transport can connect to.
		/// </summary>
		/// <param name="rebalance">
		/// A <see cref="System.Boolean"/>
		/// Should the current connection be broken and a new one created.
		/// </param>
		/// <param name="uris">
		/// A <see cref="Uri"/>
		/// </param>
		void Add(bool rebalance, Uri[] uris);

		/// <summary>
		/// Remove the given Uris from this Transports list of known Uris.
		/// </summary>
		/// <param name="rebalance">
		/// A <see cref="System.Boolean"/>
		/// Should the current connection be broken and a new one created.
		/// </param>
		/// <param name="uris">
		/// A <see cref="Uri"/>
		/// </param>
		void Remove(bool rebalance, Uri[] uris);
	}
}

