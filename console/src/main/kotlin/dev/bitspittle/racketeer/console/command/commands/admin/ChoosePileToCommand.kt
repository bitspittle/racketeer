package dev.bitspittle.racketeer.console.command.commands.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.popPast
import dev.bitspittle.racketeer.console.view.views.admin.AdminMenuView
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.pile.Pile

class ChoosePileToCommand(
    ctx: GameContext,
    private val pile: Pile,
    private val cards: List<Card>,
    forceDisabled: Boolean = false
) : Command(ctx) {
    override val type: Type = if (forceDisabled) Type.Disabled else Type.Warning
    override val title = ctx.describer.describePileTitle(ctx.state, pile, withSize = true)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.addChange(GameStateChange.MoveCards(ctx.state, cards, pile))
            ctx.viewStack.popPast { view -> view is AdminMenuView }
        }
        return true
    }
}

