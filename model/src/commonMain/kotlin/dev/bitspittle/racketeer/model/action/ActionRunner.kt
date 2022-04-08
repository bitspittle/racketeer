package dev.bitspittle.racketeer.model.action

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr

class ActionRunner(private val env: Environment) {
    var actionQueue: ActionQueue? = null
        private set

    suspend fun withActionQueue(block: suspend ActionQueue.() -> Unit) {
        require(actionQueue == null) { "Attempt to start running new actions while previous actions haven't finished yet. Use ActionQueue.enqueue instead."}
        actionQueue = ActionQueue(env)
        try {
            actionQueue!!.block()
        } finally {
            actionQueue = null
        }
    }
}