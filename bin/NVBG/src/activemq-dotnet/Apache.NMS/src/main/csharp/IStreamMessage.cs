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

namespace Apache.NMS
{   
    /// <summary>
    /// A StreamMessage object is used to send a stream of primitive types in the 
    /// .NET programming language. It is filled and read sequentially. It inherits 
    /// from the Message interface and adds a stream message body.
    /// 
    /// The primitive types can be read or written explicitly using methods for each 
    /// type. They may also be read or written generically as objects. For instance, 
    /// a call to IStreamMessage.WriteInt32(6) is equivalent to 
    /// StreamMessage.WriteObject( (Int32)6 ). Both forms are provided, because the 
    /// explicit form is convenient for static programming, and the object form is 
    /// needed when types are not known at compile time.
    /// 
    /// When the message is first created, and when ClearBody is called, the body of 
    /// the message is in write-only mode. After the first call to reset has been made, 
    /// the message body is in read-only mode. After a message has been sent, the 
    /// client that sent it can retain and modify it without affecting the message 
    /// that has been sent. The same message object can be sent multiple times. When a 
    /// message has been received, the provider has called reset so that the message 
    /// body is in read-only mode for the client.
    /// 
    /// If ClearBody is called on a message in read-only mode, the message body is 
    /// cleared and the message body is in write-only mode.
    /// 
    /// If a client attempts to read a message in write-only mode, a 
    /// MessageNotReadableException is thrown.
    /// 
    /// If a client attempts to write a message in read-only mode, a 
    /// MessageNotWriteableException is thrown.
    /// 
    /// IStreamMessage objects support the following conversion table. The marked cases 
    /// must be supported. The unmarked cases must throw a NMSException. The 
    /// String-to-primitive conversions may throw a runtime exception if the primitive's 
    /// valueOf() method does not accept it as a valid String representation of the 
    /// primitive.
    /// 
    /// A value written as the row type can be read as the column type.
    /// 
    ///  |        | boolean byte short char int long float double String byte[]
    ///  |----------------------------------------------------------------------
    ///  |boolean |    X                                            X
    ///  |byte    |          X     X         X   X                  X   
    ///  |short   |                X         X   X                  X   
    ///  |char    |                     X                           X
    ///  |int     |                          X   X                  X   
    ///  |long    |                              X                  X   
    ///  |float   |                                    X     X      X   
    ///  |double  |                                          X      X   
    ///  |String  |    X     X     X         X   X     X     X      X   
    ///  |byte[]  |                                                        X
    ///  |----------------------------------------------------------------------
    /// 
    /// </summary>
    public interface IStreamMessage : IMessage
    {

        /// <summary>
        /// Reads a boolean from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Boolean"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>
        bool ReadBoolean();
        
        /// <summary>
        /// Reads a byte from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Byte"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        byte ReadByte();
        
        /// <summary>
        /// Reads a byte array field from the stream message into the specified byte[] 
        /// object (the read buffer).
        /// 
        /// To read the field value, ReadBytes should be successively called until it returns 
        /// a value less than the length of the read buffer. The value of the bytes in the 
        /// buffer following the last byte read is undefined.
        /// 
        /// If ReadBytes returns a value equal to the length of the buffer, a subsequent 
        /// ReadBytes call must be made. If there are no more bytes to be read, this call 
        /// returns -1.
        /// 
        /// If the byte array field value is null, ReadBytes returns -1.
        /// If the byte array field value is empty, ReadBytes returns 0.
        /// 
        /// Once the first ReadBytes call on a byte[] field value has been made, the full 
        /// value of the field must be read before it is valid to read the next field. 
        /// An attempt to read the next field before that has been done will throw a 
        /// MessageFormatException.
        /// 
        /// To read the byte field value into a new byte[] object, use the ReadObject method.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Byte"/>
        /// </param>
        /// <returns>
        /// A <see cref="System.Byte"/>
        /// the total number of bytes read into the buffer, or -1 if there is no more data 
        /// because the end of the byte field has been reached
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>     
        /// <seealso cref="ReadObject"/>
        int ReadBytes(byte[] value);
        
        /// <summary>
        /// Reads a char from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Char"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>                
        char ReadChar();
        
        /// <summary>
        /// Reads a short from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Int16"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        short ReadInt16();
        
        /// <summary>
        /// Reads a int from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Int32"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        int ReadInt32();
        
        /// <summary>
        /// Reads a long from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Int64"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        long ReadInt64();
        
        /// <summary>
        /// Reads a float from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Single"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        float ReadSingle();
        
        /// <summary>
        /// Reads a double from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Double"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        double ReadDouble();
        
        /// <summary>
        /// Reads a string from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.String"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        string ReadString();
        
        /// <summary>
        /// Reads a Object from the stream message.
        /// </summary>
        /// <returns>
        /// A <see cref="System.Object"/>
        /// </returns>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to read the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageEOFException">
        /// if unexpected end of message stream has been reached.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageFormatException">
        /// if this type conversion is invalid.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotReadableException">
        /// if the message is in write-only mode.
        /// </exception>        
        Object ReadObject();
        
        /// <summary>
        /// Writes a boolean to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Boolean"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteBoolean(bool value);

        /// <summary>
        /// Writes a byte to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Byte"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteByte(byte value);

        /// <summary>
        /// Writes a byte array field to the stream message.
        /// 
        /// The byte array value is written to the message as a byte array field. 
        /// Consecutively written byte array fields are treated as two distinct 
        /// fields when the fields are read.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Byte"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteBytes(byte[] value);

        /// <summary>
        /// Writes a portion of a byte array as a byte array field to the stream message.
        /// 
        /// The a portion of the byte array value is written to the message as a byte 
        /// array field. Consecutively written byte array fields are treated as two distinct 
        /// fields when the fields are read.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Byte"/>
        /// </param>
        /// <param name="offset">
        /// A <see cref="System.Int32"/> value that indicates the point in the buffer to 
        /// begin writing to the stream message.
        /// </param>
        /// <param name="length">
        /// A <see cref="System.Int32"/> value that indicates how many bytes in the buffer
        /// to write to the stream message.
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteBytes(byte[] value, int offset, int length);

        /// <summary>
        /// Writes a char to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Char"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteChar(char value);
        
        /// <summary>
        /// Writes a short to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Int16"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteInt16(short value);
        
        /// <summary>
        /// Writes a int to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Int32"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteInt32(int value);
        
        /// <summary>
        /// Writes a long to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Int64"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteInt64(long value);
        
        /// <summary>
        /// Writes a float to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Single"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteSingle(float value);
        
        /// <summary>
        /// Writes a double to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Double"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteDouble(double value);
        
        /// <summary>
        /// Writes a string to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.String"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteString(string value);
        
        /// <summary>
        /// Writes a boolean to the stream message.
        /// </summary>
        /// <param name="value">
        /// A <see cref="System.Boolean"/>
        /// </param>
        /// <exception cref="Apache.NMS.NMSException">
        /// if the NMS provider fails to write to the message due to some internal error.
        /// </exception>
        /// <exception cref="Apache.NMS.MessageNotWriteableException">
        /// if the message is in read-only mode.
        /// </exception>
        void WriteObject(Object value);

        /// <summary>
        /// Puts the message body in read-only mode and repositions the stream to the beginning.
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
