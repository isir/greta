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
using System.Text;
using Apache.NMS.Util;

namespace Apache.NMS
{
#if NET_3_5 || MONO
	public static class MessageProducerExtensions
	{
		/// <summary>
		/// Extension function to create a text message from an object.  The object must be serializable to XML.
		/// </summary>
		public static ITextMessage CreateXmlMessage(this IMessageProducer producer, object obj)
		{
			return NMSConvert.SerializeObjToMessage(producer.CreateTextMessage(), obj);
		}

		/// <summary>
		/// Sends the message to the default destination for this producer.  The object must be serializable to XML.
		/// </summary>
		public static void Send(this IMessageProducer producer, object objMessage)
		{
			producer.Send(producer.CreateXmlMessage(objMessage));
		}

		/// <summary>
		/// Sends the message to the default destination with the explicit QoS configuration.  The object must be serializable to XML.
		/// </summary>
		public static void Send(this IMessageProducer producer, object objMessage, MsgDeliveryMode deliveryMode, MsgPriority priority, TimeSpan timeToLive)
		{
			producer.Send(producer.CreateXmlMessage(objMessage), deliveryMode, priority, timeToLive);
		}

		/// <summary>
		/// Sends the message to the given destination
		/// </summary>
		public static void Send(this IMessageProducer producer, IDestination destination, object objMessage)
		{
			producer.Send(destination, producer.CreateXmlMessage(objMessage));
		}

		/// <summary>
		/// Sends the message to the given destination with the explicit QoS configuration.  The object must be serializable to XML.
		/// </summary>
		public static void Send(this IMessageProducer producer, IDestination destination, object objMessage, MsgDeliveryMode deliveryMode, MsgPriority priority, TimeSpan timeToLive)
		{
			producer.Send(destination, producer.CreateXmlMessage(objMessage), deliveryMode, priority, timeToLive);
		}
	}
#endif
}
