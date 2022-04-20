package dev.bitspittle.racketeer.console.view.views.admin

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.SelectItemCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.pile.Pile

class ChoosePileCardsView(ctx: GameContext, private val pile: Pile) : View(ctx) {
    override val heading = "Choose one or more cards to move."

    private val selectCardCommands = pile.cards.map { card -> SelectItemCommand(ctx, card) }
    private fun hasUserSelectedEnoughChoices() = selectCardCommands.count { it.selected } > 0

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return when (key) {
            Keys.SPACE -> (currCommand as? SelectItemCommand)?.invoke()?.let { true } ?: false
            Keys.A -> {
                if (selectCardCommands.any { !it.selected }) {
                    selectCardCommands.forEach { it.selected = true }
                } else {
                    selectCardCommands.forEach { it.selected = false }
                }
                true
            }
            else -> false
        }
    }

    override fun RenderScope.renderFooter() {
        text("Press "); cyan { text("SPACE") }; textLine(" to toggle individual selections.")
        text("Press "); cyan { text("A") }; textLine(" to toggle all selections.")
    }

    override fun createCommands(): List<Command> =
        selectCardCommands + object : Command(ctx) {
            override val type: Type get() = if (hasUserSelectedEnoughChoices()) Type.Read else Type.Disabled
            override val title: String = "Confirm"
            override val description: String
                get() = if (hasUserSelectedEnoughChoices()) {
                    "Press ENTER to confirm the above choice(s)."
                } else {
                    "You must choose at least one card to continue."
                }

            override suspend fun invoke(): Boolean {
                ctx.viewStack.pushView(
                    ChoosePileToView(
                        ctx,
                        selectCardCommands.filter { it.selected }.map { it.item as Card },
                        pile
                    )
                )
                return false
            }
        }
}
