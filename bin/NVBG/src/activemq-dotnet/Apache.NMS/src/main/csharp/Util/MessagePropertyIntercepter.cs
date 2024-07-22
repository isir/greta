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
using System.Reflection;

namespace Apache.NMS.Util
{
	/// <summary>
    /// Utility class used to set NMS properties via introspection for IMessage derived
    /// instances.  This class allows IMessage classes to define Message specific properties
    /// that can be accessed using the standard property get / set semantics.
    ///
    /// This is especially useful for NMSX type properties which can vary by provider and
    /// are obtianed via a call to IConnectionMetaData.NMSXPropertyNames.  The client can
    /// set the properties on an IMessage instance without a direct cast to the providers
    /// specific Message types.
    ///
    /// Properties accessed in this way are treated as NMS Message headers which are never
    /// read-only therefore there is no exception thrown if the message itself is in the
    /// read-only property mode.
    /// </summary>
	public class MessagePropertyIntercepter : PrimitiveMapInterceptor
	{
		private const BindingFlags publicBinding = BindingFlags.Public | BindingFlags.Instance;
		private readonly Type messageType;

		public MessagePropertyIntercepter(IMessage message, IPrimitiveMap properties)
            : base(message, properties)
		{
			this.messageType = message.GetType();
		}

        public MessagePropertyIntercepter(IMessage message, IPrimitiveMap properties, bool readOnly)
            : base(message, properties, readOnly)
        {
            this.messageType = message.GetType();
        }
        
		protected override object GetObjectProperty(string name)
		{
			PropertyInfo propertyInfo = this.messageType.GetProperty(name, publicBinding);

			if(name.StartsWith("NMS"))
			{			
				if(null != propertyInfo && propertyInfo.CanRead)
				{
					return propertyInfo.GetValue(this.message, null);
				}
				else
				{
					FieldInfo fieldInfo = this.messageType.GetField(name, publicBinding);
	
					if(null != fieldInfo)
					{
						return fieldInfo.GetValue(this.message);
					}
				}
			}
			
			return base.GetObjectProperty(name);
		}

		protected override void SetObjectProperty(string name, object value)
		{
			PropertyInfo propertyInfo = this.messageType.GetProperty(name, publicBinding);

			if(!name.StartsWith("NMS"))
			{
                base.SetObjectProperty(name, value);
			}			
			else if(null != propertyInfo && propertyInfo.CanWrite)
			{
				propertyInfo.SetValue(this.message, value, null);
			}
			else
			{
				FieldInfo fieldInfo = this.messageType.GetField(name, publicBinding);

				if(null != fieldInfo && !fieldInfo.IsLiteral && !fieldInfo.IsInitOnly)
				{
					fieldInfo.SetValue(this.message, value);
				}
				else
				{
                    base.SetObjectProperty(name, value);
				}
			}
		}
	}
}
