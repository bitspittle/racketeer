package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command

class ConfirmChoicesCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Confirm"
}

