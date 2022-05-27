package dev.bitspittle.racketeer.console.view.views.game.choose

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.wrap
import dev.bitspittle.racketeer.console.view.View
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * A special case item chooser screen when you can't really make a choice, you can just review items you're getting.
 */
class ReviewItemsView(
    ctx: GameContext,
    prompt: String?,
    private val items: List<Any>,
    private val choices: Continuation<List<Any>?>,
    private val requiredChoice: Boolean,
) : View(ctx) {
    override val heading = buildString {
        if (prompt != null) {
            append(prompt)
        } else {
            if (items.size > 1) {
                append("Review all:")
            } else {
                append("Review:")
            }
        }
    }.wrap()
    override val allowEsc = !requiredChoice

    override fun createCommands(): List<Command> = items.map { item ->
        object : Command(ctx) {
            override val title = describeForTitle(item)
            override val extra = describeForExtra(item)
            override val description = describeForDescription(item)

            override suspend fun invoke(): Boolean {
                choices.resume(items)
                goBack()
                return false // Refresh will be handled by the parent screen
            }
        }
    }

    override fun onEscRequested() {
        choices.resume(null)
    }
}