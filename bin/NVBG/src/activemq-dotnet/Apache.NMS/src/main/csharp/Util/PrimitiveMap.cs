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
using System.IO;

namespace Apache.NMS.Util
{
	/// <summary>
	/// A default implementation of IPrimitiveMap
	/// </summary>
	public class PrimitiveMap : IPrimitiveMap
	{
		public const byte NULL = 0;
		public const byte BOOLEAN_TYPE = 1;
		public const byte BYTE_TYPE = 2;
		public const byte CHAR_TYPE = 3;
		public const byte SHORT_TYPE = 4;
		public const byte INTEGER_TYPE = 5;
		public const byte LONG_TYPE = 6;
		public const byte DOUBLE_TYPE = 7;
		public const byte FLOAT_TYPE = 8;
		public const byte STRING_TYPE = 9;
		public const byte BYTE_ARRAY_TYPE = 10;
		public const byte MAP_TYPE = 11;
		public const byte LIST_TYPE = 12;
		public const byte BIG_STRING_TYPE = 13;

		private IDictionary dictionary = Hashtable.Synchronized(new Hashtable());

		public void Clear()
		{
			dictionary.Clear();
		}

		public bool Contains(Object key)
		{
			return dictionary.Contains(key);
		}

		public void Remove(Object key)
		{
			dictionary.Remove(key);
		}

		public int Count
		{
			get { return dictionary.Count; }
		}

		public ICollection Keys
		{
			get { lock(dictionary.SyncRoot) return new ArrayList(dictionary.Keys); }
		}

		public ICollection Values
		{
			get { lock(dictionary.SyncRoot) return new ArrayList(dictionary.Values); }
		}

		public object this[string key]
		{
			get { return GetValue(key); }
			set
			{
				CheckValidType(value);
				SetValue(key, value);
			}
		}

		public string GetString(string key)
		{
			Object value = GetValue(key);
			if(value == null)
			{
				return null;
			}
			CheckValueType(value, typeof(string));
			return (string) value;
		}

		public void SetString(string key, string value)
		{
			SetValue(key, value);
		}

		public bool GetBool(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(bool));
			return (bool) value;
		}

		public void SetBool(String key, bool value)
		{
			SetValue(key, value);
		}

