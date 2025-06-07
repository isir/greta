package furhatos.app.gretamimic.flow.main

import furhatos.app.gretamimic.activemq.AudioReceiver
import furhatos.gestures.ARKitParams.*
import furhatos.gestures.CharParams
import furhatos.gestures.BasicParams
import furhatos.gestures.defineGesture

import furhatos.app.gretamimic.activemq.GretaHeadRotationReceiver
import furhatos.app.gretamimic.activemq.GretaAUsReceiver
import furhatos.app.gretamimic.activemq.GretaSpeechTextReceiver
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.PollyNeuralVoice
import furhatos.flow.kotlin.voice.Voice
import furhatos.util.Language

val Idle: State = state {


    val brokerurl = "tcp://localhost:61616"
    val rotationTopicName = "greta.furhat.Rotation"
    val auTopicName = "greta.furhat.AUs" //"Furhat AUs Sender" //"greta.furhat.AUs"
    val speechTextTopicName = "greta.furhat.SpeechText"

    val rotationReceiver = GretaHeadRotationReceiver(brokerurl, rotationTopicName)
    val auReceiver = GretaAUsReceiver(brokerurl, auTopicName)
    val speechTextreceiver = GretaSpeechTextReceiver(brokerurl, speechTextTopicName)

    /*val audioreceiver = AudioReceiver(brokerurl, "greta.furhat.Audio")
    audioreceiver.filepath = "C:\\Users\\sanga\\Documents\\GitHub\\FurhatSkills\\GretaMimic\\src\\main\\resources\\"
    audioreceiver.audioFileName = "ingred_audio.wav"
    audioreceiver.phonemeFileName = "greta_phone.pho"*/

    var gretaTextToSay = ""

    var roll: Double? = 0.0
    var pitch:Double? = 0.0
    var yaw:Double? = 0.0

    onEntry {
        if (furhat.character!="CustomCharacter"){
        furhat.setCharacter("CustomCharacter")
        }

        /*furhat.say{
            +"An elephant sounds like this"
            +Audio("classpath:ingred_audio.wav", "ELEPHANT SOUND")
        }*/


        // Selects a female English voice, with a high pitch and 10% increase in speech rate.
        furhat.voice = PollyNeuralVoice.Camila()
        furhat.voice = Voice(language = Language.ENGLISH_GB, pitch = "medium", rate = 1.1)


        val rotationCallback: (Double?, Double?, Double?, Double?, String) -> Unit = { x, y, z, frameNum, event ->
            //println("$x $y $z at $frameNum")
            roll = x
            pitch = y
            yaw = z

            //myEventRaisingMethode(event)
            furhat.gesture(RotateHead1(roll, pitch, yaw))
        }

        val speechTextCallback: (String, String) -> Unit = { event, gretaText ->

            println("Received text, $gretaText")

            gretaTextToSay = gretaText
            myEventRaisingMethode(event)

            //furhat.say(gretaTextToSay)
        }

        var auArray: Array<Double> = emptyArray()

        val auCallback: (Array<Double>, Double?) -> Unit = {array, frame->
            println(array.contentToString())
            auArray = array

            furhat.gesture(FurhatFaceExpression(auArray))
            furhat.gesture(FurhatAU252627(auArray[48], auArray[50], auArray[52]))
        }

        speechTextreceiver.start(speechTextCallback)
        rotationReceiver.start(rotationCallback)
        auReceiver.start(auCallback)
        //audioreceiver.start()
    }


    onEvent("speech Text Received Event"){
        //furhat.say(gretaTextToSay)
        sayWithDelays(gretaTextToSay)
    }
}

fun FlowControlRunner.myEventRaisingMethode(customEvent: String) {
    raise(customEvent)
}

fun FlowControlRunner.sayWithDelays(sentence: String) {
    // Regex to split sentence into parts while keeping the punctuation marks
    val parts = sentence.split("(?=[,.])|(?<=[,.])".toRegex())

    for (part in parts) {
        when (part.trim()) {
            "," -> delay(180)
            "." -> delay(260)
            else -> furhat.say(part.trim())
        }
    }
}

