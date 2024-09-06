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
	public static class MessageExtensions
	{
		/// <summary>
		/// Deserializes the object from Xml, and returns it.
		/// </summary>
		public static object ToObject(this IMessage message)
		{
			return ToObject<object>(message);
		}

		/// <summary>
		/// Deserializes the object from Xml, and returns it.
		/// </summary>
		public static T ToObject<T>(this IMessage message) where T : class
		{
			try
			{
				if(null != message)
				{
					return (T) NMSConvert.DeserializeObjFromMessage(message);
				}
			}
			catch(Exception ex)
			{
				Tracer.ErrorFormat("Error converting message to object: {0}", ex.Message);
			}

			return null;
		}
	}
#endif
}
