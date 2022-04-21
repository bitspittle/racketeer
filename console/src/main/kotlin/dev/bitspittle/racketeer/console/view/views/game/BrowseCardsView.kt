package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardCommand
import dev.bitspittle.racketeer.model.card.Card

class BrowseCardsView(ctx: GameContext, private val cards: List<Card>) : GameView(ctx) {
    init {
        require(cards.isNotEmpty())
    }

    override fun createCommands(): List<Command> =
        cards.map { card -> ViewCardCommand(ctx, card) }
}