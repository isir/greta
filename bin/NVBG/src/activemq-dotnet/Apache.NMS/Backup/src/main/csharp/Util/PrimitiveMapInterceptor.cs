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
using System.Collections;

namespace Apache.NMS.Util
{
    /// <summary>
    /// This class provides a mechanism to intercept calls to a IPrimitiveMap
    /// instance and perform validation, handle type conversion, or some other
    /// function necessary to use the PrimitiveMap in a Message or other NMS
    /// object.
    ///
    /// Be default this class enforces the standard conversion policy for primitive
    /// types in NMS shown in the table below:
    ///
    ///   |        | boolean byte short char int long float double String byte[]
    ///   |----------------------------------------------------------------------
    ///   |boolean |    X                                            X
    ///   |byte    |          X     X         X   X                  X
    ///   |short   |                X         X   X                  X
    ///   |char    |                     X                           X
    ///   |int     |                          X   X                  X
    ///   |long    |                              X                  X
    ///   |float   |                                    X     X      X
    ///   |double  |                                          X      X
    ///   |String  |    X     X     X         X   X     X     X      X
    ///   |byte[]  |                                                       X
    ///   |----------------------------------------------------------------------
    ///
    /// </summary>
    public class PrimitiveMapInterceptor : IPrimitiveMap
    {
        protected IMessage message;
        protected IPrimitiveMap properties;
        private bool readOnly = false;
		private bool allowByteArrays = true;

        public PrimitiveMapInterceptor(IMessage message, IPrimitiveMap properties)
        {
            this.message = message;
            this.properties = properties;
        }

        public PrimitiveMapInterceptor(IMessage message, IPrimitiveMap properties, bool readOnly)
        {
            this.message = message;
            this.properties = properties;
            this.readOnly = readOnly;
        }
        
        public PrimitiveMapInterceptor(IMessage message, IPrimitiveMap properties, bool readOnly, bool allowByteArrays)
        {
            this.message = message;
            this.properties = properties;
            this.readOnly = readOnly;
			this.allowByteArrays = allowByteArrays;
        }
		
        protected virtual object GetObjectProperty(string name)
        {
            return this.properties[name];
        }

