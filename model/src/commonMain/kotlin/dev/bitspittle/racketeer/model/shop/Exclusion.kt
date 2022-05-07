package dev.bitspittle.racketeer.model.shop

import dev.bitspittle.racketeer.model.card.CardTemplate

class Exclusion(val expr: String, val action: suspend (CardTemplate) -> Boolean) {
    suspend operator fun invoke(card: CardTemplate) = action.invoke(card)
}