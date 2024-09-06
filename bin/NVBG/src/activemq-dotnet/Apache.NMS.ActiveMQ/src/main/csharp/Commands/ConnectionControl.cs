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


using Apache.NMS.ActiveMQ.State;

namespace Apache.NMS.ActiveMQ.Commands
{
    /*
     *
     *  Command code for OpenWire format for ConnectionControl
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class ConnectionControl : BaseCommand
    {
        public const byte ID_CONNECTIONCONTROL = 18;

        bool close;
        bool exit;
        bool faultTolerant;
        bool resume;
        bool suspend;
        string connectedBrokers;
        string reconnectTo;
        bool rebalanceConnection;
        byte[] token;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_CONNECTIONCONTROL;
        }

        ///
        /// <summery>
        ///  Returns a string containing the information for this DataStructure
        ///  such as its type and value of its elements.
        /// </summery>
        ///
        public override string ToString()
        {
            return GetType().Name + "[ " + 
                "commandId = " + this.CommandId + ", " + 
                "responseRequired = " + this.ResponseRequired + ", " + 
                "Close = " + Close + ", " + 
                "Exit = " + Exit + ", " + 
                "FaultTolerant = " + FaultTolerant + ", " + 
                "Resume = " + Resume + ", " + 
                "Suspend = " + Suspend + ", " + 
                "ConnectedBrokers = " + ConnectedBrokers + ", " + 
                "ReconnectTo = " + ReconnectTo + ", " + 
                "RebalanceConnection = " + RebalanceConnection + ", " + 
                "Token = " + Token ?? System.Text.ASCIIEncoding.ASCII.GetString(Token) + " ]";
        }

        public bool Close
        {
            get { return close; }
            set { this.close = value; }
        }

        public bool Exit
        {
            get { return exit; }
            set { this.exit = value; }
        }

        public bool FaultTolerant
        {
            get { return faultTolerant; }
            set { this.faultTolerant = value; }
        }

        public bool Resume
        {
            get { return resume; }
            set { this.resume = value; }
        }

        public bool Suspend
        {
            get { return suspend; }
            set { this.suspend = value; }
        }

        public string ConnectedBrokers
        {
            get { return connectedBrokers; }
            set { this.connectedBrokers = value; }
        }

        public string ReconnectTo
        {
            get { return reconnectTo; }
            set { this.reconnectTo = value; }
        }

        public bool RebalanceConnection
        {
            get { return rebalanceConnection; }
            set { this.rebalanceConnection = value; }
        }

        public byte[] Token
        {
            get { return token; }
            set { this.token = value; }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the isConnectionControl() query.
        /// </summery>
        ///
        public override bool IsConnectionControl
        {
            get { return true; }
        }

        ///
        /// <summery>
        ///  Allows a Visitor to visit this command and return a response to the
        ///  command based on the command type being visited.  The command will call
        ///  the proper processXXX method in the visitor.
        /// </summery>
        ///
        public override Response Visit(ICommandVisitor visitor)
        {
            return visitor.ProcessConnectionControl(this);
        }

    };
}

