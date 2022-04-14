package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.SelectItemCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ChooseItemsView(
    ctx: GameContext,
    prompt: String?,
    private val items: List<Any>,
    private val range: IntRange,
    private val choices: Continuation<List<Any>>
) : View(ctx) {
    override val heading = (prompt ?: "Choose ${ctx.describer.describeRange(range)} item(s):")

    private val selectItemCommands = items.map { item -> SelectItemCommand(ctx, item) }

    override fun createCommands(): List<Command> =
         selectItemCommands + object : Command(ctx) {
            override val title: String = "Confirm"
            override val description: String
                get() = if (hasUserSelectedEnoughChoices()) {
                    "Press ENTER to confirm the above choice(s)."
                } else {
                    "You must choose ${ctx.describer.describeRange(range)} item(s) before you can confirm."
                }

             override suspend fun invoke(): Boolean {
                 if (hasUserSelectedEnoughChoices()) {
                     choices.resume(items.filterIndexed { index, _    -> selectItemCommands[index].selected })
                     goBack()
                 }

                 return false // Refresh will be handled by the parent screen
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

    override fun onEscRequested() {
        choices.resumeWithException(CancelPlayException("User canceled the play by rejecting a required choice."))
    }
}