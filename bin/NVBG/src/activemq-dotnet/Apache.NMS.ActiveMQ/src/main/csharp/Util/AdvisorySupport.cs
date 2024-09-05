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

using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ
{
    public class AdvisorySupport
    {
        public static readonly String ADVISORY_TOPIC_PREFIX = "ActiveMQ.Advisory.";
        public static readonly ActiveMQTopic CONNECTION_ADVISORY_TOPIC = new ActiveMQTopic(ADVISORY_TOPIC_PREFIX + "Connection");
        public static readonly ActiveMQTopic QUEUE_ADVISORY_TOPIC = new ActiveMQTopic(ADVISORY_TOPIC_PREFIX + "Queue");
        public static readonly ActiveMQTopic TOPIC_ADVISORY_TOPIC = new ActiveMQTopic(ADVISORY_TOPIC_PREFIX + "Topic");
        public static readonly ActiveMQTopic TEMP_QUEUE_ADVISORY_TOPIC = new ActiveMQTopic(ADVISORY_TOPIC_PREFIX + "TempQueue");
        public static readonly ActiveMQTopic TEMP_TOPIC_ADVISORY_TOPIC = new ActiveMQTopic(ADVISORY_TOPIC_PREFIX + "TempTopic");
        public static readonly String PRODUCER_ADVISORY_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "Producer.";
        public static readonly String QUEUE_PRODUCER_ADVISORY_TOPIC_PREFIX = PRODUCER_ADVISORY_TOPIC_PREFIX + "Queue.";
        public static readonly String TOPIC_PRODUCER_ADVISORY_TOPIC_PREFIX = PRODUCER_ADVISORY_TOPIC_PREFIX + "Topic.";
        public static readonly String CONSUMER_ADVISORY_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "Consumer.";
        public static readonly String QUEUE_CONSUMER_ADVISORY_TOPIC_PREFIX = CONSUMER_ADVISORY_TOPIC_PREFIX + "Queue.";
        public static readonly String TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX = CONSUMER_ADVISORY_TOPIC_PREFIX + "Topic.";
        public static readonly String EXPIRED_TOPIC_MESSAGES_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "Expired.Topic.";
        public static readonly String EXPIRED_QUEUE_MESSAGES_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "Expired.Queue.";
        public static readonly String NO_TOPIC_CONSUMERS_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "NoConsumer.Topic.";
        public static readonly String NO_QUEUE_CONSUMERS_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "NoConsumer.Queue.";
        public static readonly String SLOW_CONSUMER_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "SlowConsumer.";
        public static readonly String FAST_PRODUCER_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "FastProducer.";
        public static readonly String MESSAGE_DISCAREDED_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "MessageDiscarded.";
        public static readonly String FULL_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "FULL.";
        public static readonly String MESSAGE_DELIVERED_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "MessageDelivered.";
        public static readonly String MESSAGE_CONSUMED_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "MessageConsumed.";
        public static readonly String MESSAGE_DLQ_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "MessageDLQd.";
        public static readonly String MASTER_BROKER_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "MasterBroker";
        public static readonly String NETWORK_BRIDGE_TOPIC_PREFIX = ADVISORY_TOPIC_PREFIX + "NetworkBridge";
        public static readonly String AGENT_TOPIC = "ActiveMQ.Agent";
        public static readonly String ADIVSORY_MESSAGE_TYPE = "Advisory";
        public static readonly String MSG_PROPERTY_ORIGIN_BROKER_ID = "originBrokerId";
        public static readonly String MSG_PROPERTY_ORIGIN_BROKER_NAME = "originBrokerName";
        public static readonly String MSG_PROPERTY_ORIGIN_BROKER_URL = "originBrokerURL";
        public static readonly String MSG_PROPERTY_USAGE_NAME = "usageName";
        public static readonly String MSG_PROPERTY_CONSUMER_ID = "consumerId";
        public static readonly String MSG_PROPERTY_PRODUCER_ID = "producerId";
        public static readonly String MSG_PROPERTY_MESSAGE_ID = "orignalMessageId";
        public static readonly String MSG_PROPERTY_CONSUMER_COUNT = "consumerCount";
        public static readonly String MSG_PROPERTY_DISCARDED_COUNT = "discardedCount";

        public static readonly ActiveMQTopic TEMP_DESTINATION_COMPOSITE_ADVISORY_TOPIC = new ActiveMQTopic(
                TEMP_QUEUE_ADVISORY_TOPIC.PhysicalName + "," + TEMP_TOPIC_ADVISORY_TOPIC.PhysicalName);

        private AdvisorySupport()
        {
        }

        public static ActiveMQTopic GetConnectionAdvisoryTopic()
        {
            return CONNECTION_ADVISORY_TOPIC;
        }

        public static ActiveMQTopic GetConsumerAdvisoryTopic(IDestination destination)
        {
            return GetConsumerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static ActiveMQTopic GetConsumerAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsQueue)
            {
                return new ActiveMQTopic(QUEUE_CONSUMER_ADVISORY_TOPIC_PREFIX + destination.PhysicalName);
            }
            else
            {
                return new ActiveMQTopic(TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX + destination.PhysicalName);
            }
        }
    
        public static ActiveMQTopic GetProducerAdvisoryTopic(IDestination destination)
        {
            return GetProducerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetProducerAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsQueue)
            {
                return new ActiveMQTopic(QUEUE_PRODUCER_ADVISORY_TOPIC_PREFIX + destination.PhysicalName);
            }
            else
            {
                return new ActiveMQTopic(TOPIC_PRODUCER_ADVISORY_TOPIC_PREFIX + destination.PhysicalName);
            }
        }
    
        public static ActiveMQTopic GetExpiredMessageTopic(IDestination destination)
        {
            return GetExpiredMessageTopic(ActiveMQDestination.Transform(destination));
        }

        public static ActiveMQTopic GetExpiredMessageTopic(ActiveMQDestination destination)
        {
            if (destination.IsQueue)
            {
                return GetExpiredQueueMessageAdvisoryTopic(destination);
            }
            return GetExpiredTopicMessageAdvisoryTopic(destination);
        }
    
        public static ActiveMQTopic GetExpiredTopicMessageAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = EXPIRED_TOPIC_MESSAGES_TOPIC_PREFIX + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetExpiredQueueMessageAdvisoryTopic(IDestination destination)
        {
            return GetExpiredQueueMessageAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetExpiredQueueMessageAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = EXPIRED_QUEUE_MESSAGES_TOPIC_PREFIX + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetNoTopicConsumersAdvisoryTopic(IDestination destination)
        {
            return GetNoTopicConsumersAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetNoTopicConsumersAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = NO_TOPIC_CONSUMERS_TOPIC_PREFIX + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetNoQueueConsumersAdvisoryTopic(IDestination destination)
        {
            return GetNoQueueConsumersAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static ActiveMQTopic GetNoQueueConsumersAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = NO_QUEUE_CONSUMERS_TOPIC_PREFIX + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetSlowConsumerAdvisoryTopic(IDestination destination)
        {
            return GetSlowConsumerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetSlowConsumerAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = SLOW_CONSUMER_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetFastProducerAdvisoryTopic(IDestination destination)
        {
            return GetFastProducerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static ActiveMQTopic GetFastProducerAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = FAST_PRODUCER_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }

        public static ActiveMQTopic GetMessageDiscardedAdvisoryTopic(IDestination destination)
        {
            return GetMessageDiscardedAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetMessageDiscardedAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = MESSAGE_DISCAREDED_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetMessageDeliveredAdvisoryTopic(IDestination destination)
        {
            return GetMessageDeliveredAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static ActiveMQTopic GetMessageDeliveredAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = MESSAGE_DELIVERED_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }

        public static ActiveMQTopic GetMessageConsumedAdvisoryTopic(IDestination destination)
        {
            return GetMessageConsumedAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetMessageConsumedAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = MESSAGE_CONSUMED_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }

        public static ActiveMQTopic GetMessageDLQdAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = MESSAGE_DLQ_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetMasterBrokerAdvisoryTopic(IDestination destination)
        {
            return GetMasterBrokerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetMasterBrokerAdvisoryTopic()
        {
            return new ActiveMQTopic(MASTER_BROKER_TOPIC_PREFIX);
        }

        public static ActiveMQTopic GetNetworkBridgeAdvisoryTopic()
        {
            return new ActiveMQTopic(NETWORK_BRIDGE_TOPIC_PREFIX);
        }
    
        public static ActiveMQTopic GetFullAdvisoryTopic(IDestination destination)
        {
            return GetFullAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static ActiveMQTopic GetFullAdvisoryTopic(ActiveMQDestination destination)
        {
            String name = FULL_TOPIC_PREFIX + destination.GetDestinationTypeAsString() + "."
                    + destination.PhysicalName;
            return new ActiveMQTopic(name);
        }
    
        public static ActiveMQTopic GetDestinationAdvisoryTopic(IDestination destination)
        {
            return GetDestinationAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static ActiveMQTopic GetDestinationAdvisoryTopic(ActiveMQDestination destination)
        {
            switch (destination.GetDestinationType())
            {
                case ActiveMQDestination.ACTIVEMQ_QUEUE:
                    return QUEUE_ADVISORY_TOPIC;
                case ActiveMQDestination.ACTIVEMQ_TOPIC:
                    return TOPIC_ADVISORY_TOPIC;
                case ActiveMQDestination.ACTIVEMQ_TEMPORARY_QUEUE:
                    return TEMP_QUEUE_ADVISORY_TOPIC;
                case ActiveMQDestination.ACTIVEMQ_TEMPORARY_TOPIC:
                    return TEMP_TOPIC_ADVISORY_TOPIC;
                default:
                    throw new NMSException("Unknown destination type: " + destination.DestinationType);
            }
        }

        public static bool IsDestinationAdvisoryTopic(IDestination destination)
        {
            return IsDestinationAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static bool IsTempDestinationAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                bool containsNonTempDests = false;
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (!IsTempDestinationAdvisoryTopic(compositeDestinations[i]))
                    {
                        containsNonTempDests = true;
                    }
                }
                return !containsNonTempDests;
            }
            else
            {
                return destination.Equals(TEMP_QUEUE_ADVISORY_TOPIC) ||
                       destination.Equals(TEMP_TOPIC_ADVISORY_TOPIC);
            }
        }

        public static bool IsDestinationAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsDestinationAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.Equals(TEMP_QUEUE_ADVISORY_TOPIC) ||
                       destination.Equals(TEMP_TOPIC_ADVISORY_TOPIC) ||
                       destination.Equals(QUEUE_ADVISORY_TOPIC) ||
                       destination.Equals(TOPIC_ADVISORY_TOPIC);
            }
        }

        public static bool IsAdvisoryTopic(IDestination destination)
        {
            return IsAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(ADVISORY_TOPIC_PREFIX);
            }
        }

        public static bool IsConnectionAdvisoryTopic(IDestination destination)
        {
            return IsConnectionAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsConnectionAdvisoryTopic(ActiveMQDestination destination) {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsConnectionAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.Equals(CONNECTION_ADVISORY_TOPIC);
            }
        }
    
        public static bool IsProducerAdvisoryTopic(IDestination destination)
        {
            return IsProducerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsProducerAdvisoryTopic(ActiveMQDestination destination) {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsProducerAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(PRODUCER_ADVISORY_TOPIC_PREFIX);
            }
        }
    
        public static bool IsConsumerAdvisoryTopic(IDestination destination)
        {
            return IsConsumerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsConsumerAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsConsumerAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(CONSUMER_ADVISORY_TOPIC_PREFIX);
            }
        }
    
        public static bool IsSlowConsumerAdvisoryTopic(IDestination destination)
        {
            return IsSlowConsumerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsSlowConsumerAdvisoryTopic(ActiveMQDestination destination) {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsSlowConsumerAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(SLOW_CONSUMER_TOPIC_PREFIX);
            }
        }
    
        public static bool IsFastProducerAdvisoryTopic(IDestination destination)
        {
            return IsFastProducerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsFastProducerAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsFastProducerAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(FAST_PRODUCER_TOPIC_PREFIX);
            }
        }
    
        public static bool IsMessageConsumedAdvisoryTopic(IDestination destination)
        {
            return IsMessageConsumedAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsMessageConsumedAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsMessageConsumedAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(MESSAGE_CONSUMED_TOPIC_PREFIX);
            }
        }
    
        public static bool IsMasterBrokerAdvisoryTopic(IDestination destination)
        {
            return IsMasterBrokerAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static bool IsMasterBrokerAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsMasterBrokerAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(MASTER_BROKER_TOPIC_PREFIX);
            }
        }
    
        public static bool IsMessageDeliveredAdvisoryTopic(IDestination destination)
        {
            return IsMessageDeliveredAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }
    
        public static bool IsMessageDeliveredAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsMessageDeliveredAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(MESSAGE_DELIVERED_TOPIC_PREFIX);
            }
        }
    
        public static bool IsMessageDiscardedAdvisoryTopic(IDestination destination)
        {
            return IsMessageDiscardedAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static bool IsMessageDiscardedAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsMessageDiscardedAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(MESSAGE_DISCAREDED_TOPIC_PREFIX);
            }
        }
    
        public static bool IsFullAdvisoryTopic(IDestination destination)
        {
            return IsFullAdvisoryTopic(ActiveMQDestination.Transform(destination));
        }

        public static bool IsFullAdvisoryTopic(ActiveMQDestination destination)
        {
            if (destination.IsComposite)
            {
                ActiveMQDestination[] compositeDestinations = destination.GetCompositeDestinations();
                for (int i = 0; i < compositeDestinations.Length; i++)
                {
                    if (IsFullAdvisoryTopic(compositeDestinations[i]))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return destination.IsTopic && destination.PhysicalName.StartsWith(FULL_TOPIC_PREFIX);
            }
        }
    }
}

