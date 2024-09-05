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

namespace Apache.NMS
{
	/// <summary>
	/// Define an enumerated array of message priorities.
	/// </summary>
	public enum MsgPriority
	{
		Lowest			= 0,
		VeryLow			= 1,
		Low				= 2,
		AboveLow		= 3,
		BelowNormal		= 4,
		Normal			= 5,
		AboveNormal		= 6,
		High			= 7,
		VeryHigh		= 8,
		Highest			= 9
	}

	/// <summary>
	/// Define an enumerated array of message delivery modes.  Provider-specific
	/// values can be used to extend this enumerated mode.  TIBCO is known to
	/// provide a third value of ReliableDelivery.  At minimum, a provider must
	/// support Persistent and NonPersistent.
	/// </summary>
	public enum MsgDeliveryMode
	{
		Persistent,
		NonPersistent
	}

	/// <summary>
	/// Defines a number of constants
	/// </summary>
	public class NMSConstants
	{
		public const MsgPriority defaultPriority = MsgPriority.Normal;
		public const MsgDeliveryMode defaultDeliveryMode = MsgDeliveryMode.Persistent;
		public static readonly TimeSpan defaultTimeToLive = TimeSpan.Zero;
		public static readonly TimeSpan defaultRequestTimeout = TimeSpan.FromMilliseconds(Timeout.Infinite);
	}
}


