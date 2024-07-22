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
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Commands
{
	public class ActiveMQStreamMessage : ActiveMQMessage, IStreamMessage
	{
		private EndianBinaryReader dataIn = null;
		private EndianBinaryWriter dataOut = null;
		private MemoryStream byteBuffer = null;
		private int bytesRemaining = -1;

		public const byte ID_ACTIVEMQSTREAMMESSAGE = 27;

		public override byte GetDataStructureType()
		{
			return ID_ACTIVEMQSTREAMMESSAGE;
		}

		public bool ReadBoolean()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.BOOLEAN_TYPE:
				            return this.dataIn.ReadBoolean();
				        case PrimitiveMap.STRING_TYPE:
				            return Boolean.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a bool");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Boolean type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public byte ReadByte()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.BYTE_TYPE:
				            return this.dataIn.ReadByte();
				        case PrimitiveMap.STRING_TYPE:
				            return Byte.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a byte");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Byte type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public char ReadChar()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.CHAR_TYPE:
				            return this.dataIn.ReadChar();
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a char");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Char type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public short ReadInt16()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.SHORT_TYPE:
				            return this.dataIn.ReadInt16();
				        case PrimitiveMap.BYTE_TYPE:
				            return this.dataIn.ReadByte();
				        case PrimitiveMap.STRING_TYPE:
				            return Int16.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a short");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Int16 type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public int ReadInt32()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.INTEGER_TYPE:
				            return this.dataIn.ReadInt32();
				        case PrimitiveMap.SHORT_TYPE:
				            return this.dataIn.ReadInt16();
				        case PrimitiveMap.BYTE_TYPE:
				            return this.dataIn.ReadByte();
				        case PrimitiveMap.STRING_TYPE:
				            return Int32.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a int");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Int32 type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public long ReadInt64()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.LONG_TYPE:
				            return this.dataIn.ReadInt64();
				        case PrimitiveMap.INTEGER_TYPE:
				            return this.dataIn.ReadInt32();
				        case PrimitiveMap.SHORT_TYPE:
				            return this.dataIn.ReadInt16();
				        case PrimitiveMap.BYTE_TYPE:
				            return this.dataIn.ReadByte();
				        case PrimitiveMap.STRING_TYPE:
				            return Int64.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a long");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Int64 type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public float ReadSingle()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.FLOAT_TYPE:
				            return this.dataIn.ReadSingle();
				        case PrimitiveMap.STRING_TYPE:
				            return Single.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a float");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Single type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public double ReadDouble()
		{
			InitializeReading();

			try
			{
				long startingPos = this.byteBuffer.Position;
				try
				{
				    int type = this.dataIn.ReadByte();

				    switch (type)
				    {
				        case PrimitiveMap.DOUBLE_TYPE:
				            return this.dataIn.ReadDouble();
				        case PrimitiveMap.FLOAT_TYPE:
				            return this.dataIn.ReadSingle();
				        case PrimitiveMap.STRING_TYPE:
				            return Single.Parse(this.dataIn.ReadString16());
				        case PrimitiveMap.NULL:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new NMSException("Cannot convert Null type to a double");
				        default:
				            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				            throw new MessageFormatException("Value is not a Double type.");
				    }
				}
				catch(FormatException e)
				{
					this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
					throw NMSExceptionSupport.CreateMessageFormatException(e);
				}
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public string ReadString()
		{
			InitializeReading();

			long startingPos = this.byteBuffer.Position;

			try
			{
			    int type = this.dataIn.ReadByte();

			    switch (type)
			    {
			        case PrimitiveMap.BIG_STRING_TYPE:
			            return this.dataIn.ReadString32();
			        case PrimitiveMap.STRING_TYPE:
			            return this.dataIn.ReadString16();
			        case PrimitiveMap.LONG_TYPE:
			            return this.dataIn.ReadInt64().ToString();
			        case PrimitiveMap.INTEGER_TYPE:
			            return this.dataIn.ReadInt32().ToString();
			        case PrimitiveMap.SHORT_TYPE:
			            return this.dataIn.ReadInt16().ToString();
			        case PrimitiveMap.FLOAT_TYPE:
			            return this.dataIn.ReadSingle().ToString();
			        case PrimitiveMap.DOUBLE_TYPE:
			            return this.dataIn.ReadDouble().ToString();
			        case PrimitiveMap.CHAR_TYPE:
			            return this.dataIn.ReadChar().ToString();
			        case PrimitiveMap.BYTE_TYPE:
			            return this.dataIn.ReadByte().ToString();
			        case PrimitiveMap.BOOLEAN_TYPE:
			            return this.dataIn.ReadBoolean().ToString();
			        case PrimitiveMap.NULL:
			            return null;
			        default:
			            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
			            throw new MessageFormatException("Value is not a known type.");
			    }
			}
			catch(FormatException e)
			{
				this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public int ReadBytes(byte[] value)
		{
			InitializeReading();

			if(value == null)
			{
				throw new NullReferenceException("Passed Byte Array is null");
			}

			try
			{
				if(this.bytesRemaining == -1)
				{
					long startingPos = this.byteBuffer.Position;
					byte type = this.dataIn.ReadByte();

					if(type != PrimitiveMap.BYTE_ARRAY_TYPE)
					{
						this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
						throw new MessageFormatException("Not a byte array");
					}

					this.bytesRemaining = this.dataIn.ReadInt32();
				}
				else if(this.bytesRemaining == 0)
				{
					this.bytesRemaining = -1;
					return -1;
				}

				if(value.Length <= this.bytesRemaining)
				{
					// small buffer
					this.bytesRemaining -= value.Length;
					this.dataIn.Read(value, 0, value.Length);
					return value.Length;
				}
				else
				{
					// big buffer
					int rc = this.dataIn.Read(value, 0, this.bytesRemaining);
					this.bytesRemaining = 0;
					return rc;
				}
			}
			catch(EndOfStreamException ex)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(ex);
			}
			catch(IOException ex)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(ex);
			}
		}

		public Object ReadObject()
		{
			InitializeReading();

			long startingPos = this.byteBuffer.Position;

			try
			{
			    int type = this.dataIn.ReadByte();

			    switch (type)
			    {
			        case PrimitiveMap.BIG_STRING_TYPE:
			            return this.dataIn.ReadString32();
			        case PrimitiveMap.STRING_TYPE:
			            return this.dataIn.ReadString16();
			        case PrimitiveMap.LONG_TYPE:
			            return this.dataIn.ReadInt64();
			        case PrimitiveMap.INTEGER_TYPE:
			            return this.dataIn.ReadInt32();
			        case PrimitiveMap.SHORT_TYPE:
			            return this.dataIn.ReadInt16();
			        case PrimitiveMap.FLOAT_TYPE:
			            return this.dataIn.ReadSingle();
			        case PrimitiveMap.DOUBLE_TYPE:
			            return this.dataIn.ReadDouble();
			        case PrimitiveMap.CHAR_TYPE:
			            return this.dataIn.ReadChar();
			        case PrimitiveMap.BYTE_TYPE:
			            return this.dataIn.ReadByte();
			        case PrimitiveMap.BOOLEAN_TYPE:
			            return this.dataIn.ReadBoolean();
			        case PrimitiveMap.BYTE_ARRAY_TYPE:
			            {
			                int length = this.dataIn.ReadInt32();
			                byte[] data = new byte[length];
			                this.dataIn.Read(data, 0, length);
			                return data;
			            }
			        case PrimitiveMap.NULL:
			            return null;
			        default:
			            this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
			            throw new MessageFormatException("Value is not a known type.");
			    }
			}
			catch(FormatException e)
			{
				this.byteBuffer.Seek(startingPos, SeekOrigin.Begin);
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
			catch(EndOfStreamException e)
			{
				throw NMSExceptionSupport.CreateMessageEOFException(e);
			}
			catch(IOException e)
			{
				throw NMSExceptionSupport.CreateMessageFormatException(e);
			}
		}

		public void WriteBoolean(bool value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.BOOLEAN_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteByte(byte value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.BYTE_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteBytes(byte[] value)
		{
			InitializeWriting();
			this.WriteBytes(value, 0, value.Length);
		}

		public void WriteBytes(byte[] value, int offset, int length)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.BYTE_ARRAY_TYPE);
				this.dataOut.Write((int) length);
				this.dataOut.Write(value, offset, length);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteChar(char value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.CHAR_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteInt16(short value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.SHORT_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteInt32(int value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.INTEGER_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteInt64(long value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.LONG_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteSingle(float value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.FLOAT_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteDouble(double value)
		{
			InitializeWriting();
			try
			{
				this.dataOut.Write(PrimitiveMap.DOUBLE_TYPE);
				this.dataOut.Write(value);
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteString(string value)
		{
			InitializeWriting();
			try
			{
				if( value.Length > 8192 )
				{
					this.dataOut.Write(PrimitiveMap.BIG_STRING_TYPE);
					this.dataOut.WriteString32(value);
				}
				else
				{
					this.dataOut.Write(PrimitiveMap.STRING_TYPE);
					this.dataOut.WriteString16(value);
				}
			}
			catch(IOException e)
			{
				NMSExceptionSupport.Create(e);
			}
		}

		public void WriteObject(Object value)
		{
			InitializeWriting();
			if( value is System.Byte )
			{
				this.WriteByte( (byte) value );
			}
			else if( value is Char )
			{
				this.WriteChar( (char) value );
			}
			else if( value is Boolean )
			{
				this.WriteBoolean( (bool) value );
			}
			else if( value is Int16 )
			{
				this.WriteInt16( (short) value );
			}
			else if( value is Int32 )
			{
				this.WriteInt32( (int) value );
			}
			else if( value is Int64 )
			{
				this.WriteInt64( (long) value );
			}
			else if( value is Single )
			{
				this.WriteSingle( (float) value );
			}
			else if( value is Double )
			{
				this.WriteDouble( (double) value );
			}
			else if( value is byte[] )
			{
				this.WriteBytes( (byte[]) value );
			}
			else if( value is String )
			{
				this.WriteString( (string) value );
			}
			else
			{
				throw new MessageFormatException("Cannot write non-primitive type:" + value.GetType());
			}
		}

		public override Object Clone()
		{
			StoreContent();
			return base.Clone();
		}

		public override void OnSend()
		{
			base.OnSend();
			StoreContent();
		}

		public override void ClearBody()
		{
			base.ClearBody();
			this.byteBuffer = null;
			this.dataIn = null;
			this.dataOut = null;
			this.bytesRemaining = -1;
		}

		public void Reset()
		{
			StoreContent();
			this.dataIn = null;
			this.dataOut = null;
			this.byteBuffer = null;
			this.bytesRemaining = -1;
			this.ReadOnlyBody = true;
		}

		private void InitializeReading()
		{
			FailIfWriteOnlyBody();
			if(this.dataIn == null)
			{
				this.byteBuffer = new MemoryStream(this.Content, false);

                Stream target = this.byteBuffer;
                if(this.Connection != null && this.Compressed == true)
                {
                    target = this.Connection.CompressionPolicy.CreateDecompressionStream(target);
                }
                
				this.dataIn = new EndianBinaryReader(target);
			}
		}

		private void InitializeWriting()
		{
			FailIfReadOnlyBody();
			if(this.dataOut == null)
			{
                this.byteBuffer = new MemoryStream();
                Stream target = this.byteBuffer;
                
                if(this.Connection != null && this.Connection.UseCompression)
                {
                    target = this.Connection.CompressionPolicy.CreateCompressionStream(target);
					this.Compressed = true;
                }

				this.dataOut = new EndianBinaryWriter(target);
			}
		}

		private void StoreContent()
		{
			if( dataOut != null)
			{
				dataOut.Close();

				this.Content = byteBuffer.ToArray();
				this.dataOut = null;
				this.byteBuffer = null;
			}
		}
	}
}

