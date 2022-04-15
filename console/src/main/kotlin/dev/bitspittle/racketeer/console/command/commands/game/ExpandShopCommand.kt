package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command

class ExpandShopCommand(ctx: GameContext) : Command(ctx) {
    init {
        require(ctx.state.shop.tier < ctx.data.maxTier) { "Shop already at max tier - Expand Shop should be hidden" }
    }

    private val influenceCost = ctx.data.shopPrices[ctx.state.shop.tier]
    override val type = if (ctx.state.influence >= influenceCost) Type.ModifyAlt else Type.Disabled

    override val title = "Expand shop"
    override val meta = ctx.describer.describeInfluence(influenceCost)

    override val description = "Expand the shop, adding an additional card for sale and increasing the quality of cards that it sells."

    override suspend fun invoke(): Boolean {
        return if (ctx.state.shop.upgrade()) {
            ctx.state.influence -= influenceCost
            ctx.viewStack.currentView.refreshCommands()
            true
        } else {
            false
        }
    }
}
