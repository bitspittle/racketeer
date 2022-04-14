package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class OptionsMenuView(ctx: GameContext) : View(ctx) {
    override val title: String = "Options"

    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Quit"
                override val description: String = "End this game. You will have a chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmQuitView(ctx))
                    return true
                }
            }
        )
}