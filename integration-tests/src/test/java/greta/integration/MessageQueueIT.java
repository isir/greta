package greta.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.*;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Message Queue System (ActiveMQ)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageQueueIT extends BaseIntegrationTest {
    
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    
    @BeforeAll
    @Override
    public void setupContainers() {
        super.setupContainers();
        gretaApp.start();
        RestAssured.baseURI = getGretaBaseUrl();
        
        // Setup JMS connection
        connectionFactory = new ActiveMQConnectionFactory(getActiveMQUrl());
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            fail("Failed to setup JMS connection: " + e.getMessage());
        }
    }
    
    @AfterAll
    public void tearDown() throws JMSException {
        if (session != null) session.close();
        if (connection != null) connection.close();
    }
    
    @Test
    @Order(1)
    @DisplayName("Should process animation signals through message queue")
    public void testAnimationSignalProcessing() throws Exception {
        String queueName = "greta.animation.signals";
        Queue queue = session.createQueue(queueName);
        
        // Create message consumer
        MessageConsumer consumer = session.createConsumer(queue);
        CountDownLatch latch = new CountDownLatch(1);
        List<String> receivedMessages = new ArrayList<>();
        
        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage) {
                    receivedMessages.add(((TextMessage) message).getText());
                    latch.countDown();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
        
        // Send animation request via REST API
        String animationRequest = """
            {
                "type": "gesture",
                "name": "wave",
                "sendToQueue": true
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(animationRequest)
        .when()
            .post("/api/animation/play")
        .then()
            .statusCode(202);
        
        // Wait for message to be received
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive message within 5 seconds");
        assertEquals(1, receivedMessages.size());
        assertTrue(receivedMessages.get(0).contains("wave"));
        
        consumer.close();
    }
    
    @Test
    @Order(2)
    @DisplayName("Should handle high-throughput message processing")
    public void testHighThroughputMessaging() throws Exception {
        String queueName = "greta.performance.test";
        Queue queue = session.createQueue(queueName);
        
        int messageCount = 1000;
        CountDownLatch sendLatch = new CountDownLatch(messageCount);
        CountDownLatch receiveLatch = new CountDownLatch(messageCount);
        AtomicInteger receivedCount = new AtomicInteger(0);
        
        // Create multiple consumers for parallel processing
        List<MessageConsumer> consumers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(message -> {
                receivedCount.incrementAndGet();
                receiveLatch.countDown();
            });
            consumers.add(consumer);
        }
        
        // Send messages in parallel
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < messageCount; i++) {
            final int messageId = i;
            new Thread(() -> {
                try {
                    MessageProducer producer = session.createProducer(queue);
                    TextMessage message = session.createTextMessage(
                        String.format("Performance test message %d", messageId));
                    producer.send(message);
                    producer.close();
                    sendLatch.countDown();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        // Wait for all messages to be sent and received
        assertTrue(sendLatch.await(10, TimeUnit.SECONDS), 
            "Should send all messages within 10 seconds");
        assertTrue(receiveLatch.await(15, TimeUnit.SECONDS), 
            "Should receive all messages within 15 seconds");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(messageCount, receivedCount.get());
        
        // Calculate throughput
        double throughput = (messageCount * 1000.0) / duration;
        System.out.println(String.format("Message throughput: %.2f messages/second", throughput));
        assertTrue(throughput > 100, "Should process at least 100 messages per second");
        
        // Cleanup
        consumers.forEach(c -> {
            try { c.close(); } catch (JMSException e) { }
        });
    }
    
    @Test
    @Order(3)
    @DisplayName("Should handle message queue failures gracefully")
    public void testMessageQueueResilience() throws Exception {
        String queueName = "greta.resilience.test";
        Queue queue = session.createQueue(queueName);
        
        // Setup dead letter queue monitoring
        String dlqName = "ActiveMQ.DLQ";
        Queue dlq = session.createQueue(dlqName);
        MessageConsumer dlqConsumer = session.createConsumer(dlq);
        List<Message> deadLetterMessages = new ArrayList<>();
        
        dlqConsumer.setMessageListener(deadLetterMessages::add);
        
        // Create a consumer that fails on certain messages
        MessageConsumer consumer = session.createConsumer(queue);
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        
        consumer.setMessageListener(message -> {
            try {
                String text = ((TextMessage) message).getText();
                if (text.contains("fail")) {
                    failedCount.incrementAndGet();
                    throw new RuntimeException("Simulated processing failure");
                }
                processedCount.incrementAndGet();
                message.acknowledge();
            } catch (Exception e) {
                // Message will be redelivered or sent to DLQ
            }
        });
        
        // Send mix of good and bad messages
        MessageProducer producer = session.createProducer(queue);
        for (int i = 0; i < 10; i++) {
            String messageText = i % 3 == 0 ? 
                String.format("Message %d - fail", i) : 
                String.format("Message %d - success", i);
            TextMessage message = session.createTextMessage(messageText);
            producer.send(message);
        }
        producer.close();
        
        // Wait for processing
        await()
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> processedCount.get() >= 7);
        
        // Verify resilience
        assertEquals(7, processedCount.get(), "Should process non-failing messages");
        assertTrue(failedCount.get() >= 3, "Should attempt to process failing messages");
        
        // Cleanup
        consumer.close();
        dlqConsumer.close();
    }
    
    @Test
    @Order(4)
    @DisplayName("Should maintain message ordering within partitions")
    public void testMessageOrdering() throws Exception {
        String queueName = "greta.ordering.test";
        Queue queue = session.createQueue(queueName);
        
        // Send ordered messages for different "agents"
        MessageProducer producer = session.createProducer(queue);
        String[] agents = {"agent1", "agent2", "agent3"};
        
        for (String agent : agents) {
            for (int i = 0; i < 10; i++) {
                TextMessage message = session.createTextMessage(
                    String.format("%s:sequence:%d", agent, i));
                message.setStringProperty("agent", agent);
                message.setIntProperty("sequence", i);
                producer.send(message);
            }
        }
        producer.close();
        
        // Consume messages and verify ordering per agent
        MessageConsumer consumer = session.createConsumer(queue);
        AtomicInteger totalReceived = new AtomicInteger(0);
        List<Message> receivedMessages = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(30);
        
        consumer.setMessageListener(message -> {
            receivedMessages.add(message);
            totalReceived.incrementAndGet();
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive all messages");
        
        // Verify ordering per agent
        for (String agent : agents) {
            List<Message> agentMessages = receivedMessages.stream()
                .filter(m -> {
                    try {
                        return agent.equals(m.getStringProperty("agent"));
                    } catch (JMSException e) {
                        return false;
                    }
                })
                .toList();
            
            assertEquals(10, agentMessages.size(), 
                "Should receive all messages for " + agent);
            
            // Check sequence ordering
            for (int i = 0; i < agentMessages.size() - 1; i++) {
                try {
                    int seq1 = agentMessages.get(i).getIntProperty("sequence");
                    int seq2 = agentMessages.get(i + 1).getIntProperty("sequence");
                    assertTrue(seq1 < seq2, 
                        "Messages should be in order for " + agent);
                } catch (JMSException e) {
                    fail("Failed to read message properties");
                }
            }
        }
        
        consumer.close();
    }
    
    @Test
    @Order(5)
    @DisplayName("Should handle topic-based publish/subscribe")
    public void testTopicPubSub() throws Exception {
        String topicName = "greta.events.system";
        Topic topic = session.createTopic(topicName);
        
        // Create multiple subscribers
        List<List<String>> subscriberMessages = new ArrayList<>();
        List<MessageConsumer> subscribers = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);
        
        for (int i = 0; i < 3; i++) {
            List<String> messages = new ArrayList<>();
            subscriberMessages.add(messages);
            
            MessageConsumer subscriber = session.createConsumer(topic);
            subscriber.setMessageListener(message -> {
                try {
                    messages.add(((TextMessage) message).getText());
                    if (messages.size() == 5) {
                        latch.countDown();
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });
            subscribers.add(subscriber);
        }
        
        // Publish events
        MessageProducer publisher = session.createProducer(topic);
        String[] events = {
            "system.started",
            "animation.completed",
            "behavior.planned",
            "user.interaction",
            "system.shutdown"
        };
        
        for (String event : events) {
            TextMessage message = session.createTextMessage(event);
            publisher.send(message);
            Thread.sleep(100); // Small delay to ensure ordering
        }
        publisher.close();
        
        // Wait for all subscribers to receive all messages
        assertTrue(latch.await(5, TimeUnit.SECONDS), 
            "All subscribers should receive all messages");
        
        // Verify each subscriber received all messages
        for (List<String> messages : subscriberMessages) {
            assertEquals(5, messages.size());
            for (String event : events) {
                assertTrue(messages.contains(event), 
                    "Subscriber should receive event: " + event);
            }
        }
        
        // Cleanup
        subscribers.forEach(s -> {
            try { s.close(); } catch (JMSException e) { }
        });
    }
    
    @Test
    @Order(6)
    @DisplayName("Should monitor queue metrics via management API")
    public void testQueueMetrics() throws Exception {
        // Create and use a test queue
        String queueName = "greta.metrics.test";
        Queue queue = session.createQueue(queueName);
        
        // Send some messages
        MessageProducer producer = session.createProducer(queue);
        for (int i = 0; i < 50; i++) {
            producer.send(session.createTextMessage("Metric test " + i));
        }
        producer.close();
        
        // Consume some messages
        MessageConsumer consumer = session.createConsumer(queue);
        for (int i = 0; i < 30; i++) {
            Message msg = consumer.receive(1000);
            assertNotNull(msg);
        }
        consumer.close();
        
        // Check metrics via management API
        given()
        .when()
            .get(getGretaManagementUrl() + "/metrics/messaging")
        .then()
            .statusCode(200)
            .body("queues", notNullValue())
            .body("totalMessagesSent", greaterThan(50))
            .body("totalMessagesReceived", greaterThan(30))
            .body("queues." + queueName + ".pending", equalTo(20))
            .body("queues." + queueName + ".consumers", greaterThanOrEqualTo(0));
    }
}