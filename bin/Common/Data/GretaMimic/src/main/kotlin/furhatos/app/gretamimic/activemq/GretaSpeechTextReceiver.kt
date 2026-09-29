package furhatos.app.gretamimic.activemq

import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

class GretaSpeechTextReceiver(private val brokerUrl: String, private val topicName: String) {

    private lateinit var connection: Connection
    private lateinit var session: Session
    private lateinit var consumer: MessageConsumer
    private lateinit var receivedMessage: String;

    private var lastProcessingTime: Long = 0
    private var processingInterval: Long = 1000 // 40 milli second interval

    fun start(callback: (String, String) -> Unit){
        try {
            val connectionFactory = ActiveMQConnectionFactory(brokerUrl)
            connection = connectionFactory.createConnection()
            connection.start()

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
            val destination = session.createTopic(topicName)
            consumer = session.createConsumer(destination)

            consumer.setMessageListener { message ->

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastProcessingTime >= processingInterval) {
                try {
                    if (message is TextMessage) {
                        receivedMessage = message.text
                        val customEvent = "speech Text Received Event"

                        callback(customEvent, receivedMessage)
                        //callback(customEvent, receivedMessage)
                    }
                }   catch (e: Exception) {
                    // Handle the exception
                    println("GretaSpeechTextreceiver->start(): Error processing message: ${e.message}")
                }

                    lastProcessingTime = currentTime
                }

        }

        } catch (e: Exception) {
            // Handle the exception
            println("GretaSpeechTextreceiver->start() Error starting consumer: ${e.message}")
        }
    }

    fun stop(){
        consumer.close()
        session.close()
        connection.close()
    }

}