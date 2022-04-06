package dev.bitspittle.racketeer.model.card

/**
 * A meaningful group of cards, with a particular purpose (as opposed to just a random collection of cards)
 *
 * For example, the discard pile, or the deck.
 */
interface Pile {
    val cards: List<Card>
}

class MutablePile(override val cards: MutableList<Card> = mutableListOf()) : Pile {
    fun copy() = MutablePile(cards.map { it.copy() }.toMutableList())
}
