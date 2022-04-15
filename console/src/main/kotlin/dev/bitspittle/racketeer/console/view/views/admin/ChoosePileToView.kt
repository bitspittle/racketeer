package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.admin.ChoosePileToCommand
import dev.bitspittle.racketeer.console.command.commands.admin.allPiles
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.Pile

class ChoosePileToView(ctx: GameContext, private val cards: List<Card>, private val disallowedPile: Pile? = null) : View(ctx) {
    override val heading = "Choose a pile to move your selected cards to."

    override fun createCommands(): List<Command> =
        ctx.state.allPiles.map { pile -> ChoosePileToCommand(ctx, pile, cards, forceDisabled = (pile == disallowedPile)) }
}