package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.CardTemplate

class SoldOutCardCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.Disabled
    override val title = "SOLD OUT"
}

