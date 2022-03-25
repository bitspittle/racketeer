package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class ViewCardCommand(card: Card) : Command {
    override val title = card.toString()

//    override fun runPrimary(gameState: GameState): ActionGroup? {
//        return null
//    }
}