		public byte GetByte(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(byte));
			return (byte) value;
		}

		public void SetByte(String key, byte value)
		{
			SetValue(key, value);
		}

		public char GetChar(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(char));
			return (char) value;
		}

		public void SetChar(String key, char value)
		{
			SetValue(key, value);
		}

		public short GetShort(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(short));
			return (short) value;
		}

		public void SetShort(String key, short value)
		{
			SetValue(key, value);
		}

		public int GetInt(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(int));
			return (int) value;
		}

		public void SetInt(String key, int value)
		{
			SetValue(key, value);
		}

		public long GetLong(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(long));
			return (long) value;
		}

		public void SetLong(String key, long value)
		{
			SetValue(key, value);
		}

		public float GetFloat(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(float));
			return (float) value;
		}

		public void SetFloat(String key, float value)
		{
			SetValue(key, value);
		}

		public double GetDouble(String key)
		{
			Object value = GetValue(key);
			CheckValueType(value, typeof(double));
			return (double) value;
		}

		public void SetDouble(String key, double value)
		{
			SetValue(key, value);
		}

		public IList GetList(String key)
		{
			Object value = GetValue(key);
			if(value != null && !(value is IList))
			{
				throw new NMSException("Property: " + key + " is not an IList but is: " + value);
			}
			return (IList) value;
		}		
		
		public void SetList(String key, IList value)
		{
			SetValue(key, value);
		}

		public void SetBytes(String key, byte[] value) 
		{
			this.SetBytes(key, value, 0, value.Length);
		}
		
		public void SetBytes(String key, byte[] value, int offset, int length)
		{
			byte[] copy = new byte[length];
			Array.Copy(value, offset, copy, 0, length);
			SetValue(key, copy);
		}
		
		public byte[] GetBytes(string key)
		{
			Object value = GetValue(key);
			if(value != null && !(value is Byte[]))
			{
				throw new NMSException("Property: " + key + " is not an byte[] but is: " + value);
			}
			return (byte[]) value;
		}
		
		public IDictionary GetDictionary(String key)
		{
			Object value = GetValue(key);
			if(value != null && !(value is IDictionary))
			{
				throw new NMSException("Property: " + key + " is not an IDictionary but is: " + value);
			}
			return (IDictionary) value;
		}

		public void SetDictionary(String key, IDictionary value)
		{
			SetValue(key, value);
		}

		protected virtual void SetValue(String key, Object value)
		{
			dictionary[key] = value;
		}

		protected virtual Object GetValue(String key)
		{
			return dictionary[key];
		}

		protected virtual void CheckValueType(Object value, Type type)
		{
			if(!type.IsInstanceOfType(value))
			{
				throw new NMSException("Expected type: " + type.Name + " but was: " + value);
			}
		}

		protected virtual void CheckValidType(Object value)
		{
			if(value != null && !(value is IList) && !(value is IDictionary))
			{
				Type type = value.GetType();

				if(type.IsInstanceOfType(typeof(Object)) || 
                   (!type.IsPrimitive && !type.IsValueType && !type.IsAssignableFrom(typeof(string))))
				{
					throw new NMSException("Invalid type: " + type.Name + " for value: " + value);
				}
			}
		}

		/// <summary>
		/// Method ToString
		/// </summary>
		/// <returns>A string</returns>
		public override String ToString()
		{
			String s = "{";
			bool first = true;
			lock(dictionary.SyncRoot)
			{
				foreach(DictionaryEntry entry in dictionary)
				{
					if(!first)
					{
						s += ", ";
					}
					first = false;
					String name = (String) entry.Key;
					Object value = entry.Value;
					s += name + "=" + value;
				}
			}
			s += "}";
			return s;
		}

		/// <summary>
		/// Unmarshalls the map from the given data or if the data is null just
		/// return an empty map
		/// </summary>
		public static PrimitiveMap Unmarshal(byte[] data)
		{
			PrimitiveMap answer = new PrimitiveMap();
			answer.dictionary = UnmarshalPrimitiveMap(data);
			return answer;
		}

		/// <summary>
		/// Unmarshals a PrimitiveMap directly from a Stream object.  This 
		/// allows for clients to read PrimitiveMaps from Compressed or other
		/// wise encoded streams without this class needing to know about it.
		/// </summary>
		/// <param name="source">
		/// A <see cref="Stream"/>
		/// </param>
		/// <returns>
		/// A <see cref="PrimitiveMap"/>
		/// </returns>
		public static PrimitiveMap Unmarshal(Stream source)
		{
			PrimitiveMap answer = new PrimitiveMap();
			answer.dictionary = UnmarshalPrimitiveMap(source);
			return answer;
		}

		public byte[] Marshal()
		{
			lock(dictionary.SyncRoot)
			{
				return MarshalPrimitiveMap(dictionary);
			}
		}

		/// <summary>
		/// Marshals a PrimitiveMap directly to a Stream object.  This
		/// allows a client to write a PrimitiveMap in a compressed or 
		/// otherwise encoded form without this class needing to know 
		/// about it.
		/// </summary>
		/// <param name="destination">
		/// A <see cref="Stream"/>
		/// </param>
		public void Marshal(Stream destination)
		{
			lock(dictionary.SyncRoot)
			{
				MarshalPrimitiveMap(dictionary, destination);
			}
		}
				
		/// <summary>
		/// Marshals the primitive type map to a byte array
		/// </summary>
		public static byte[] MarshalPrimitiveMap(IDictionary map)
		{
			if(map == null)
			{
				return null;
			}

			MemoryStream memoryStream = new MemoryStream();
			lock(map.SyncRoot)
			{
				MarshalPrimitiveMap(map, new EndianBinaryWriter(memoryStream));
			}

			return memoryStream.ToArray();
		}

		public static void MarshalPrimitiveMap(IDictionary map, Stream stream)
		{
			if(map != null)
			{
				lock(map.SyncRoot)
				{
					MarshalPrimitiveMap(map, new EndianBinaryWriter(stream));
				}
			}
		}		
		
		public static void MarshalPrimitiveMap(IDictionary map, BinaryWriter dataOut)
		{
			if(map == null)
			{
				dataOut.Write((int) -1);
			}
			else
			{
				lock(map.SyncRoot)
				{
					dataOut.Write(map.Count);
					foreach(DictionaryEntry entry in map)
					{
						String name = (String) entry.Key;
						dataOut.Write(name);
						Object value = entry.Value;
						MarshalPrimitive(dataOut, value);
					}
				}
			}
		}

		/// <summary>
		/// Unmarshals the primitive type map from the given byte array
		/// </summary>
		public static IDictionary UnmarshalPrimitiveMap(byte[] data)
		{
			if(data == null)
			{
				return new Hashtable();
			}
			else
			{
				return UnmarshalPrimitiveMap(new EndianBinaryReader(new MemoryStream(data)));
			}
		}
		
		public static IDictionary UnmarshalPrimitiveMap(Stream source)
		{
			return UnmarshalPrimitiveMap(new EndianBinaryReader(source));
		}		

		public static IDictionary UnmarshalPrimitiveMap(BinaryReader dataIn)
		{
			int size = dataIn.ReadInt32();
			if(size < 0)
			{
				return null;
			}

			IDictionary answer = new Hashtable(size);
			for(int i = 0; i < size; i++)
			{
				String name = dataIn.ReadString();
				answer[name] = UnmarshalPrimitive(dataIn);
			}

			return answer;
		}

		public static void MarshalPrimitiveList(IList list, BinaryWriter dataOut)
		{
			dataOut.Write((int) list.Count);
			foreach(Object element in list)
			{
				MarshalPrimitive(dataOut, element);
			}
		}

		public static IList UnmarshalPrimitiveList(BinaryReader dataIn)
		{
			int size = dataIn.ReadInt32();
			IList answer = new ArrayList(size);
			while(size-- > 0)
			{
				answer.Add(UnmarshalPrimitive(dataIn));
			}

			return answer;
		}

		public static void MarshalPrimitive(BinaryWriter dataOut, Object value)
		{
			if(value == null)
			{
				dataOut.Write(NULL);
			}
			else if(value is bool)
			{
				dataOut.Write(BOOLEAN_TYPE);
				dataOut.Write((bool) value);
			}
			else if(value is byte)
			{
				dataOut.Write(BYTE_TYPE);
				dataOut.Write(((byte) value));
			}
			else if(value is char)
			{
				dataOut.Write(CHAR_TYPE);
				dataOut.Write((char) value);
			}
			else if(value is short)
			{
				dataOut.Write(SHORT_TYPE);
				dataOut.Write((short) value);
			}
			else if(value is int)
			{
				dataOut.Write(INTEGER_TYPE);
				dataOut.Write((int) value);
			}
			else if(value is long)
			{
				dataOut.Write(LONG_TYPE);
				dataOut.Write((long) value);
			}
			else if(value is float)
			{
				dataOut.Write(FLOAT_TYPE);
				dataOut.Write((float) value);
			}
			else if(value is double)
			{
				dataOut.Write(DOUBLE_TYPE);
				dataOut.Write((double) value);
			}
			else if(value is byte[])
			{
				byte[] data = (byte[]) value;
				dataOut.Write(BYTE_ARRAY_TYPE);
				dataOut.Write(data.Length);
				dataOut.Write(data);
			}
			else if(value is string)
			{
				string s = (string) value;
				// is the string big??
				if(s.Length > 8191)
				{
					dataOut.Write(BIG_STRING_TYPE);
					((EndianBinaryWriter) dataOut).WriteString32(s);
				}
				else
				{
					dataOut.Write(STRING_TYPE);
					((EndianBinaryWriter) dataOut).WriteString16(s);
				}
			}
			else if(value is IDictionary)
			{
				dataOut.Write(MAP_TYPE);
				MarshalPrimitiveMap((IDictionary) value, dataOut);
			}
			else if(value is IList)
			{
				dataOut.Write(LIST_TYPE);
				MarshalPrimitiveList((IList) value, dataOut);
			}
			else
			{
				throw new IOException("Object is not a primitive: " + value);
			}
		}

		public static Object UnmarshalPrimitive(BinaryReader dataIn)
		{
			Object value = null;
			byte type = dataIn.ReadByte();
			switch(type)
			{
			case NULL:
				value = null;
				break;
			case BYTE_TYPE:
				value = dataIn.ReadByte();
				break;
			case BOOLEAN_TYPE:
				value = dataIn.ReadBoolean();
				break;
			case CHAR_TYPE:
				value = dataIn.ReadChar();
				break;
			case SHORT_TYPE:
				value = dataIn.ReadInt16();
				break;
			case INTEGER_TYPE:
				value = dataIn.ReadInt32();
				break;
			case LONG_TYPE:
				value = dataIn.ReadInt64();
				break;
			case FLOAT_TYPE:
				value = dataIn.ReadSingle();
				break;
			case DOUBLE_TYPE:
				value = dataIn.ReadDouble();
				break;
			case BYTE_ARRAY_TYPE:
				int size = dataIn.ReadInt32();
				byte[] data = new byte[size];
				dataIn.Read(data, 0, size);
				value = data;
				break;
			case STRING_TYPE:
				value = ((EndianBinaryReader) dataIn).ReadString16();
				break;
			case BIG_STRING_TYPE:
				value = ((EndianBinaryReader) dataIn).ReadString32();
				break;
			case MAP_TYPE:
				value = UnmarshalPrimitiveMap(dataIn);
				break;
			case LIST_TYPE:
				value = UnmarshalPrimitiveList(dataIn);
				break;

			default:
				throw new Exception("Unsupported data type: " + type);
			}
			return value;
		}
	}
}
