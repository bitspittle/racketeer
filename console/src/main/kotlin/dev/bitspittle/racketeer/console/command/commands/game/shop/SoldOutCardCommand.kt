package dev.bitspittle.racketeer.console.command.commands.game.shop

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command

class SoldOutCardCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.Disabled
    override val title = "SOLD OUT"
}

