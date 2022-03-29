package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command

/**
 * A no-op command useful for indicating an empty pile.
 */
class EmptyPileCommand(ctx: GameContext) : Command(ctx) {
    override val title = "(Empty pile)"
    override val description = "The current pile of cards you wanted to inspect is empty."
}
