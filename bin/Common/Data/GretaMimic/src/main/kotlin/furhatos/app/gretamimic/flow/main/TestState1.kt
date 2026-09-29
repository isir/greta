package furhatos.app.gretamimic.flow.main

import furhatos.app.gretamimic.activemq.*
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.PollyNeuralVoice
import furhatos.flow.kotlin.voice.Voice
import furhatos.gestures.ARKitParams
import furhatos.gestures.ARKitParams.*
import furhatos.gestures.BasicParams.*
import furhatos.gestures.CharParams.*
import furhatos.gestures.defineGesture
import furhatos.util.Gender
import furhatos.util.Language

val TestState1: State = state {

    val brokerurl = "tcp://localhost:61616"
    val speechTextTopicName = "greta.furhat.SpeechText"

    val speechTextreceiver = GretaSpeechTextReceiver(brokerurl, speechTextTopicName)
    var textToSay = ""
    var gretaTextToSay = ""


    onEntry {

        furhat.voice = PollyNeuralVoice.Camila()
        furhat.voice = Voice(language = Language.ENGLISH_GB, pitch = "medium", rate = 0.93)

        val speechTextCallback: (String, String) -> Unit = { event, gretaText ->
            println("hello, $gretaText")
            gretaTextToSay = gretaText
            //myEventRaisingMethode(event)

            furhat.say(gretaTextToSay)
        }
        speechTextreceiver.start(speechTextCallback)
    }



    onEvent("speech Text Received Event"){
        furhat.say(textToSay)
    }
}

fun FlowControlRunner.myEventRaisingMethod(customEvent: String) {
    raise(customEvent)
}
