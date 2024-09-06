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
/*
 *
 *  MarshallerFactory code for OpenWire Protocol Version 6
 *
 *  NOTE!: This file is auto generated - do not modify!
 *         if you need to make a change, please see the Java Classes
 *         in the nms-activemq-openwire-generator module
 *
 */

namespace Apache.NMS.ActiveMQ.OpenWire.V6
{
    /// <summary>
    ///  Used to create marshallers for a specific version of the OpenWire protocol.
    ///  Each non-abstract DataStructure object has a registered Marshaller that is
    ///  created and added to the OpenWireFormat objects format collection.
    /// </summary>
    public class MarshallerFactory : IMarshallerFactory
    {
        /// <summery>
        ///  Adds the Marshallers for this version of the OpenWire protocol to the 
        ///  Collection of Marshallers stored in the OpenWireFormat class.
        /// </summery>
        public void configure(OpenWireFormat format) 
        {
            format.clearMarshallers();
            format.addMarshaller(new ActiveMQBlobMessageMarshaller());
            format.addMarshaller(new ActiveMQBytesMessageMarshaller());
            format.addMarshaller(new ActiveMQMapMessageMarshaller());
            format.addMarshaller(new ActiveMQMessageMarshaller());
            format.addMarshaller(new ActiveMQObjectMessageMarshaller());
            format.addMarshaller(new ActiveMQQueueMarshaller());
            format.addMarshaller(new ActiveMQStreamMessageMarshaller());
            format.addMarshaller(new ActiveMQTempQueueMarshaller());
            format.addMarshaller(new ActiveMQTempTopicMarshaller());
            format.addMarshaller(new ActiveMQTextMessageMarshaller());
            format.addMarshaller(new ActiveMQTopicMarshaller());
            format.addMarshaller(new BrokerIdMarshaller());
            format.addMarshaller(new BrokerInfoMarshaller());
            format.addMarshaller(new ConnectionControlMarshaller());
            format.addMarshaller(new ConnectionErrorMarshaller());
            format.addMarshaller(new ConnectionIdMarshaller());
            format.addMarshaller(new ConnectionInfoMarshaller());
            format.addMarshaller(new ConsumerControlMarshaller());
            format.addMarshaller(new ConsumerIdMarshaller());
            format.addMarshaller(new ConsumerInfoMarshaller());
            format.addMarshaller(new ControlCommandMarshaller());
            format.addMarshaller(new DataArrayResponseMarshaller());
            format.addMarshaller(new DataResponseMarshaller());
            format.addMarshaller(new DestinationInfoMarshaller());
            format.addMarshaller(new DiscoveryEventMarshaller());
            format.addMarshaller(new ExceptionResponseMarshaller());
            format.addMarshaller(new FlushCommandMarshaller());
            format.addMarshaller(new IntegerResponseMarshaller());
            format.addMarshaller(new JournalQueueAckMarshaller());
            format.addMarshaller(new JournalTopicAckMarshaller());
            format.addMarshaller(new JournalTraceMarshaller());
            format.addMarshaller(new JournalTransactionMarshaller());
            format.addMarshaller(new KeepAliveInfoMarshaller());
            format.addMarshaller(new LastPartialCommandMarshaller());
            format.addMarshaller(new LocalTransactionIdMarshaller());
            format.addMarshaller(new MessageAckMarshaller());
            format.addMarshaller(new MessageDispatchMarshaller());
            format.addMarshaller(new MessageDispatchNotificationMarshaller());
            format.addMarshaller(new MessageIdMarshaller());
            format.addMarshaller(new MessagePullMarshaller());
            format.addMarshaller(new NetworkBridgeFilterMarshaller());
            format.addMarshaller(new PartialCommandMarshaller());
            format.addMarshaller(new ProducerAckMarshaller());
            format.addMarshaller(new ProducerIdMarshaller());
            format.addMarshaller(new ProducerInfoMarshaller());
            format.addMarshaller(new RemoveInfoMarshaller());
            format.addMarshaller(new RemoveSubscriptionInfoMarshaller());
            format.addMarshaller(new ReplayCommandMarshaller());
            format.addMarshaller(new ResponseMarshaller());
            format.addMarshaller(new SessionIdMarshaller());
            format.addMarshaller(new SessionInfoMarshaller());
            format.addMarshaller(new ShutdownInfoMarshaller());
            format.addMarshaller(new SubscriptionInfoMarshaller());
            format.addMarshaller(new TransactionInfoMarshaller());
            format.addMarshaller(new WireFormatInfoMarshaller());
            format.addMarshaller(new XATransactionIdMarshaller());
        }
    }
}
