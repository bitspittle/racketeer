package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.popAllViewsAndRefresh
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.game.GameState

class NewGameCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.ModifyAlt

    override val title = "New Game"

    override suspend fun invoke(): Boolean {
        ctx.state = GameState(ctx.data, ctx.cardQueue)
        ctx.viewStack.popAllViewsAndRefresh()
        return true
    }
}
