package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseShopView
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

class BuyCardCommand(ctx: GameContext, private val card: Card) : Command(ctx) {
    override val title = "Buy: ${ctx.describer.describe(card.template, concise = true)}"

    override val description = ctx.describer.describe(card.template)

    override suspend fun invoke(): Boolean {
        return if (ctx.state.cash >= card.template.cost) {
            ctx.state.cash -= card.template.cost
            ctx.state.move(card, ctx.state.discard)
            ctx.viewStack.replaceView(BrowseShopView(ctx))
            true
        } else {
            false
        }
    }
}

