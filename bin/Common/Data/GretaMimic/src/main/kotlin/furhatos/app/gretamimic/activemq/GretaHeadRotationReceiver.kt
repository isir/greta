package furhatos.app.gretamimic.activemq

import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*



class GretaHeadRotationReceiver(private val brokerUrl: String, private val topicName: String) {

    private lateinit var connection: Connection
    private lateinit var session: Session
    private lateinit var consumer: MessageConsumer
    private lateinit var receivedMessage: String;
    private var frameNumber: Double? = 0.0
    private var roll: Double? = 0.0
    private var pitch: Double? = 0.0
    private var yaw: Double? = 0.0

    private var lastProcessingTime: Long = 0
    private var processingInterval: Long = 40 // 40 milli second interval



    fun start(callback: (Double?, Double?, Double?, Double?, String) -> Unit){
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

                            //println("Received message: $receivedMessage")

                            val angleRPY: Map<String, Double> = rotationStringToDoubleMap(receivedMessage)
                            roll = angleRPY["roll"]
                            pitch = angleRPY["pitch"]
                            yaw = angleRPY["yaw"]
                            frameNumber = frameNumber?.plus(1) //angleRPY["frameNumber"]

                            // Invoke callback with updated angle values
                            callback(roll, pitch, yaw, frameNumber, "RotateHeadEvent")


                        }
                    } catch (e: Exception) {
                        // Handle the exception
                        println("Error processing message: ${e.message}")
                    }

                    lastProcessingTime = currentTime
                }
            }
        } catch (e: Exception) {
            // Handle the exception
            println("Error starting consumer: ${e.message}")
        }

    }

    fun stop(){
        consumer.close()
        session.close()
        connection.close()
    }

    fun getReceivedMessage():String{
        return this.receivedMessage
    }

    fun getFrameTime(): Double?{
        return this.frameNumber
    }


    fun rotationStringToDoubleMap(stringRotation: String): Map<String, Double>{

        // This function takes the rotation string received by the activemq rotation consumer
        // And transform it to valid roll, pitch and yaw values

        val rotationStringArray = stringRotation.split(" ")

        val roll: Double = rotationStringArray[0].toDouble()
        val pitch: Double = rotationStringArray[1].toDouble()
        val yaw: Double = rotationStringArray[2].toDouble()
        val frameNumber = rotationStringArray[3].toDouble()

        val doubleMapValues: Map<String, Double> = mapOf("roll" to roll, "pitch" to pitch, "yaw" to yaw, "frameNumber" to frameNumber)

        return doubleMapValues
    }

    fun getRoll():Double?{
        return this.roll
    }
    fun getPitch():Double?{
        return pitch
    }
    fun getYaw():Double?{
        return yaw
    }


}


