package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.popAllAndRefresh
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.random.CloneableRandom

class NewGameCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.ModifyAlt

    override val title = "New Game"

    override suspend fun invoke(): Boolean {
        ctx.state = GameState(ctx.data, ctx.cardQueue, CloneableRandom())
        ctx.viewStack.popAll()
        ctx.viewStack.replaceView(PreDrawView(ctx))
        return true
    }
}
