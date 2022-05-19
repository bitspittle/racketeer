package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.system.ChooseFeaturesView
import kotlin.io.path.deleteIfExists

class NewGameCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.Accented

    override val title = "New Game"

    override suspend fun invoke(): Boolean {
        ctx.app.userDataDir.pathForSlot(UserDataDir.QUICKSAVE_SLOT).deleteIfExists()
        ctx.app.cloudFileService.clearThrottles()
        ChooseFeaturesView.enter(ctx)
        return true
    }
}
