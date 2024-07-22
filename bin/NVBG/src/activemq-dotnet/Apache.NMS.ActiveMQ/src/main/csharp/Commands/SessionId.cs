/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


namespace Apache.NMS.ActiveMQ.Commands
{
    /*
     *
     *  Command code for OpenWire format for SessionId
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class SessionId : BaseDataStructure
    {
        public const byte ID_SESSIONID = 121;

        private ConnectionId parentId;

        string connectionId;
        long value;

        public SessionId()
        {
        }

        public SessionId( ConnectionId connectionId, long sessionId )
        {
            this.ConnectionId = connectionId.Value;
            this.value = sessionId;
        }

        public SessionId( ProducerId producerId )
        {
            this.ConnectionId = producerId.ConnectionId;
            this.value = producerId.SessionId;
        }

        public SessionId( ConsumerId consumerId )
        {
            this.ConnectionId = consumerId.ConnectionId;
            this.value = consumerId.SessionId;
        }

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_SESSIONID;
        }

        ///
        /// <summery>
        ///  Returns a string containing the information for this DataStructure
        ///  such as its type and value of its elements.
        /// </summery>
        ///
        public override string ToString()
        {
            return this.connectionId + ":" + this.value;
        }

        public ConnectionId ParentId
        {
            get
            {
                 if( this.parentId == null ) {
                     this.parentId = new ConnectionId( this );
                 }
                 return this.parentId;
            }
        }

        public string ConnectionId
        {
            get { return connectionId; }
            set { this.connectionId = value; }
        }

        public long Value
        {
            get { return value; }
            set { this.value = value; }
        }

        public override int GetHashCode()
        {
            int answer = 0;

            answer = (answer * 37) + HashCode(ConnectionId);
            answer = (answer * 37) + HashCode(Value);

            return answer;
        }

        public override bool Equals(object that)
        {
            if(that is SessionId)
            {
                return Equals((SessionId) that);
            }

            return false;
        }

        public virtual bool Equals(SessionId that)
        {
            if(!Equals(this.ConnectionId, that.ConnectionId))
            {
                return false;
            }
            if(!Equals(this.Value, that.Value))
            {
                return false;
            }

            return true;
        }
    };
}

