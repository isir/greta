package furhatos.app.gretamimic.activemq

import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*
import kotlin.math.abs

class GretaAUsReceiver(private val brokerUrl: String, private val topicName: String) {

    private lateinit var connection: Connection
    private lateinit var session: Session
    private lateinit var consumer: MessageConsumer
    private lateinit var receivedMessage: String;
    private var frameNumber: Double? = 0.0

    private var previousValues: Array<Double> = Array(128) { 0.0 }

    private var lastProcessingTime: Long = 0
    private var processingInterval: Long = 40 // 40 milli second interval



    fun start(callback: (Array<Double>, Double?) -> Unit) {
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

                            val auArray = treatMessage(receivedMessage)
                            println("previous values == curr val ---> ${arraysAreEqual(auArray, previousValues)}")
                            // Check if array has changed compared to previous values
                            if (!arraysAreEqual(auArray, previousValues)) {
                                // Only update values if something has changed
                                val auArrayNonNullValues = updateValues(auArray)
                                frameNumber = frameNumber?.plus(1)
                                //println("previous values == curr val ---> ${arraysAreEqual(auArray, previousValues)}")
                                println("prev val: ${previousValues.contentToString()}")
                                // Invoke callback with updated values
                                callback(auArrayNonNullValues, frameNumber)
                            }
                            /*
                            val auArrayNonNullValues = updateValues(auArray)
                            frameNumber = frameNumber?.plus(1)

                            // Invoke callback with updated angle values
                            callback(auArrayNonNullValues, frameNumber)*/

                        }
                    }   catch (e: Exception) {
                        // Handle the exception
                        println("GretaAUsReceiver->start() Error processing message: ${e.message}")
                    }

                    lastProcessingTime = currentTime
                }
            }
        } catch (e: Exception) {
            // Handle the exception
            println("GretaAUsReceiver->start() Error starting consumer: ${e.message}")
        }
    }
    private fun updateValues(receivedValues: Array<Double?>): Array<Double> {
        val updatedValues = Array(128) { 0.0 }
        for (i in receivedValues.indices) {
            updatedValues[i] = receivedValues[i] ?: previousValues[i]
        }
        previousValues = updatedValues.copyOf()
        return updatedValues
    }

    private fun arraysAreEqual(currentValues: Array<Double?>, previousValues: Array<Double>): Boolean {
        val tolerance = 2e-3
        for (i in currentValues.indices) {
            val currValue = currentValues[i] ?: 0.0
            if (kotlin.math.abs(currValue - previousValues[i]) > tolerance) {
                return false // Array has changed
            }
        }
        return true // No significant change in the array
    }

    fun stop(){
        consumer.close()
        session.close()
        connection.close()
    }

    fun treatMessage(message: String): Array<Double?>{

        val element = message.removeSurrounding("[", "]").split(", ")
        val array = element.map{ if (it=="-") null else it.toDouble()/1000}.toTypedArray()
//        for (i in array.indices){
//            if (array[i]!! <0){
//                array[i] = array[i]?.let { abs(it) }
//            }
//        }

        return array

    }
}