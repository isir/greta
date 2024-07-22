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
using Apache.NMS.ActiveMQ.OpenWire;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Commands
{
	public class ActiveMQTextMessage : ActiveMQMessage, ITextMessage
	{
		public const byte ID_ACTIVEMQTEXTMESSAGE = 28;

		private String text = null;

		public ActiveMQTextMessage()
		{
		}

		public ActiveMQTextMessage(String text)
		{
			this.Text = text;
		}

		public override string ToString()
		{
            string text = this.Text;

            if(text != null && text.Length > 63)
            {
                text = text.Substring(0, 45) + "..." + text.Substring(text.Length - 12);
            }
			return base.ToString() + " Text = " + (text ?? "null");
		}

        public override void ClearBody()
        {
            base.ClearBody();
            this.text = null;
        }

		public override byte GetDataStructureType()
		{
			return ID_ACTIVEMQTEXTMESSAGE;
		}

		// Properties

		public string Text
		{
			get
			{
                try
                {
    				if(this.text == null && this.Content != null)
    				{
    					Stream stream = new MemoryStream(this.Content);

                        if(this.Connection != null && this.Compressed == true)
                        {
                            stream = this.Connection.CompressionPolicy.CreateDecompressionStream(stream);                            
                        }
                        
    					EndianBinaryReader reader = new EndianBinaryReader(stream);
    					this.text = reader.ReadString32();
                        this.Content = null;
    				}
    				return this.text;
                }
                catch(IOException ex)
                {
                    throw NMSExceptionSupport.Create(ex);
                }
			}

			set
			{
                FailIfReadOnlyBody();                
				this.text = value;
                this.Content = null;
			}
		}

        public override void BeforeMarshall(OpenWireFormat wireFormat)
        {
            base.BeforeMarshall(wireFormat);

            if(this.Content == null && text != null)
            {
                byte[] data = null;
                
                // Set initial size to the size of the string the UTF-8 encode could
                // result in more if there are chars that encode to multibye values.
                MemoryStream buffer = new MemoryStream(text.Length);
                Stream target = buffer;
				
                if(this.Connection != null && this.Connection.UseCompression)
                {
                    target = this.Connection.CompressionPolicy.CreateCompressionStream(target);                            
					this.Compressed = true;
                }
                
                EndianBinaryWriter writer = new EndianBinaryWriter(target);
                writer.WriteString32(text);
                target.Close();
                data = buffer.ToArray();
                
                this.Content = data;
                this.text = null;
            }
        }

        public override int Size()
        {
            if(this.Content == null && text != null) 
            {
                int size = DEFAULT_MINIMUM_MESSAGE_SIZE;

                if(MarshalledProperties != null) 
                {
                    size += MarshalledProperties.Length;
                }
                
                return (size += this.text.Length * 2);
            }

            return base.Size();
        }        
	}
}

