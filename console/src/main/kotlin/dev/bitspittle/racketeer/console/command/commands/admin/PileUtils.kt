package dev.bitspittle.racketeer.console.command.commands.admin

import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.game.GameState

val GameState.allPiles get() = listOf(hand, deck, street, discard, jail)

fun Pile.toTitle(state: GameState): String {
    return when (this) {
        state.hand -> "Hand"
        state.deck -> "Deck"
        state.street -> "Street"
        state.discard -> "Discard"
        state.jail -> "Jail"
        else -> error("Unknown pile")
    }
}