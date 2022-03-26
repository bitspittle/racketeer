package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command

class DrawCardsCommand(ctx: GameContext) : Command {
    override val title = "Draw cards"

    override val description = "Draw ${ctx.state.handSize} cards and put them into your hand."
}

