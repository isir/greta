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

namespace Apache.NMS.Util
{    
    public sealed class NMSExceptionSupport
    {
        private NMSExceptionSupport()
        {            
        }
        
        public static NMSException Create(string message, string errorCode, Exception cause)
        {
            NMSException exception = new NMSException(message, errorCode, cause);
            return exception;
        }
                
        public static NMSException Create(string message, Exception cause)
        {
            NMSException exception = new NMSException(message, cause);
            return exception;
        }
        
        public static NMSException Create(Exception cause)
        {
            if(cause is NMSException) 
            {
                return (NMSException) cause;
            }
            string msg = cause.Message;
            if(msg == null || msg.Length == 0) 
            {
                msg = cause.ToString();
            }
            NMSException exception = new NMSException(msg, cause);
            return exception;
        }
    
        public static MessageEOFException CreateMessageEOFException(Exception cause) 
        {
            string msg = cause.Message;
            if (msg == null || msg.Length == 0) 
            {
                msg = cause.ToString();
            }
            MessageEOFException exception = new MessageEOFException(msg, cause);
            return exception;
        }
    
        public static MessageFormatException CreateMessageFormatException(Exception cause) 
        {
            string msg = cause.Message;
            if (msg == null || msg.Length == 0) 
            {
                msg = cause.ToString();
            }
            MessageFormatException exception = new MessageFormatException(msg, cause);
            return exception;
        }
    }
}
