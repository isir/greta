package furhatos.app.gretamimic

import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

    class AuEmitter(private val brokerUrl: String, private val topicName: String) {

        private lateinit var connection: Connection
        private lateinit var session: Session
        private lateinit var producer: MessageProducer

        init {
            initializeConnection()
        }

        private fun initializeConnection() {
            val connectionFactory = ActiveMQConnectionFactory(brokerUrl)
            connection = connectionFactory.createConnection()
            connection.start()
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
            val destination = session.createTopic(topicName)
            producer = session.createProducer(destination)
        }

        fun sendMessage(message: String) {
            val textMessage = session.createTextMessage(message)
            producer.send(textMessage)
            println("Message sent successfully")
        }

        fun closeConnection() {
            producer.close()
            session.close()
            connection.close()
        }
    }

