package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.VisitShopView

class VisitShopCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Visit shop (Tier ${ctx.state.shop.tier + 1})"

    override val description = "Look over the cards in the shop, or take other relevant actions."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(VisitShopView(ctx))
        return true
    }
}