        protected virtual void SetObjectProperty(string name, object value)
        {
            FailIfReadOnly();

            try
            {
				if(!this.allowByteArrays && (value is byte[]))
				{
					throw new NotSupportedException("Byte Arrays not allowed in this PrimitiveMap");
				}
				
                this.properties[name] = value;
            }
            catch(Exception ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        #region IPrimitiveMap Members

        public void Clear()
        {
            FailIfReadOnly();
            this.properties.Clear();
        }

        public bool Contains(object key)
        {
            return this.properties.Contains(key);
        }

        public void Remove(object key)
        {
            FailIfReadOnly();
            this.properties.Remove(key);
        }

        public int Count
        {
            get { return this.properties.Count; }
        }

        public System.Collections.ICollection Keys
        {
            get { return this.properties.Keys; }
        }

        public System.Collections.ICollection Values
        {
            get { return this.properties.Values; }
        }

        public object this[string key]
        {
            get { return GetObjectProperty(key); }
            set { SetObjectProperty(key, value); }
        }

        public string GetString(string key)
        {
            Object value = GetObjectProperty(key);
            
            if(value == null)
            {
                return null;
            }
            else if((value is IList) || (value is IDictionary))
            {
                throw new MessageFormatException(" cannot read a boolean from " + value.GetType().Name);
            }

            return value.ToString();
        }

        public void SetString(string key, string value)
        {
            SetObjectProperty(key, value);
        }

        public bool GetBool(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Boolean)
                {
                    return (bool) value;
                }
                else if(value is String)
                {
                    return ((string) value).ToLower() == "true";
                }
                else
                {
                    throw new MessageFormatException(" cannot read a boolean from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetBool(string key, bool value)
        {
            SetObjectProperty(key, value);
        }

        public byte GetByte(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Byte)
                {
                    return (byte) value;
                }
                else if(value is String)
                {
                    return Convert.ToByte(value);
                }
                else
                {
                    throw new MessageFormatException(" cannot read a byte from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetByte(string key, byte value)
        {
            SetObjectProperty(key, value);
        }

        public char GetChar(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Char)
                {
                    return (char) value;
                }
				else if(value is String)
				{
					string svalue = value as string;
					if(svalue.Length == 1)
					{
						return svalue.ToCharArray()[0];
					}
				}

				throw new MessageFormatException(" cannot read a char from " + value.GetType().Name);
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }            
        }

        public void SetChar(string key, char value)
        {
            SetObjectProperty(key, value);
        }

        public short GetShort(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Int16)
                {
                    return (short) value;
                }
                else if(value is Byte || value is String)
                {
                    return Convert.ToInt16(value);
                }
                else
                {
                    throw new MessageFormatException(" cannot read a short from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetShort(string key, short value)
        {
            SetObjectProperty(key, value);
        }

        public int GetInt(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Int32)
                {
                    return (int) value;
                }
                else if(value is Int16 || value is Byte || value is String)
                {
                    return Convert.ToInt32(value);
                }
                else
                {
                    throw new MessageFormatException(" cannot read a int from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetInt(string key, int value)
        {
            SetObjectProperty(key, value);
        }

        public long GetLong(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Int64)
                {
                    return (long) value;
                }
                else if(value is Int32 || value is Int16 || value is Byte || value is String)
                {
                    return Convert.ToInt64(value);
                }
                else
                {
                    throw new MessageFormatException(" cannot read a long from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetLong(string key, long value)
        {
            SetObjectProperty(key, value);
        }

        public float GetFloat(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Single)
                {
                    return (float) value;
                }
                else if(value is String)
                {
                    return Convert.ToSingle(value);
                }
                else
                {
                    throw new MessageFormatException(" cannot read a float from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetFloat(string key, float value)
        {
            SetObjectProperty(key, value);
        }

        public double GetDouble(string key)
        {
            Object value = GetObjectProperty(key);

            try
            {
                if(value is Double)
                {
                    return (double) value;
                }
                else if(value is Single || value is String)
                {
                    return Convert.ToDouble(value);
                }
                else
                {
                    throw new MessageFormatException(" cannot read a double from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
        }

        public void SetDouble(string key, double value)
        {
            SetObjectProperty(key, value);
        }

		public void SetBytes(String key, byte[] value) 
		{
			this.SetBytes(key, value, 0, value.Length);
		}
		
		public void SetBytes(String key, byte[] value, int offset, int length)
		{
			byte[] copy = new byte[length];
			Array.Copy(value, offset, copy, 0, length);
            SetObjectProperty(key, value);
		}
		
		public byte[] GetBytes(string key)
		{
            Object value = GetObjectProperty(key);
			
            try
            {
                if(value is Byte[])
                {
                    return (byte[]) value;
                }
                else
                {
                    throw new MessageFormatException(" cannot read a byte[] from " + value.GetType().Name);
                }
            }
            catch(FormatException ex)
            {
                throw NMSExceptionSupport.CreateMessageFormatException(ex);
            }
		}
		
        public System.Collections.IList GetList(string key)
        {
            return (System.Collections.IList) GetObjectProperty(key);
        }

        public void SetList(string key, System.Collections.IList list)
        {
            SetObjectProperty(key, list);
        }

        public System.Collections.IDictionary GetDictionary(string key)
        {
            return (System.Collections.IDictionary) GetObjectProperty(key);
        }

        public void SetDictionary(string key, System.Collections.IDictionary dictionary)
        {
            SetObjectProperty(key, dictionary);
        }

        #endregion

        public bool ReadOnly
        {
            get{ return this.readOnly; }
            set{ this.readOnly = value; }
        }

        public bool AllowByteArrays
        {
            get{ return this.allowByteArrays; }
            set{ this.allowByteArrays = value; }
        }
		
        protected virtual void FailIfReadOnly()
        {
            if(this.ReadOnly == true)
            {
                throw new MessageNotWriteableException("Properties are in Read-Only mode.");
            }
        }
    }
}
