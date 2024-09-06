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
	/// A BinaryWriter that switches the endian orientation of the read operations so that they
	/// are compatible across platforms.
	/// </summary>
	[CLSCompliant(false)]
	public class EndianBinaryReader : BinaryReader
	{
		public EndianBinaryReader(Stream input)
			: base(input)
		{
		}

		/// <summary>
		/// Method Read
		/// </summary>
		/// <returns>An int</returns>
		/// <param name="buffer">A  char[]</param>
		/// <param name="index">An int</param>
		/// <param name="count">An int</param>
		public override int Read(char[] buffer, int index, int count)
		{
			int size = base.Read(buffer, index, count);
			for(int i = 0; i < size; i++)
			{
				buffer[index + i] = EndianSupport.SwitchEndian(buffer[index + i]);
			}
			return size;
		}

		/// <summary>
		/// Method ReadChars
		/// </summary>
		/// <returns>A char[]</returns>
		/// <param name="count">An int</param>
		public override char[] ReadChars(int count)
		{
			char[] rc = base.ReadChars(count);
			if(rc != null)
			{
				for(int i = 0; i < rc.Length; i++)
				{
					rc[i] = EndianSupport.SwitchEndian(rc[i]);
				}
			}
			return rc;
		}

		/// <summary>
		/// Method ReadInt16
		/// </summary>
		/// <returns>A short</returns>
		public override short ReadInt16()
		{
			return EndianSupport.SwitchEndian(base.ReadInt16());
		}

		/// <summary>
		/// Method ReadChar
		/// </summary>
		/// <returns>A char</returns>
		public override char ReadChar()
		{
			return (char) (
				(((char) ((byte) (base.ReadByte()))) << 8) |
				(((char) ((byte) (base.ReadByte()))))
				);

			//			return EndianSupport.SwitchEndian(base.ReadChar());
		}

		/// <summary>
		/// Method ReadInt64
		/// </summary>
		/// <returns>A long</returns>
		public override long ReadInt64()
		{
			return EndianSupport.SwitchEndian(base.ReadInt64());
		}

		/// <summary>
		/// Method ReadUInt64
		/// </summary>
		/// <returns>An ulong</returns>
		public override ulong ReadUInt64()
		{
			return EndianSupport.SwitchEndian(base.ReadUInt64());
		}

		/// <summary>
		/// Method ReadUInt32
		/// </summary>
		/// <returns>An uint</returns>
		public override uint ReadUInt32()
		{
			return EndianSupport.SwitchEndian(base.ReadUInt32());
		}

		/// <summary>
		/// Method ReadUInt16
		/// </summary>
		/// <returns>An ushort</returns>
		public override ushort ReadUInt16()
		{
			return EndianSupport.SwitchEndian(base.ReadUInt16());
		}

		/// <summary>
		/// Method ReadInt32
		/// </summary>
		/// <returns>An int</returns>
		public override int ReadInt32()
		{
			int x = base.ReadInt32();
			int y = EndianSupport.SwitchEndian(x);
			return y;
		}

		/// <summary>
		/// Method ReadString
		/// </summary>
		/// <returns>A string</returns>
		public override String ReadString()
		{
            return ReadString16();
		}

		/// <summary>
		/// Method ReadString16, reads a String value encoded in the Java modified
		/// UTF-8 format with a length index encoded as a 16bit unsigned short.
		/// </summary>
		/// <returns>A string</returns>
		public String ReadString16()
		{
			int utfLength = ReadUInt16();

			if(utfLength < 0)
			{
				return null;
			} 
            else if(utfLength == 0)
            {
                return "";
            }

            return doReadString(utfLength);
		}

		/// <summary>
		/// Method ReadString32, reads a String value encoded in the Java modified 
		/// UTF-8 format with a length index encoded as a singed integer value.
		/// </summary>
		/// <returns>A string</returns>
		public String ReadString32()
		{
			int utfLength = ReadInt32();

            if(utfLength < 0)
            {
                return null;
            } 
            else if(utfLength == 0)
            {
                return "";
            }

            return doReadString(utfLength);
		}

        private string doReadString(int utfLength)
        {
            char[] result = new char[utfLength];
            byte[] buffer = new byte[utfLength];

            int bytesRead = 0;
            while(bytesRead < utfLength)
            {
                int rc = Read(buffer, bytesRead, utfLength - bytesRead);
                if(rc == 0)
                {
                    throw new IOException("premature end of stream");
                }

                bytesRead += rc;
            }

            int count = 0;
            int index = 0;
            byte a = 0;

            while(count < utfLength)
            {
                if((result[index] = (char) buffer[count++]) < 0x80)
                {
                    index++;
                }
                else if(((a = (byte) result[index]) & 0xE0) == 0xC0)
                {
                    if(count >= utfLength)
                    {
                        throw new IOException("Invalid UTF-8 encoding found, start of two byte char found at end.");
                    }

                    byte b = buffer[count++];
                    if((b & 0xC0) != 0x80)
                    {
                        throw new IOException("Invalid UTF-8 encoding found, byte two does not start with 0x80.");
                    }

                    result[index++] = (char) (((a & 0x1F) << 6) | (b & 0x3F));
                }
                else if((a & 0xF0) == 0xE0)
                {

                    if(count + 1 >= utfLength)
                    {
                        throw new IOException("Invalid UTF-8 encoding found, start of three byte char found at end.");
                    }

                    byte b = buffer[count++];
                    byte c = buffer[count++];
                    if(((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80))
                    {
                        throw new IOException("Invalid UTF-8 encoding found, byte two does not start with 0x80.");
                    }

                    result[index++] = (char) (((a & 0x0F) << 12) |
                                              ((b & 0x3F) << 6) | (c & 0x3F));
                }
                else
                {
                    throw new IOException("Invalid UTF-8 encoding found, aborting.");
                }
            }

            return new String(result, 0, index);            
        }

		public override float ReadSingle()
		{
			return EndianSupport.SwitchEndian(base.ReadSingle());
		}

		public override double ReadDouble()
		{
			return EndianSupport.SwitchEndian(base.ReadDouble());
		}

		protected static Exception CreateDataFormatException()
		{
			// TODO: implement a better exception
			return new IOException("Data format error!");
		}
	}
}
