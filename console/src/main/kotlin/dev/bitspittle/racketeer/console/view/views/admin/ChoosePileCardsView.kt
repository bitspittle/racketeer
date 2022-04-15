package dev.bitspittle.racketeer.console.view.views.admin

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.SelectItemCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.Pile

class ChoosePileCardsView(ctx: GameContext, private val pile: Pile) : View(ctx) {
    override val heading = "Choose one or more cards to move."

    private val selectCardCommands = pile.cards.map { card -> SelectItemCommand(ctx, card) }
    private fun hasUserSelectedEnoughChoices() = selectCardCommands.count { it.selected } > 0

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key == Keys.SPACE) {
            (currCommand as? SelectItemCommand)?.invoke()?.let { true } ?: false
        } else {
            false
        }
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
