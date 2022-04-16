package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.card.Card

class BuyCardCommand(ctx: GameContext, private val card: Card) : Command(ctx) {
    override val type = if (ctx.state.cash >= card.template.cost) Type.Modify else Type.Disabled

    override val title = "Buy: ${ctx.describer.describe(card.template, concise = true)}"
    override val meta = ctx.describer.describeCash(card.template.cost)
    override val description = ctx.describer.describe(card.template)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.cash -= card.template.cost
            ctx.state.move(card, ctx.state.discard)
        }
        return true
    }
}

