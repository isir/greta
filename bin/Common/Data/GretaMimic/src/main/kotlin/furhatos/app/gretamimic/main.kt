package furhatos.app.gretamimic

import furhatos.app.gretamimic.flow.Init
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill

class GretamimicSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}

