package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.Card

class Effect(val expr: String, val desc: String, val action: suspend (Card) -> Unit) {
    suspend fun invoke(card: Card) = action.invoke(card)
}