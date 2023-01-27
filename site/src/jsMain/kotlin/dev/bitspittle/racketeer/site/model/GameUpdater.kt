package dev.bitspittle.racketeer.site.model

import dev.bitspittle.racketeer.site.components.util.Payload
import dev.bitspittle.racketeer.site.components.util.Uploads
import dev.bitspittle.racketeer.site.components.util.toPayloadMessage
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

                println(ex.toPayloadMessage())
                ex.printStackTrace()
            }
        }
    }
    fun runStateChangingAction(block: suspend () -> Unit) = runStateChangingActions(block)
}

