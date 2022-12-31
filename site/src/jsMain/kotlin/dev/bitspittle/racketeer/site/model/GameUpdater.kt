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

            var changed = false
            for (block in blocks) {
                if (ctx.runStateChangingAction { block() }) { changed = true }
            }

            if (changed) {
                events.emit(Event.GameStateUpdated(ctx))
            }
        }
    }
    fun runStateChangingAction(block: suspend () -> Unit) = runStateChangingActions(block)
}

