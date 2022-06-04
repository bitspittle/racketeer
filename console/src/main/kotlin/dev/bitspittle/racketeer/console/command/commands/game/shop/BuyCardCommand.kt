package dev.bitspittle.racketeer.console.command.commands.game.shop

import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange

class BuyCardCommand(ctx: GameContext, private val card: Card) : Command(ctx) {
    override val type = if (ctx.state.cash >= card.template.cost) Type.Emphasized else Type.Disabled

    override val title get() = "Buy: ${ctx.describer.describeCardTitle(card)}"

    override val extra = ctx.describer.describeCash(card.template.cost)
    override val description = ctx.describer.describeCardBody(card.template)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, -card.template.cost))
            ctx.state.apply(GameStateChange.MoveCard(card, ctx.state.street))
        }
        return true
    }

    override fun renderContentLowerInto(scope: RenderScope) {
        renderContentLowerInto(scope, card)
    }
}

