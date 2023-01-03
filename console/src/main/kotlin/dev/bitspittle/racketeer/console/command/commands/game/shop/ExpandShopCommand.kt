package dev.bitspittle.racketeer.console.command.commands.game.shop

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange

class ExpandShopCommand(ctx: GameContext) : Command(ctx) {
    init {
        require(ctx.state.shop.tier < ctx.data.maxTier) { "Shop already at max tier - Expand Shop should be hidden" }
    }

    private val influenceCost = ctx.data.shopPrices[ctx.state.shop.tier]
    override val type = if (ctx.state.influence >= influenceCost) Type.Accented else Type.Disabled

    override val title = "Expand shop"
    override val extra = ctx.describer.describeInfluence(influenceCost)

    override val description = "Expand the shop, adding an additional card for sale and increasing the quality of cards that it sells."

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.addChange(GameStateChange.UpgradeShop())
            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, -influenceCost))
        }
        return true
    }
}
