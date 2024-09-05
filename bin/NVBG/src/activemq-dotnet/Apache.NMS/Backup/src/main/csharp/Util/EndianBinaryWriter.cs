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
using System.IO;

namespace Apache.NMS.Util
{
	/// <summary>
	/// A BinaryWriter that switches the endian orientation of the write operations so that they
	/// are compatible across platforms.
	/// </summary>
	[CLSCompliant(false)]
	public class EndianBinaryWriter : BinaryWriter
	{
		public const int MAXSTRINGLEN = short.MaxValue;

		public EndianBinaryWriter(Stream output)
			: base(output)
		{
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">A  long</param>
		public override void Write(long value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">An ushort</param>
		public override void Write(ushort value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">An int</param>
		public override void Write(int value)
		{
			int x = EndianSupport.SwitchEndian(value);
			base.Write(x);
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="chars">A  char[]</param>
		/// <param name="index">An int</param>
		/// <param name="count">An int</param>
		public override void Write(char[] chars, int index, int count)
		{
			char[] t = new char[count];
			for(int i = 0; i < count; i++)
			{
				t[index + i] = EndianSupport.SwitchEndian(t[index + i]);
			}
			base.Write(t);
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="chars">A  char[]</param>
		public override void Write(char[] chars)
		{
			Write(chars, 0, chars.Length);
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">An uint</param>
		public override void Write(uint value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="ch">A  char</param>
		public override void Write(char ch)
		{
			base.Write((byte) ((ch >> 8) & 0xFF));
			base.Write((byte) (ch & 0xFF));
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">An ulong</param>
		public override void Write(ulong value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">A  short</param>
		public override void Write(short value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		/// <summary>
		/// Method Write, writes a string to the output using the WriteString16
		/// method.
		/// </summary>
		/// <param name="text">A  string</param>
		public override void Write(String text)
		{
			WriteString16(text);
		}

		/// <summary>
		/// Method WriteString16, writes a string to the output using the Java 
		/// standard modified UTF-8 encoding with an unsigned short value written first to 
		/// indicate the length of the encoded data, the short is read as an unsigned 
		/// value so the max amount of data this method can write is 65535 encoded bytes.
		/// 
		/// Unlike the WriteString32 method this method does not encode the length
		/// value to -1 if the string is null, this is to match the behaviour of 
		/// the Java DataOuputStream class's writeUTF method.	
		/// 
		/// Because modified UTF-8 encding can result in a number of bytes greater that 
		/// the size of the String this method must first check that the encoding proces 
		/// will not result in a value that cannot be written becuase it is greater than
		/// the max value of an unsigned short.
		/// </summary>
		/// <param name="text">A  string</param>
		public void WriteString16(String text)
		{
			if(text != null)
			{
				if(text.Length > ushort.MaxValue)
				{
					throw new IOException(
						String.Format(
							"Cannot marshall string longer than: {0} characters, supplied string was: " +
							"{1} characters", ushort.MaxValue, text.Length));
				}

				char[] charr = text.ToCharArray();
				uint utfLength = CountUtf8Bytes(charr);

				if(utfLength > ushort.MaxValue)
				{
					throw new IOException(
						String.Format(
							"Cannot marshall an encoded string longer than: {0} bytes, supplied" +
							"string requires: {1} characters to encode", ushort.MaxValue, utfLength));
				}

				byte[] bytearr = new byte[utfLength];
				encodeUTF8toBuffer(charr, bytearr);

				Write((ushort) utfLength);
				Write(bytearr);
			}
		}

		/// <summary>
		/// Method WriteString32, writes a string to the output using the Openwire 
		/// standard modified UTF-8 encoding which an int value written first to 
		/// indicate the length of the encoded data, the int is read as an signed 
		/// value so the max amount of data this method can write is 2^31 encoded bytes.
		/// 
		/// In the case of a null value being passed this method writes a -1 to the 
		/// stream to indicate that the string is null.
		/// 
		/// Because modified UTF-8 encding can result in a number of bytes greater that 
		/// the size of the String this method must first check that the encoding proces 
		/// will not result in a value that cannot be written becuase it is greater than
		/// the max value of an int.
		/// </summary>
		/// <param name="text">A  string</param>
		public void WriteString32(String text)
		{
			if(text != null)
			{
				char[] charr = text.ToCharArray();
				uint utfLength = CountUtf8Bytes(charr);

				if(utfLength > int.MaxValue)
				{
					throw new IOException(
						String.Format(
							"Cannot marshall an encoded string longer than: {0} bytes, supplied" +
							"string requires: {1} characters to encode", int.MaxValue, utfLength));
				}

				byte[] bytearr = new byte[utfLength];
				encodeUTF8toBuffer(charr, bytearr);

				Write(utfLength);
				Write(bytearr);
			}
			else
			{
				Write((int) -1);
			}
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">A  double</param>
		public override void Write(float value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		/// <summary>
		/// Method Write
		/// </summary>
		/// <param name="value">A  double</param>
		public override void Write(double value)
		{
			base.Write(EndianSupport.SwitchEndian(value));
		}

		private static uint CountUtf8Bytes(char[] chars)
		{
			uint utfLength = 0;
			int c = 0;

			for(int i = 0; i < chars.Length; i++)
			{
				c = chars[i];
				if((c >= 0x0001) && (c <= 0x007F))
				{
					utfLength++;
				}
				else if(c > 0x07FF)
				{
					utfLength += 3;
				}
				else
				{
					utfLength += 2;
				}
			}

			return utfLength;
		}

		private static void encodeUTF8toBuffer(char[] chars, byte[] buffer)
		{
			int c = 0;
			int count = 0;

			for(int i = 0; i < chars.Length; i++)
			{
				c = chars[i];
				if((c >= 0x0001) && (c <= 0x007F))
				{
					buffer[count++] = (byte) c;
				}
				else if(c > 0x07FF)
				{
					buffer[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
					buffer[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
					buffer[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
				}
				else
				{
					buffer[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
					buffer[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
				}
			}
		}
	}
}
