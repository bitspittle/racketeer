package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command

class ConfirmQuitCommand(private val ctx: GameContext) : Command {
    override val title = "Quit"

    override val description = "Press ENTER if you're sure you want to quit. Otherwise, go back!"

    override fun invoke() {
        ctx.quit()
    }
}

