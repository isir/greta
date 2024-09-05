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

using System.Text;
using Apache.NMS.Util;

namespace Apache.NMS
{
#if NET_3_5 || MONO
	public static class SessionExtensions
	{
		/// <summary>
		/// Extension function to create a text message from an object.  The object must be serializable to XML.
		/// </summary>
		public static ITextMessage CreateXmlMessage(this ISession session, object obj)
		{
			return NMSConvert.SerializeObjToMessage(session.CreateTextMessage(), obj);
		}

		/// <summary>
		/// Extension function to get the destination by parsing the embedded type prefix.  Default is Queue if no prefix is
		/// embedded in the destinationName.
		/// </summary>
		public static IDestination GetDestination(this ISession session, string destinationName)
		{
			return SessionUtil.GetDestination(session, destinationName);
		}

		/// <summary>
		/// Extension function to get the destination by parsing the embedded type prefix.
		/// </summary>
		public static IDestination GetDestination(this ISession session, string destinationName, DestinationType defaultType)
		{
			return SessionUtil.GetDestination(session, destinationName, defaultType);
		}

		/// <summary>
		/// Extension function to get the destination by parsing the embedded type prefix.
		/// </summary>
		public static IQueue GetQueue(this ISession session, string queueName)
		{
			return SessionUtil.GetQueue(session, queueName);
		}

		/// <summary>
		/// Extension function to get the destination by parsing the embedded type prefix.
		/// </summary>
		public static ITopic GetTopic(this ISession session, string topicName)
		{
			return SessionUtil.GetTopic(session, topicName);
		}

		/// <summary>
		/// Extension function to delete the named destination by parsing the embedded type prefix.  Default is Queue if no prefix is
		/// embedded in the destinationName.
		/// </summary>
		public static void DeleteDestination(this ISession session, string destinationName)
		{
			SessionUtil.DeleteDestination(session, destinationName);
		}

		/// <summary>
		/// Extension function to delete the named destination by parsing the embedded type prefix.
		/// </summary>
		public static void DeleteDestination(this ISession session, string destinationName, DestinationType defaultType)
		{
			SessionUtil.DeleteDestination(session, destinationName, defaultType);
		}

		/// <summary>
		/// Extension function to delete the named destination by parsing the embedded type prefix.
		/// </summary>
		public static void DeleteQueue(this ISession session, string queueName)
		{
			SessionUtil.DeleteDestination(session, queueName);
		}

		/// <summary>
		/// Extension function to delete the named destination by parsing the embedded type prefix.
		/// </summary>
		public static void DeleteTopic(this ISession session, string topicName)
		{
			SessionUtil.DeleteDestination(session, topicName);
		}
	}
#endif
}
