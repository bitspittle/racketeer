package dev.bitspittle.racketeer.console.command.commands.game.play

import dev.bitspittle.limp.utils.ifTrue
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.shop.VisitShopView
import dev.bitspittle.racketeer.model.shop.Shop

fun Shop.canAffordSomething(ctx: GameContext): Boolean {
    return (
            ctx.state.cash >= (stock.filterNotNull().minOfOrNull { it.template.cost } ?: Int.MAX_VALUE) ||
                    (ctx.state.shop.tier < ctx.data.maxTier && ctx.state.influence >= ctx.data.shopPrices[ctx.state.shop.tier])
            )
}

class VisitShopCommand(ctx: GameContext) : Command(ctx) {
    private val expansionCost = (ctx.state.shop.tier < ctx.data.maxTier).ifTrue { ctx.data.shopPrices[ctx.state.shop.tier] }

    override val type = if (ctx.state.shop.canAffordSomething(ctx)) Type.Emphasized else Type.Normal

    override val title = "Visit shop [Tier ${ctx.state.shop.tier + 1}]"
    override val extra = expansionCost?.let { "(${ctx.describer.describeInfluence(expansionCost)})" }
    override val description = buildString {
        append("Look over the cards in the shop, or take other relevant actions.")
        expansionCost?.takeIf { it > ctx.state.influence }?.let { expansionCost ->
            appendLine()
            appendLine()
            append("You will need an additional ${ctx.describer.describeInfluence(expansionCost - ctx.state.influence)} to expand it.")
        }
    }

    override suspend fun invoke(): Boolean {
        ctx.viewStack.popAll() // Blueprints should always be anchored to the top level
        ctx.viewStack.pushView(VisitShopView(ctx))
        return true
    }
}

