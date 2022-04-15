package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command

class MaxedOutShopCommand(ctx: GameContext) : Command(ctx) {
    override val type = Type.Disabled
    override val title = "Expand shop"
    override val meta = "MAX"

    override val description = "Your shop is maxed out and cannot be upgraded further"
}
