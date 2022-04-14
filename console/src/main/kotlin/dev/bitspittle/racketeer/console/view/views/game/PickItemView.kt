package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A special case item chooser screen when you only have to pick one.
 */
class PickItemView(
    ctx: GameContext,
    prompt: String?,
    private val items: List<Any>,
    private val choices: Continuation<List<Any>>
) : View(ctx) {
    override val heading = (prompt ?: "Choose 1 item:")

    override fun createCommands(): List<Command> = items.map { item ->
        object : Command(ctx) {
            override val title = describeForTitle(item)
            override val description = describeForDescription(item)

            override suspend fun invoke(): Boolean {
                choices.resume(listOf(item))
                goBack()
                return false // Refresh will be handled by the parent screen
            }
        }
    }

    override fun onEscRequested() {
        choices.resumeWithException(CancelPlayException("User canceled the play by rejecting a required choice."))
    }
}