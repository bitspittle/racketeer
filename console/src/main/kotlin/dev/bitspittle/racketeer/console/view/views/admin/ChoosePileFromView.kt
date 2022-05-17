package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.admin.ChoosePileFromCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.allPiles

class ChoosePileFromView(ctx: GameContext) : View(ctx) {
    override val heading = "Choose a pile to move one or more cards from."

    override fun createCommands(): List<Command> = ctx.state.allPiles.map { pile -> ChoosePileFromCommand(ctx, pile) }.toList()
}