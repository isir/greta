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

namespace Apache.NMS.Util
{
    /// <summary>
    /// Base Utility class for conversion between IMessage type objects for different
    /// NMS providers.
    /// </summary>
	public abstract class MessageTransformation
	{
        protected MessageTransformation()
		{
		}

        public T TransformMessage<T>(IMessage message)
        {
            if(message is T)
            {
                return (T) message;
            }
            else
            {
                IMessage result = null;
    
                if(message is IBytesMessage)
                {
                    IBytesMessage bytesMsg = message as IBytesMessage;
                    bytesMsg.Reset();
                    IBytesMessage msg = DoCreateBytesMessage();

                    try
                    {
                        for(;;)
                        {
                            // Reads a byte from the message stream until the stream is empty
                            msg.WriteByte(bytesMsg.ReadByte());
                        }
                    }
                    catch
                    {
                    }

                    result = msg;
                }
                else if(message is IMapMessage)
                {
                    IMapMessage mapMsg = message as IMapMessage;
                    IMapMessage msg = DoCreateMapMessage();

                    foreach(string key in mapMsg.Body.Keys)
                    {
                        msg.Body[key] = mapMsg.Body[key];
                    }

                    result = msg;
                }
                else if(message is IObjectMessage)
                {
                    IObjectMessage objMsg = message as IObjectMessage;
                    IObjectMessage msg = DoCreateObjectMessage();
                    msg.Body = objMsg.Body;

                    result = msg;
                }
                else if(message is IStreamMessage)
                {
                    IStreamMessage streamMessage = message as IStreamMessage;
                    streamMessage.Reset();
                    IStreamMessage msg = DoCreateStreamMessage();

                    object obj = null;

                    try
                    {
                        while((obj = streamMessage.ReadObject()) != null)
                        {
                            msg.WriteObject(obj);
                        }
                    }
                    catch
                    {
                    }

                    result = msg;
                }
                else if(message is ITextMessage)
                {
                    ITextMessage textMsg = message as ITextMessage;
                    ITextMessage msg = DoCreateTextMessage();
                    msg.Text = textMsg.Text;
                    result = msg;
                }
                else
                {
                    result = DoCreateMessage();
                }

                CopyProperties(message, result);

                // Let the subclass have a chance to do any last minute configurations
                // on the newly converted message.
                DoPostProcessMessage(result);

                return (T) result;
            }
        }

        /// <summary>
        /// Copies the standard NMS and user defined properties from the givem
        /// message to the specified message, the class version transforms the
        /// Destination instead of just doing a straight copy.
        /// </summary>
        public virtual void CopyProperties(IMessage fromMessage, IMessage toMessage)
        {
            toMessage.NMSMessageId = fromMessage.NMSMessageId;
            toMessage.NMSCorrelationID = fromMessage.NMSCorrelationID;
            toMessage.NMSReplyTo = DoTransformDestination(fromMessage.NMSReplyTo);
            toMessage.NMSDestination = DoTransformDestination(fromMessage.NMSDestination);
            toMessage.NMSDeliveryMode = fromMessage.NMSDeliveryMode;
            toMessage.NMSRedelivered = fromMessage.NMSRedelivered;
            toMessage.NMSType = fromMessage.NMSType;
            toMessage.NMSPriority = fromMessage.NMSPriority;
            toMessage.NMSTimestamp = fromMessage.NMSTimestamp;
            toMessage.NMSTimeToLive = fromMessage.NMSTimeToLive;

            foreach(string key in fromMessage.Properties.Keys)
            {
                toMessage.Properties[key] = fromMessage.Properties[key];
            }
        }

        /// <summary>
        /// Copies the standard NMS and user defined properties from the givem
        /// message to the specified message, this method makes no attempt to convert
        /// the values in the Message to native provider implementations.
        /// </summary>
        public static void CopyNMSMessageProperties(IMessage fromMessage, IMessage toMessage)
        {
            toMessage.NMSMessageId = fromMessage.NMSMessageId;
            toMessage.NMSCorrelationID = fromMessage.NMSCorrelationID;
            toMessage.NMSReplyTo = fromMessage.NMSReplyTo;
            toMessage.NMSDestination = fromMessage.NMSDestination;
            toMessage.NMSDeliveryMode = fromMessage.NMSDeliveryMode;
            toMessage.NMSRedelivered = fromMessage.NMSRedelivered;
            toMessage.NMSType = fromMessage.NMSType;
            toMessage.NMSPriority = fromMessage.NMSPriority;
            toMessage.NMSTimestamp = fromMessage.NMSTimestamp;
            toMessage.NMSTimeToLive = fromMessage.NMSTimeToLive;

            foreach(string key in fromMessage.Properties.Keys)
            {
                toMessage.Properties[key] = fromMessage.Properties[key];
            }
        }

        #region Creation Methods and Conversion Support Methods

        protected abstract IMessage DoCreateMessage();
        protected abstract IBytesMessage DoCreateBytesMessage();
        protected abstract ITextMessage DoCreateTextMessage();
        protected abstract IStreamMessage DoCreateStreamMessage();
        protected abstract IMapMessage DoCreateMapMessage();
        protected abstract IObjectMessage DoCreateObjectMessage();

        protected abstract IDestination DoTransformDestination(IDestination destination);
        protected abstract void DoPostProcessMessage(IMessage message);

        #endregion

	}
}

