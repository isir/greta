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
	/// <summary>
	/// Class to provide support for working with Session objects.
	/// </summary>
	public class SessionUtil
	{
		private const string QueuePrefix = "queue://";
		private const string TopicPrefix = "topic://";
		private const string TempQueuePrefix = "temp-queue://";
		private const string TempTopicPrefix = "temp-topic://";

		/// <summary>
		/// Get the destination by parsing the embedded type prefix.  Default is Queue if no prefix is
		/// embedded in the destinationName.
		/// </summary>
		/// <param name="session">Session object to use to get the destination.</param>
		/// <param name="destinationName">Name of destination with embedded prefix.  The embedded prefix can be one of the following:
		///		<list type="bullet">
		///			<item>queue://</item>
		///			<item>topic://</item>
		///			<item>temp-queue://</item>
		///			<item>temp-topic://</item>
		///		</list>
		///	</param>
		/// <returns></returns>
		public static IDestination GetDestination(ISession session, string destinationName)
		{
			return SessionUtil.GetDestination(session, destinationName, DestinationType.Queue);
		}

		/// <summary>
		/// Get the destination by parsing the embedded type prefix.
		/// </summary>
		/// <param name="session">Session object to use to get the destination.</param>
		/// <param name="destinationName">Name of destination with embedded prefix.  The embedded prefix can be one of the following:
		///		<list type="bullet">
		///			<item>queue://</item>
		///			<item>topic://</item>
		///			<item>temp-queue://</item>
		///			<item>temp-topic://</item>
		///		</list>
		///	</param>
		/// <param name="defaultType">Default type if no embedded prefix is specified.</param>
		/// <returns></returns>
		public static IDestination GetDestination(ISession session, string destinationName, DestinationType defaultType)
		{
			IDestination destination = null;
			DestinationType destinationType = defaultType;

			if(null != destinationName)
			{
				if(destinationName.Length > QueuePrefix.Length
					&& 0 == String.Compare(destinationName.Substring(0, QueuePrefix.Length), QueuePrefix, false))
				{
					destinationType = DestinationType.Queue;
					destinationName = destinationName.Substring(QueuePrefix.Length);
				}
				else if(destinationName.Length > TopicPrefix.Length
					&& 0 == String.Compare(destinationName.Substring(0, TopicPrefix.Length), TopicPrefix, false))
				{
					destinationType = DestinationType.Topic;
					destinationName = destinationName.Substring(TopicPrefix.Length);
				}
				else if(destinationName.Length > TempQueuePrefix.Length
					&& 0 == String.Compare(destinationName.Substring(0, TempQueuePrefix.Length), TempQueuePrefix, false))
				{
					destinationType = DestinationType.TemporaryQueue;
					destinationName = destinationName.Substring(TempQueuePrefix.Length);
				}
				else if(destinationName.Length > TempTopicPrefix.Length
					&& 0 == String.Compare(destinationName.Substring(0, TempTopicPrefix.Length), TempTopicPrefix, false))
				{
					destinationType = DestinationType.TemporaryTopic;
					destinationName = destinationName.Substring(TempTopicPrefix.Length);
				}
			}

			switch(destinationType)
			{
			case DestinationType.Queue:
				if(null != destinationName)
				{
					destination = session.GetQueue(destinationName);
				}
			break;

			case DestinationType.Topic:
				if(null != destinationName)
				{
					destination = session.GetTopic(destinationName);
				}
			break;

			case DestinationType.TemporaryQueue:
				destination = session.CreateTemporaryQueue();
			break;

			case DestinationType.TemporaryTopic:
				destination = session.CreateTemporaryTopic();
			break;
			}

			return destination;
		}

		public static IQueue GetQueue(ISession session, string queueName)
		{
			return GetDestination(session, queueName, DestinationType.Queue) as IQueue;
		}

		public static ITopic GetTopic(ISession session, string topicName)
		{
			return GetDestination(session, topicName, DestinationType.Topic) as ITopic;
		}

		/// <summary>
		/// Delete the named destination by parsing the embedded type prefix.  Default is Queue if no prefix is
		/// embedded in the destinationName.
		/// </summary>
		/// <param name="session">Session object to use to get the destination.</param>
		/// <param name="destinationName">Name of destination with embedded prefix.  The embedded prefix can be one of the following:
		///		<list type="bullet">
		///			<item>queue://</item>
		///			<item>topic://</item>
		///			<item>temp-queue://</item>
		///			<item>temp-topic://</item>
		///		</list>
		///	</param>
		/// <returns></returns>
		public static void DeleteDestination(ISession session, string destinationName)
		{
			SessionUtil.DeleteDestination(session, destinationName, DestinationType.Queue);
		}

		/// <summary>
		/// Delete the named destination by parsing the embedded type prefix.
		/// </summary>
		/// <param name="session">Session object to use to get the destination.</param>
		/// <param name="destinationName">Name of destination with embedded prefix.  The embedded prefix can be one of the following:
		///		<list type="bullet">
		///			<item>queue://</item>
		///			<item>topic://</item>
		///			<item>temp-queue://</item>
		///			<item>temp-topic://</item>
		///		</list>
		///	</param>
		/// <param name="defaultType">Default type if no embedded prefix is specified.</param>
		/// <returns></returns>
		public static void DeleteDestination(ISession session, string destinationName, DestinationType defaultType)
		{
			IDestination destination = SessionUtil.GetDestination(session, destinationName, defaultType);

			if(null != destination)
			{
				session.DeleteDestination(destination);
			}
		}

		public static void DeleteQueue(ISession session, string queueName)
		{
			DeleteDestination(session, queueName, DestinationType.Queue);
		}

		public static void DeleteTopic(ISession session, string topicName)
		{
			DeleteDestination(session, topicName, DestinationType.Topic);
		}
	}
}

