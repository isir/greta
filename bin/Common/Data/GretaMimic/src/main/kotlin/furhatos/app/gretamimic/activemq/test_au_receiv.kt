package furhatos.app.gretamimic.activemq

fun main(args: Array<String>){


    val brokerurl = "tcp://localhost:61616"
    val auTopicName = "helloTopic"

    val auReceiver = GretaAUsReceiver(brokerurl, auTopicName)

    var auArray: Array<Double> = emptyArray()

    val auCallback: (Array<Double>, Double?) -> Unit = {array, frame->
        println(array.contentToString())
        auArray = array
     }

    auReceiver.start(auCallback)

}