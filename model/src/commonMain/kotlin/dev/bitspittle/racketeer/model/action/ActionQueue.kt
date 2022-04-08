package dev.bitspittle.racketeer.model.action

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr

class ActionQueue(private val env: Environment) {
    private val actionsToRun = mutableListOf<Expr>()
    private var isRunning = false

    val size: Int get() = actionsToRun.size

    fun enqueue(actions: Iterable<Expr>) {
        actionsToRun.addAll(actions)
    }

    fun enqueue(action: Expr) = enqueue(listOf(action))

    fun clear() = actionsToRun.clear()

    suspend fun start() {
        if (isRunning) throw IllegalStateException("Attempting to start an action queue that's already running")
        isRunning = true
        try {
            val evaluator = Evaluator()
            while (actionsToRun.isNotEmpty()) {
                val actionToRun = actionsToRun.removeFirst()
                evaluator.evaluate(env, actionToRun)
            }
        }
        finally {
            isRunning = false
        }
    }
}