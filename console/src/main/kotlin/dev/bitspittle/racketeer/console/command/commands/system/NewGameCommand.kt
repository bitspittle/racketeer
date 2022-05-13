package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.play.PreDrawView
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.model.random.CopyableRandom
import kotlin.io.path.deleteIfExists

class NewGameCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.Accented

    override val title = "New Game"

    override suspend fun invoke(): Boolean {
        ctx.app.userData.pathForSlot(UserData.QUICKSAVE_SLOT).deleteIfExists()
        ctx.state = MutableGameState(ctx.data, ctx.enqueuers, CopyableRandom())
        ctx.app.uploadService.clearThrottles()
        ctx.viewStack.popAll()
        ctx.viewStack.replaceView(PreDrawView(ctx))
        return true
    }
}
