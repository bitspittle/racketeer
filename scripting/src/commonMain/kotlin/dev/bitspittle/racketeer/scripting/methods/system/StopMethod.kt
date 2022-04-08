package dev.bitspittle.racketeer.scripting.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.action.ActionQueue

class StopMethod(private val getActionQueue: () -> ActionQueue?) : Method("stop!", 0) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val actionQueue =
            getActionQueue() ?: throw IllegalStateException("ActionQueue should exist while running cards")
        actionQueue.clear()

        return Unit
    }
}