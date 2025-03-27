/*package furhatos.app.gretamimic.flow.main

import furhatos.gestures.ARKitParams.*
import furhatos.gestures.CharParams.*
import furhatos.gestures.BasicParams.*
import furhatos.gestures.defineGesture
import furhatos.gestures.Gestures

import furhatos.app.gretamimic.activemq.GretaHeadRotationReceiver
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.AzureVoice
import furhatos.gestures.ARKitParams
import furhatos.util.Language

import furhatos.records.Transcription

val TestState: State = state {

    onEntry {

        val input =
            "start=0.0 tm1=0.03 Hello, tm2=0.67691666 My name is Camille. tm3=2.125146 I am one of the virtual character tm4=3.8079584 of the Greta platform. tm5=5.0387917 end=5.53725"

        val regex = Regex("(start|end|tm\\d+)=(\\d+\\.\\d+)\\s*")
        val matches = regex.findAll(input)

        var previousTime = 0.0
        var lastIndex = 0
        matches.forEachIndexed { index, match ->
            val (marker, time) = match.destructured
            val markerIndex = input.indexOf(marker, lastIndex)
            val text = input.substring(lastIndex, markerIndex)
            val delay = (time.toDouble() - previousTime) * 1000 // Convert seconds to milliseconds
            println("$previousTime,  $time")
            previousTime = time.toDouble()

            lastIndex = markerIndex + marker.length + time.length + 1 // Update the lastIndex to exclude the current marker
            //println("$lastIndex, $markerIndex")
            if (text.isNotBlank()) {
                println(text.trim())
                /*furhat.say {
                    +text.trim() // Trim to remove any leading or trailing whitespace
                    if (index < matches.count() - 1) {
                        delay(delay.toLong())
                    }
                }*/
            }
        }

        val delai = 2000
        val delai_long = delai.toLong()
        //furhat.say("Welcome <break strength=\"medium\" /> to text-to-speech.", withVoice= AzureVoice(language= Language.ENGLISH_GB))
        furhat.activeFaceEngine()
        println("default")
        furhat.setCharacter("default")
        delay(delai_long)

        println("CustomCharacter")
        furhat.setCharacter("CustomCharacter")
        delay(delai_long)

        furhat.say {
            +"An elephant sounds like this"
            +Audio("classpath:actor1_good.wav", "ELEPHANT SOUND", speech = true)
        }

        //delay(delai_long)

        /*
        println("Bjorn")
        furhat.setCharacter("Bjorn")
        delay(delai_long)



        println("Isabel")
        furhat.setMask("adult","Isabel")
        delay(delai_long)

        println("Alex")
        furhat.setCharacter("Alex")
        delay(delai_long)

        println("Clown")
        furhat.setCharacter("Clown")*/


    }

//    onTime(repeat=3000) {
//        furhat.gesture(FaceArkits)
//        furhat.gesture(FaceChar)
//        furhat.gesture(FaceBasics)
//    }

}


val FaceArkits = defineGesture("FaceArkits") {
    frame(0.32, persist = false) {
        BROW_INNER_UP to 0.0
        BROW_OUTER_UP_LEFT to 0.0
        BROW_OUTER_UP_RIGHT to 0.0
        EYE_BLINK_LEFT to 0.0
        EYE_BLINK_RIGHT to 0.0
        EYE_LOOK_DOWN_LEFT to 0.0
        EYE_LOOK_DOWN_RIGHT to 0.0
        EYE_LOOK_IN_LEFT to 0.0
        EYE_LOOK_IN_RIGHT to 0.0
        EYE_LOOK_OUT_LEFT to 0.0
        EYE_LOOK_OUT_RIGHT to 0.0
        EYE_LOOK_UP_LEFT to 0.0
        EYE_LOOK_UP_RIGHT to 0.0
        ARKitParams.EYE_SQUINT_LEFT to 0.0
        ARKitParams.EYE_SQUINT_RIGHT to 0.0
        EYE_WIDE_LEFT to 0.0
        EYE_WIDE_RIGHT to 0.0
        CHEEK_PUFF to 0.0
        CHEEK_SQUINT_LEFT to 0.0
        CHEEK_SQUINT_RIGHT to 0.0
        JAW_FORWARD to 0.0
        JAW_LEFT to 0.0
        JAW_OPEN to 0.0
        JAW_RIGHT to 0.0
        NOSE_SNEER_LEFT to 0.0
        NOSE_SNEER_RIGHT to 0.0
    }
    reset(1.0)

}

