package dev.bitspittle.racketeer.console.command.commands.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.admin.ChoosePileCardsView
import dev.bitspittle.racketeer.model.pile.Pile

class ChoosePileFromCommand(ctx: GameContext, private val pile: Pile) : Command(ctx) {
    override val type: Type = if (pile.cards.isEmpty()) Type.Disabled else Type.Normal
    override val title = ctx.describer.describePileTitle(ctx.state, pile, withSize = true)

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(ChoosePileCardsView(ctx, pile))
        return true
    }
}

