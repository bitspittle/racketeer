package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.random.CopyableRandom
import kotlin.io.path.deleteIfExists

class NewGameCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.Accented

    override val title = "New Game"

    override suspend fun invoke(): Boolean {
        UserDataSupport.pathForSlot(UserDataSupport.QUICKSAVE_SLOT).deleteIfExists()
        ctx.state = GameState(ctx.data, ctx.cardQueue, CopyableRandom())
        ctx.viewStack.popAll()
        ctx.viewStack.replaceView(PreDrawView(ctx))
        return true
    }
}
