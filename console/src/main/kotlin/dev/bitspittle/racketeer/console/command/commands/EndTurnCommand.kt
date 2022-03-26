package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class EndTurnCommand(ctx: GameContext) : Command {
    override val title = "End turn"

    override val description = "Finish this turn. Any leftover money will be discarded and the shop restocked with random items."
}

