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

using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Util
{
    public class ActiveMQMessageTransformation : MessageTransformation
    {
        private readonly Connection connection;

        public ActiveMQMessageTransformation(Connection connection) : base()
        {
            this.connection = connection;
        }

        #region Creation Methods and Conversion Support Methods

        protected override IMessage DoCreateMessage()
        {
            ActiveMQMessage message = new ActiveMQMessage();
            message.Connection = this.connection;
            return message;
        }

        protected override IBytesMessage DoCreateBytesMessage()
        {
            ActiveMQBytesMessage message = new ActiveMQBytesMessage();
            message.Connection = this.connection;
            return message;
        }

        protected override ITextMessage DoCreateTextMessage()
        {
            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.Connection = this.connection;
            return message;
        }

        protected override IStreamMessage DoCreateStreamMessage()
        {
            ActiveMQStreamMessage message = new ActiveMQStreamMessage();
            message.Connection = this.connection;
            return message;
        }

        protected override IMapMessage DoCreateMapMessage()
        {
            ActiveMQMapMessage message = new ActiveMQMapMessage();
            message.Connection = this.connection;
            return message;
        }

        protected override IObjectMessage DoCreateObjectMessage()
        {
            ActiveMQObjectMessage message = new ActiveMQObjectMessage();
            message.Connection = this.connection;
            return message;
        }

        protected override IDestination DoTransformDestination(IDestination destination)
        {
            return ActiveMQDestination.Transform(destination);
        }

        protected override void DoPostProcessMessage(IMessage message)
        {
        }

        #endregion
    }
}

