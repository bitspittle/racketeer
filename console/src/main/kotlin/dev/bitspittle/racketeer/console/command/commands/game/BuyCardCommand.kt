package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class BuyCardCommand(ctx: GameContext, private val card: Card, padding: Int) : Command(ctx) {
    override val type = if (ctx.state.cash >= card.template.cost) Type.Modify else Type.Disabled

    override val title = "Buy: ${ctx.describer.describe(card.template, padding, concise = true)}"

    override val description = ctx.describer.describe(card.template)

    override suspend fun invoke(): Boolean {
        ctx.state.cash -= card.template.cost
        ctx.state.move(card, ctx.state.discard)
        ctx.viewStack.currentView.refreshCommands()
        return true
    }
}

