package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.system.UserDataView
import kotlin.io.path.exists

class UserDataCommand(ctx: GameContext) : Command(ctx) {
    override val type get() = if (ctx.app.userDataDir.path.exists()) Type.Normal else Type.Hidden
    override val title = "User data"
    override val description: String = "Go to a menu for interacting with your saved user data."
    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(UserDataView(ctx))
        return true
    }
}
