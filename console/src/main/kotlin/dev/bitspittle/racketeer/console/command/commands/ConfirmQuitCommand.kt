package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command

class ConfirmQuitCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Confirm"

    override val description = "Press ENTER if you're sure you want to quit. Otherwise, go back!"

    override fun invoke() {
        ctx.quit()
    }
}

