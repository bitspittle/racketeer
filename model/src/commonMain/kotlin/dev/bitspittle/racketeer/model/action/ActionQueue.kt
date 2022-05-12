package dev.bitspittle.racketeer.model.action

class ActionQueue {
    private val actionsToRun = mutableListOf<suspend () -> Unit>()

    fun enqueue(action: suspend () -> Unit) {
        actionsToRun.add(action)
    }

    fun clear() {
        actionsToRun.clear()
    }

    suspend fun runEnqueuedActions() {
        if (isRunning) return
        isRunning = true
        try {
            while (actionsToRun.isNotEmpty()) {
                actionsToRun.removeFirst().invoke()
            }
        } finally {
            actionsToRun.clear()
            isRunning = false
        }
    }

    var isRunning: Boolean = false
        private set
}
