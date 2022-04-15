package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.scripting.types.CancelPlayException

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
        val prevState = ctx.state
        ctx.state = prevState.copy()

        return try {
            if (ctx.state.shop.upgrade()) {
                ctx.state.influence -= influenceCost
                ctx.viewStack.currentView.refreshCommands()
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            // This should only happen in dev mode, but a bad upgrade can leave the game in a broken state,
            // so let's restore the last good snapshot
            ctx.state = prevState
            throw ex
        }
    }
}
