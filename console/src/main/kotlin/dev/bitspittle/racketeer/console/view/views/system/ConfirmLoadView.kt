package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.ConfirmLoadCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class ConfirmLoadView(ctx: GameContext, private val slot: Int) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(ConfirmLoadCommand(ctx, slot))
}