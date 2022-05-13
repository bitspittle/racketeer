package dev.bitspittle.racketeer.model.action

class ActionQueue {
    private val actionsToRun = mutableListOf<ActionGroup>()

    private class ActionGroup(
        val init: suspend () -> Unit,
        val tearDown: suspend () -> Unit,
        val actions: List<suspend () -> Unit>,
    )

    fun enqueue(init: suspend () -> Unit = {}, tearDown: suspend () -> Unit = {}, action: suspend () -> Unit) {
        enqueue(listOf(action), init, tearDown)
    }

    fun enqueue(actions: List<suspend () -> Unit>, init: suspend () -> Unit = {}, tearDown: suspend () -> Unit = {}) {
        actionsToRun.add(ActionGroup(init, tearDown, actions))
    }

    fun stop() {
        isRunning = false
        actionsToRun.clear()
    }

    suspend fun runEnqueuedActions() {
        if (isRunning) return
        isRunning = true
        try {
            while (actionsToRun.isNotEmpty()) {
                val group = actionsToRun.removeFirst()
                try {
                    group.init()
                    val innerActions = group.actions.toMutableList()
                    while (innerActions.isNotEmpty()) {
                        innerActions.removeFirst().invoke()
                        if (!isRunning) break // An action might have indirectly triggered `stop()`
                    }
                }
                finally {
                    group.tearDown()
                }
            }
        } finally {
            stop()
        }
    }

    var isRunning: Boolean = false
        private set
}
