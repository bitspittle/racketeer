package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.views.game.ConfirmEndTurnView
import dev.bitspittle.racketeer.console.view.views.game.GameSummaryView
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView

class EndTurnCommand(ctx: GameContext, private val showConfirmationIfNecessary: Boolean = true) : Command(ctx) {
    override val type = Type.Accented

    override val title = "End turn"

    override val description =
        "Finish this turn. Cards in your hand and the street will move to the discard pile, any leftover money will be" +
                "lost, and the shop will get restocked with random items."

    private fun canStillBuyStuff() = run {
        ctx.state.shop.stock.filterNotNull().any { it.template.cost <= ctx.state.cash }
    }

    override suspend fun invoke(): Boolean {
        if (showConfirmationIfNecessary && canStillBuyStuff()) {
            ctx.viewStack.pushView(ConfirmEndTurnView(ctx))
        } else {
            ctx.runStateChangingAction {
                if (ctx.state.endTurn()) {
                    ctx.viewStack.replaceView(PreDrawView(ctx))
                } else {
                    ctx.viewStack.replaceView(GameSummaryView(ctx))
                }
            }
        }
        return true
    }
}

