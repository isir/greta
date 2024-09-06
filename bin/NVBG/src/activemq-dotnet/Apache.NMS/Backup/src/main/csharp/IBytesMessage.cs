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
namespace Apache.NMS
{
	/// <summary>
	///
	/// A BytesMessage object is used to send a message containing a stream of uninterpreted
	/// bytes. It inherits from the Message interface and adds a bytes message body. The
	/// receiver of the message supplies the interpretation of the bytes.
	///
	/// This message type is for client encoding of existing message formats. If possible,
	/// one of the other self-defining message types should be used instead.
	///
	/// Although the NMS API allows the use of message properties with byte messages, they
	/// are typically not used, since the inclusion of properties may affect the format.
	///
	/// When the message is first created, and when ClearBody is called, the body of the
	/// message is in write-only mode. After the first call to Reset has been made, the
	/// message body is in read-only mode. After a message has been sent, the client that
	/// sent it can retain and modify it without affecting the message that has been sent.
	/// The same message object can be sent multiple times. When a message has been received,
	/// the provider has called Reset so that the message body is in read-only mode for the
	/// client.
	///
	/// If ClearBody is called on a message in read-only mode, the message body is cleared and
	/// the message is in write-only mode.
	///
	/// If a client attempts to read a message in write-only mode, a MessageNotReadableException
	/// is thrown.
	///
	/// If a client attempts to write a message in read-only mode, a MessageNotWriteableException
	/// is thrown.
	/// </summary>
	public interface IBytesMessage : IMessage
	{
		byte[] Content { get; set; }

		/// <value>
		/// Gets the number of bytes of the message body when the message is in read-only mode.
		/// The value returned can be used to allocate a byte array. The value returned is the
		/// entire length of the message body, regardless of where the pointer for reading the
		/// message is currently located.
		/// </value>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		long BodyLength { get; }

		/// <summary>
		/// Reads a byte from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Byte"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		byte ReadByte();

		/// <summary>
		/// Writes a byte to the Message stream.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Byte"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteByte( byte value );

		/// <summary>
		/// Reads a boolean from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Boolean"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		bool ReadBoolean();

		/// <summary>
		/// Write a one byte value to the message stream representing the boolean
		/// value passed.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Boolean"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteBoolean( bool value );

		/// <summary>
		/// Reads a char from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Char"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		char ReadChar();

		/// <summary>
		/// Write a two byte value to the message stream representing the character
		/// value passed.  High byte first.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Char"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteChar( char value );

		/// <summary>
		/// Reads a Short from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Int16"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		short ReadInt16();

		/// <summary>
		/// Write a two byte value to the message stream representing the short
		/// value passed.  High byte first.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Int16"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteInt16( short value );

		/// <summary>
		/// Reads an int from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Int32"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		int ReadInt32();

		/// <summary>
		/// Write a four byte value to the message stream representing the integer
		/// value passed.  High byte first.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Int32"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteInt32( int value );

		/// <summary>
		/// Reads a long from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Int64"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		long ReadInt64();

		/// <summary>
		/// Write a eight byte value to the message stream representing the long
		/// value passed.  High byte first.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Int64"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteInt64( long value );

		/// <summary>
		/// Reads a float from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Single"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		float ReadSingle();

		/// <summary>
		/// Write a four byte value to the message stream representing the float
		/// value passed.  High byte first.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Single"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteSingle( float value );

		/// <summary>
		/// Reads an double from the Message Stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.Double"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		double ReadDouble();

		/// <summary>
		/// Write a eight byte value to the message stream representing the double
		/// value passed.  High byte first.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Double"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteDouble( double value );

		/// <summary>
		/// Reads a byte array from the bytes message stream.
		///
		/// If the length of array value is less than the number of bytes remaining to
		/// be read from the stream, the array should be filled. A subsequent call reads
		/// the next increment, and so on.
		///
		/// If the number of bytes remaining in the stream is less than the length of array
		/// value, the bytes should be read into the array. The return value of the total number
		/// of bytes read will be less than the length of the array, indicating that there are
		/// no more bytes left to be read from the stream. The next read of the stream returns -1.
		/// </summary>
		/// <param name="value">
		/// The byte array that will be used as a buffer to read into.
		/// </param>
		/// <returns>
		/// A <see cref="System.Int32"/>
		/// The number of bytes read into the passed byte array, or -1 if there are no more
		/// bytes left to be read from the stream.
		/// </returns>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		int ReadBytes( byte[] value );

		/// <summary>
		/// Reads a portion of the bytes message stream.
		///
		/// If the length of array value is less than the number of bytes remaining to be
		/// read from the stream, the array should be filled. A subsequent call reads the
		/// next increment, and so on.
		///
		/// If the number of bytes remaining in the stream is less than the length of array
		/// value, the bytes should be read into the array. The return value of the total
		/// number of bytes read will be less than the length of the array, indicating that
		/// there are no more bytes left to be read from the stream. The next read of the
		/// stream returns -1.
		///
		/// If length is negative, or length is greater than the length of the array value,
		/// then an Exception is thrown. No bytes will be read from the stream for this
		/// exception case.
		/// </summary>
		/// <param name="value">
		/// The byte array that will be used as a buffer to read into.
		/// </param>
		/// <param name="length">
		/// The amount of bytes to read into the buffer.
		/// </param>
		/// <returns>
		/// A <see cref="System.Int32"/>
		/// The number of bytes read into the passed byte array, or -1 if there are no more
		/// bytes left to be read from the stream.
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		int ReadBytes( byte[] value, int length );

		/// <summary>
		/// Writes a byte array to the bytes message stream.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Byte"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteBytes( byte[] value );

		/// <summary>
		/// Writes a portion of a byte array to the bytes message stream.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Byte"/>
		/// </param>
		/// <param name="offset">
		/// A <see cref="System.Int32"/>
		/// </param>
		/// <param name="length">
		/// A <see cref="System.Int32"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteBytes( byte[] value, int offset, int length );

		/// <summary>
		/// Reads a string that has been encoded using a modified UTF-8 format from the bytes
		/// message stream.
		/// </summary>
		/// <returns>
		/// A <see cref="System.String"/>
		/// </returns>
		/// <exception cref="Apache.NMS.MessageNotReadableException">
		/// Thrown when the Message is in write-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageEOFException">
		/// Thrown when an unexpected end of bytes has been reached.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		string ReadString();

		/// <summary>
		/// Writes a string to the bytes message stream using UTF-8 encoding in a
		/// machine-independent manner.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.String"/>
		/// </param>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteString( string value );

		/// <summary>
		/// Writes an object to the bytes message stream.
		///
		/// This method works only for the objectified primitive object types
		/// (Int32, Double, Boolean ...), String objects, and byte arrays.
		/// </summary>
		/// <param name="value">
		/// A <see cref="System.Object"/>
		/// the object in the .NET programming language to be written; it must not be null
		/// </param>
		/// <exception cref="Apache.NMS.MessageFormatException">
		/// Thrown when the Message has an invalid format.
		/// </exception>
		/// <exception cref="Apache.NMS.MessageNotWriteableException">
		/// Thrown when the Message is in read-only mode.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void WriteObject( System.Object value );

		/// <summary>
		/// Puts the message body in read-only mode and repositions the stream of bytes to the beginning.
		/// </summary>
		/// <exception cref="Apache.NMS.MessageFormatException">
		/// Thrown when the Message has an invalid format.
		/// </exception>
		/// <exception cref="Apache.NMS.NMSException">
		/// Thrown when there is an unhandled exception thrown from the provider.
		/// </exception>
		void Reset();

	}
}

