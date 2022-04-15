package dev.bitspittle.racketeer.console.command.commands.admin

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popPastAndRefresh
import dev.bitspittle.racketeer.console.view.views.admin.AdminMenuView
import dev.bitspittle.racketeer.model.card.CardTemplate

class CreateCardCommand(ctx: GameContext, private val card: CardTemplate) : Command(ctx) {
    override val type = Type.Warning
    override val title = ctx.describer.describe(card, concise = true)
    override val description = ctx.describer.describe(card, showCash = true)

    override suspend fun invoke(): Boolean {
        ctx.state.move(card.instantiate(), ctx.state.hand, ListStrategy.FRONT)
        ctx.viewStack.popPastAndRefresh { view -> view is AdminMenuView }
        return true
    }
}

