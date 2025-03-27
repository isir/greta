package furhatos.app.gretamimic.flow

import furhatos.app.gretamimic.flow.main.TestState1
import furhatos.app.gretamimic.flow.main.Idle
//import furhatos.app.gretamimic.flow.main.Greeting

//import furhatos.app.gretamimic.setting.DISTANCE_TO_ENGAGE
//import furhatos.app.gretamimic.setting.MAX_NUMBER_OF_USERS
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state
import furhatos.flow.kotlin.users

val Init: State = state {

    onEntry {
        /** start interaction */
        when {
            furhat.isVirtual() -> goto(Idle) // Convenient to bypass the need for user when running Virtual Furhat
            users.hasAny() -> {
                //furhat.attend(users.random)
                goto(Idle)
            }
            else -> goto(Idle)
        }
    }

}