val FaceChar = defineGesture("FaceChar") {
    frame(0.05, persist = false) {
        EYEBROW_DOWN to 0.0
        EYEBROW_LARGER to 0.0
        EYEBROW_NARROWER to 0.0
        EYEBROW_SMALLER to 0.0
        EYEBROW_TILT_DOWN to 0.0
        EYEBROW_TILT_UP to 0.0
        EYEBROW_UP to 0.0
        EYEBROW_WIDER to 0.0
        EYES_DOWN to 0.0
        EYES_NARROWER to 0.0
        EYES_SCALE_DOWN to 0.0
        EYES_SCALE_UP to 0.0
        EYES_TILT_DOWN to 0.0
        EYES_TILT_UP to 0.0
        EYES_UP to 0.0
        EYES_WIDER to 0.0
        MOUTH_DOWN to 0.0
        MOUTH_FLATTER to 0.0
        MOUTH_NARROWER to 0.0
        MOUTH_SCALE to 0.0
        MOUTH_UP to 0.0
        MOUTH_WIDER to 0.0
        LIP_BOTTOM_THICKER to 0.0
        LIP_BOTTOM_THINNER to 0.0
        LIP_TOP_THICKER to 0.0
        LIP_TOP_THINNER to 0.0
        CHEEK_BONES_DOWN to 0.0
        CHEEK_BONES_NARROWER to 0.0
        CHEEK_BONES_UP to 0.0
        CHEEK_BONES_WIDER to 0.0
        CHEEK_FULLER to 0.0
        CHEEK_THINNER to 0.0
        CHIN_DOWN to 0.0
        CHIN_NARROWER to 0.0
        CHIN_UP to 0.0
        CHIN_WIDER to 0.0
        NOSE_DOWN to 0.0
        NOSE_NARROWER to 0.0
        NOSE_UP to 0.0
        NOSE_WIDER to 0.0
    }
    reset(1.0)
}

val FaceBasics = defineGesture("FaceBasics") {
    frame(0.05, persist = false) {
        SMILE_CLOSED to 0.0
        SMILE_OPEN to 0.0
        SURPRISE to 0.0
        BLINK_LEFT to 0.0
        BLINK_RIGHT to 0.0
        ARKitParams.BROW_DOWN_LEFT to 0.0
        ARKitParams.BROW_DOWN_RIGHT to 0.0
        BROW_IN_LEFT to 0.0
        BROW_IN_RIGHT to 0.0
        BROW_UP_LEFT to 0.0
        BROW_UP_RIGHT to 0.0
        EPICANTHIC_FOLD to 0.0
        ARKitParams.EYE_SQUINT_LEFT to 0.0
        ARKitParams.EYE_SQUINT_RIGHT to 0.0
        LOOK_DOWN to 0.0
        LOOK_LEFT to 0.0
        LOOK_RIGHT to 0.0
        LOOK_UP to 0.0
        LOOK_DOWN_LEFT to 0.0
        LOOK_DOWN_RIGHT to 0.0
        LOOK_LEFT_LEFT to 0.0
        LOOK_LEFT_RIGHT to 0.0
        LOOK_RIGHT_LEFT to 0.0
        LOOK_RIGHT_RIGHT to 0.0
        LOOK_UP_LEFT to 0.0
        LOOK_UP_RIGHT to 0.0
        GAZE_PAN to 0.0
        GAZE_TILT to 0.0
    }
    reset(1.0)
}
*/