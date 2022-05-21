package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.clear
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.popAll

class ConfirmDeleteUserDataView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Danger
            override val title = "Confirm"

            override val description = "Press ENTER if you're sure you want to delete ALL your user data (i.e. quick saves and card history)! Otherwise, go back!"

            override suspend fun invoke(): Boolean {
                ctx.settings.clear()
                ctx.userStats.clear()
                ctx.app.userDataDir.path.toFile().deleteRecursively()
                ctx.viewStack.popAll()
                ctx.viewStack.currentView.refreshCommands() // Ensure we hide "User data" if it was showing before
                return true
            }
        }
    )
}