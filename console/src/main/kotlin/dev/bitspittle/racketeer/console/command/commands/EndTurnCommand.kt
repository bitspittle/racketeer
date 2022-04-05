package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.GameOverView
import dev.bitspittle.racketeer.console.view.views.PreDrawView

class EndTurnCommand(ctx: GameContext) : Command(ctx) {
    override val title = "End turn"

    override val description =
        "Finish this turn. Any leftover money will be discarded and the shop restocked with random items."

    override fun invoke(): Boolean {
        if (ctx.state.endTurn()) {
            ctx.viewStack.replaceView(PreDrawView(ctx))
        } else {
            ctx.viewStack.replaceView(GameOverView(ctx))
        }
        return true
    }
}