fun RotateHead1(roll:Double?, pitch:Double?, yaw:Double?) =
    defineGesture("RotateHead") {
        if (roll!=null && pitch!=null && yaw!=null) {
            frame(0.05, persist = true) {
                BasicParams.NECK_TILT to roll
                BasicParams.NECK_PAN to pitch
                BasicParams.NECK_ROLL to yaw
            }
        }
    }

fun FurhatAU252627(au25:Double, au26:Double, au27:Double) =
    defineGesture("FurhatAU252627") {
        var value = 0.0
        var flag = false
        if (au27 != 0.0){
            value = au27
            flag = true
        }
        else if (au26 !=0.0){
            value = au26*0.6
            flag = true
        }
        else if (au25 !=0.0){
            // In this part AU25 will be a combination of AU16 to show the teeth and AU27 JAW_OPEN
            value = au25*0.25
            flag = false
            frame(0.05, persist = true) {
                JAW_OPEN to value
                MOUTH_UPPER_UP_LEFT to value // AU10
                MOUTH_UPPER_UP_RIGHT to value // AU10
            }
        }
        if (flag==true){
            frame(0.05, persist = true) {
                JAW_OPEN to value
            }
        }

    }

fun FurhatFaceExpression(auArray: Array<Double>) =
    defineGesture("FurhatFaceExpression") {
        frame(0.05, persist = true){
              BROW_INNER_UP to auArray[0] // AU1

              BROW_OUTER_UP_RIGHT to auArray[2] // AU2
              BROW_OUTER_UP_LEFT to auArray[3] // AU2

              BROW_DOWN_LEFT to auArray[6] // AU4
              BROW_DOWN_RIGHT to auArray[7] // AU4

              EYE_WIDE_LEFT to auArray[8] // AU5
              EYE_WIDE_RIGHT to auArray[9] // AU5

              CHEEK_SQUINT_LEFT to auArray[10] // AU6
              CHEEK_SQUINT_RIGHT to auArray[11] // AU6

              EYE_SQUINT_LEFT to auArray[12] // AU7
              EYE_SQUINT_RIGHT to auArray[13] // AU7

              MOUTH_UPPER_UP_LEFT to auArray[18] // AU10
              MOUTH_UPPER_UP_RIGHT to auArray[19] // AU10

              MOUTH_SMILE_LEFT to auArray[22] // AU12
              MOUTH_SMILE_RIGHT to auArray[23] // AU12
              //
              MOUTH_DIMPLE_LEFT to auArray[26] // AU14
              MOUTH_DIMPLE_RIGHT to auArray[27] // AU14
              //
              MOUTH_FROWN_LEFT to (auArray[28]?:0.0) // AU15
              MOUTH_FROWN_RIGHT to (auArray[29]?:0.0) // AU15
              //
              MOUTH_LOWER_DOWN_LEFT to (auArray[30]?:0.0) // AU16
              MOUTH_LOWER_DOWN_RIGHT to (auArray[31]?:0.0) // AU16
              //
              MOUTH_SHRUG_LOWER to auArray[32] // AU17
              //
              MOUTH_PUCKER to auArray[34] // AU18
//
              MOUTH_STRETCH_LEFT to auArray[38] // AU20
              MOUTH_STRETCH_RIGHT to auArray[39] // AU20
//
              MOUTH_FUNNEL to auArray[42] // AU22
//
              CharParams.MOUTH_NARROWER to auArray[44] // AU23
//
              MOUTH_PRESS_LEFT to auArray[46] // AU24
              MOUTH_PRESS_RIGHT to auArray[47] // AU24
//
              // JAW_FORWARD to auArray[48] // AU25
//
              // CharParams.LIP_BOTTOM_THICKER to auArray[50] // AU26
//
              //JAW_OPEN to auArray[52] // AU27
              //MOUTH_ROLL_UPPER to auArray[53] // AU27
//
              EYE_BLINK_LEFT to auArray[84] // AU43
              EYE_BLINK_RIGHT to auArray[85] // AU43
//
              EYE_LOOK_IN_LEFT to auArray[120] // AU61
//
              EYE_LOOK_IN_RIGHT to auArray[122] // AU62
//
              CharParams.EYES_UP to auArray[124] // AU63

              CharParams.EYES_DOWN to auArray[126] // AU64
            }

        }