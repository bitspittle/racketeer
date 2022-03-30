package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.PreDrawView
import dev.bitspittle.racketeer.model.game.GameState

class NewGameCommand(ctx: GameContext) : Command(ctx) {
    override val title = "New Game"

    override fun invoke(): Boolean {
        check(!ctx.viewStack.canGoBack)
        ctx.state = GameState(ctx.data)
        ctx.viewStack.replaceView(PreDrawView(ctx))
        return true
    }
}
