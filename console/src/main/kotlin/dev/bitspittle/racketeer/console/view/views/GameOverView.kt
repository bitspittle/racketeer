package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.*
import dev.bitspittle.racketeer.console.view.View

class GameOverView(ctx: GameContext) : View(ctx) {
    override val allowQuit = false

    override val commands: List<Command> =
        listOf(
            NewGameCommand(ctx),
            object : Command(ctx) {
                override val title = "Exit"
                override fun invoke() {
                    ctx.quit()
                }
            }
        )
}