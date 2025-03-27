package furhatos.app.gretamimic.activemq


import furhatos.flow.kotlin.*
import org.apache.activemq.ActiveMQConnectionFactory
import java.io.File
import java.io.FileOutputStream
import javax.jms.*


@Suppress("NAME_SHADOWING")
class AudioReceiver(private val brokerUrl: String, private val topicName: String) {

    private lateinit var connection: Connection
    private lateinit var session : Session
    private lateinit var consumer: MessageConsumer
    lateinit var filepath: String
    lateinit var audioFileName: String
    lateinit var phonemeFileName: String
    var isreceived: Boolean = false
    var isconnected = false
    val audioUploader = AudioUrlSelenium()

    fun start() {
        try {
            val connectionFactory = ActiveMQConnectionFactory(brokerUrl)
            connection = connectionFactory.createConnection()
            connection.start()

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
            val destination = session.createTopic(topicName)
            consumer = session.createConsumer(destination)
            isconnected = true
            consumer.setMessageListener { message ->
                try {
                    //val message: Message = consumer.receive()
                    processMessage(message)
                    isreceived = true
                    //println("isreceived: $isreceived")

                    //println("isreceived: hello")

                }   catch (e: Exception) {
                    // Handle the exception
                    println("AudioReceiver->start() Error processing message: ${e.message}")
                }

            }
        } catch (e: Exception) {
            // Handle the exception
            println("AudioReceiver->start() Error starting consumer: ${e.message}")
        }
    }

    private fun processMessage(message: Message) {
        when (message) {
            is BytesMessage -> handleAudioMessage(message)
            is TextMessage -> handleTextMessage(message)
        }
    }

    private fun handleAudioMessage(message: BytesMessage) {
        val audioBuffer = ByteArray(message.bodyLength.toInt())
        message.readBytes(audioBuffer)

        val sampleRate = 48000.0 // Furhat robot requires 16KHz //message.getFloatProperty("SampleRate") 48KHz
        val bitsPerSample = message.getIntProperty("BitsPerSample")
        val channels = message.getIntProperty("Channels")

        val wavFile = File("$filepath$audioFileName")

        // Check if file exists and delete it if it does
        if (wavFile.exists()) {
            wavFile.delete()
        }

        FileOutputStream(wavFile).use { fos ->
            writeWavHeader(fos, audioBuffer.size, sampleRate.toInt(), bitsPerSample, channels)
            fos.write(audioBuffer)
        }

        println("AudioReceiver->start() Received and saved audio buffer as WAV file")
    }

    private fun handleTextMessage(message: TextMessage) {
        val type = message.getStringProperty("Type")
        val text = message.text

        when (type) {
            "PhonemeData" -> {
                val phonemeFile = File("$filepath$phonemeFileName")
                phonemeFile.writeText(text)
                //println(text)
                //println("Received and saved phoneme data")
            }
        }
    }

    fun stop(){
            consumer.close()
            session.close()
            connection.close()
        }

    companion object {
        fun writeWavHeader(fos: FileOutputStream, dataLength: Int, sampleRate: Int, bitsPerSample: Int, channels: Int) {

            val header = ByteArray(44)
            val totalDataLen = dataLength + 36
            val byteRate = sampleRate * channels * bitsPerSample / 8

            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = (totalDataLen shr 8 and 0xff).toByte()
            header[6] = (totalDataLen shr 16 and 0xff).toByte()
            header[7] = (totalDataLen shr 24 and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()

            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = (sampleRate shr 8 and 0xff).toByte()
            header[26] = (sampleRate shr 16 and 0xff).toByte()
            header[27] = (sampleRate shr 24 and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = (byteRate shr 8 and 0xff).toByte()
            header[30] = (byteRate shr 16 and 0xff).toByte()
            header[31] = (byteRate shr 24 and 0xff).toByte()
            header[32] = (channels * bitsPerSample / 8).toByte()
            header[33] = 0
            header[34] = bitsPerSample.toByte()
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (dataLength and 0xff).toByte()
            header[41] = (dataLength shr 8 and 0xff).toByte()
            header[42] = (dataLength shr 16 and 0xff).toByte()
            header[43] = (dataLength shr 24 and 0xff).toByte()

            fos.write(header, 0, 44)

        }
    }
}