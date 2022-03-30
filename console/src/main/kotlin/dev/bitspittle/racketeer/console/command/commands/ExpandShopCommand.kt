package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command

class ExpandShopCommand(ctx: GameContext) : Command(ctx) {
    init {
        require(ctx.state.shopTier < 4) { "Shop already at max tier - Expand Shop item should be hidden" }
    }
    override val title = "Expand shop ${ctx.describers.describeInfluence(ctx.data.shopPrices[ctx.state.shopTier + 1])}"

    override val description = "Expand the shop, adding an additional card for sale and increasing the quality of cards that it sells."
}
