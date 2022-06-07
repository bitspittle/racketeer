package dev.bitspittle.racketeer.console.command.commands.game.shop

import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.shop.priceFor

class BuyCardCommand(ctx: GameContext, private val card: Card) : Command(ctx) {
    private val price = ctx.state.shop.priceFor(card)
    override val type = if (ctx.state.cash >= price) Type.Emphasized else Type.Disabled

    override val title get() = "Buy: ${ctx.describer.describeCardTitle(card)}"

    override val extra = ctx.describer.describeCash(price)
    override val description = ctx.describer.describeCardBody(card.template)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            if (price > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, -price))
            }
            ctx.state.apply(GameStateChange.MoveCard(card, ctx.state.street))
        }
        return true
    }

    override fun renderContentLowerInto(scope: RenderScope) {
        renderContentLowerInto(scope, card)
    }
}

