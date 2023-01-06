package dev.bitspittle.racketeer.site.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameUpdater(
    private val scope: CoroutineScope,
    private val events: Events,
    private val ctx: GameContext,
) {
    fun runStateChangingActions(vararg blocks: suspend () -> Unit) {
        scope.launch {
            ctx.logger.clear()

            try {
                var changed = false
                for (block in blocks) {
                    if (ctx.runStateChangingAction { block() }) {
                        changed = true
                    }
                }

                if (changed) {
                    events.emit(Event.GameStateUpdated(ctx))
                }
            } catch (ex: Exception) {
                ctx.logger.error(ex.message ?: "Code threw exception without a message: ${ex::class.simpleName}")
                events.emit(Event.GameStateUpdated(ctx)) // Emit an event so that the logging area gets updated
            }
        }
    }
    fun runStateChangingAction(block: suspend () -> Unit) = runStateChangingActions(block)
}

