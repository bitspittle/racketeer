package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.SelectItemCommand
import dev.bitspittle.racketeer.console.game.GameContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ChooseItemsView(
    ctx: GameContext,
    prompt: String?,
    private val items: List<Any>,
    private val range: IntRange,
    private val choices: Continuation<List<Any>?>,
    private val requiredChoice: Boolean,
) : GameView(ctx) {
    override val heading = (prompt ?: "Choose ${ctx.describer.describeRange(range)} item(s):")
    override val allowEsc = !requiredChoice

    private val selectItemCommands = items.map { item -> SelectItemCommand(ctx, item) }

    override fun createCommands(): List<Command> =
         selectItemCommands + object : Command(ctx) {
             override val type: Type get() = if (hasUserSelectedEnoughChoices()) Type.Normal else Type.Disabled
             override val title: String = "Confirm"
            override val description: String
                get() = if (hasUserSelectedEnoughChoices()) {
                    "Press ENTER to confirm the above choice(s)."
                } else {
                    "You must choose ${ctx.describer.describeRange(range)} item(s) before you can confirm."
                }

             override suspend fun invoke(): Boolean {
                 choices.resume(items.filterIndexed { index, _    -> selectItemCommands[index].selected })
                 goBack()

                 return true
             }
         }

    private fun hasUserSelectedEnoughChoices() = selectItemCommands.count { it.selected } in range

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key == Keys.SPACE) {
            (currCommand as? SelectItemCommand)?.invoke()?.let { true } ?: false
        } else {
            false
        }
    }

    override fun MainRenderScope.renderContentUpper() {
        if (requiredChoice) yellow { textLine("This choice is not optional, so you cannot back out of it."); textLine() }
    }

    override fun RenderScope.renderUpperFooter() {
        if (currCommand is SelectItemCommand) {
            text("Press "); cyan { text("SPACE") }; textLine(" to toggle the selected item.")
        }
    }

    override fun onEscRequested() {
        choices.resume(null)
    }
}