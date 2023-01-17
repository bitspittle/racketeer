package dev.bitspittle.racketeer.site.model

import dev.bitspittle.racketeer.site.components.util.Payload
import dev.bitspittle.racketeer.site.components.util.Uploads
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
                launch { Uploads.upload(Payload.Crash(ctx, ex)) }
                events.emit(Event.GameStateUpdated(ctx)) // Emit an event so that the logging area gets updated
            }
        }
    }
    fun runStateChangingAction(block: suspend () -> Unit) = runStateChangingActions(block)
}

