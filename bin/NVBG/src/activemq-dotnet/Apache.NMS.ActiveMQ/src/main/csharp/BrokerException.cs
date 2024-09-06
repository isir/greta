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
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ
{
	/// <summary>
	/// Exception thrown when the broker returns an error
	/// </summary>
	[Serializable]
	public class BrokerException : NMSException
	{
		private readonly BrokerError brokerError = null;

		public BrokerException()
			: base("Broker failed with missing exception log")
		{
		}

		public BrokerException(BrokerError brokerError)
			: this(brokerError, null)
		{
		}

		public BrokerException(BrokerError brokerError, Exception innerException)
			: base(brokerError.ExceptionClass + " : " + brokerError.Message + "\n" + StackTraceDump(brokerError.StackTraceElements),
					innerException)
		{
			this.brokerError = brokerError;
		}

		#region ISerializable interface implementation

		/// <summary>
		/// Initializes a new instance of the BrokerException class with serialized data.
		/// Throws System.ArgumentNullException if the info parameter is null.
		/// Throws System.Runtime.Serialization.SerializationException if the class name is null or System.Exception.HResult is zero (0).
		/// </summary>
		/// <param name="info">The SerializationInfo that holds the serialized object data about the exception being thrown.</param>
		/// <param name="context">The StreamingContext that contains contextual information about the source or destination.</param>
		protected BrokerException(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context)
			: base(info, context)
		{
			brokerError = info.GetValue("BrokerException.brokerError", typeof(BrokerError)) as BrokerError;
		}

		/// <summary>
		/// When overridden in a derived class, sets the SerializationInfo
		/// with information about the exception.
		/// </summary>
		/// <param name="info">The SerializationInfo that holds the serialized object data about the exception being thrown.</param>
		/// <param name="context">The StreamingContext that contains contextual information about the source or destination.</param>
		public override void GetObjectData(System.Runtime.Serialization.SerializationInfo info, System.Runtime.Serialization.StreamingContext context)
		{
			base.GetObjectData(info, context);
			info.AddValue("BrokerException.brokerError", brokerError);
		}

		#endregion

		/// <summary>
		/// Generates a nice textual stack trace
		/// </summary>
		public static string StackTraceDump(StackTraceElement[] elements)
		{
			StringBuilder builder = new StringBuilder();
			if(elements != null)
			{
				foreach(StackTraceElement e in elements)
				{
					builder.Append("\n " + e.ClassName + "." + e.MethodName + "(" + e.FileName + ":" + e.LineNumber + ")");
				}
			}
			return builder.ToString();
		}

		public BrokerError BrokerError
		{
			get { return brokerError; }
		}

		public virtual string JavaStackTrace
		{
			get { return brokerError.StackTrace; }
		}
	}
}
