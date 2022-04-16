package dev.bitspittle.racketeer.console.command.commands.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.popPast
import dev.bitspittle.racketeer.console.view.views.admin.AdminMenuView
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.Pile

class ChoosePileToCommand(
    ctx: GameContext,
    private val pile: Pile,
    private val cards: List<Card>,
    forceDisabled: Boolean = false
) : Command(ctx) {
    override val type: Type = if (forceDisabled) Type.Disabled else Type.Warning
    override val title = ctx.describer.describe(ctx.state, pile)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.move(cards, pile)
            ctx.viewStack.popPast { view -> view is AdminMenuView }
        }
        return true
    }
}

