package dev.bitspittle.racketeer.console.view.views.game.choose

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.wrap
import dev.bitspittle.racketeer.console.view.View
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * A special case item chooser screen when you only have to pick one.
 */
class PickItemView(
    ctx: GameContext,
    prompt: String?,
    private val items: List<Any>,
    private val choices: Continuation<List<Any>?>,
    private val requiredChoice: Boolean,
) : View(ctx) {
    override val heading = buildString {
        if (prompt != null) {
            append(prompt)
            append(' ')
        }
        append("Choose 1:")
    }.wrap()
    override val allowEsc = !requiredChoice

    override fun createCommands(): List<Command> = items.map { item ->
        object : Command(ctx) {
            override val title = describeForTitle(item)
            override val extra = describeForExtra(item)
            override val description = describeForDescription(item)

            override suspend fun invoke(): Boolean {
                choices.resume(listOf(item))
                goBack()
                return false // Refresh will be handled by the parent screen
            }

            override fun renderContentLowerInto(scope: RenderScope) {
                renderContentLowerInto(scope, item)
            }
        }
    }

    override fun MainRenderScope.renderContentUpper() {
        if (requiredChoice) yellow { textLine("This choice is not optional, so you cannot back out of it."); textLine() }
    }

    override fun onEscRequested() {
        choices.resume(null)
    }
}