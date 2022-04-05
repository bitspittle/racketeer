package dev.bitspittle.racketeer.model.card

interface Pile {
    val cards: List<Card>
}

/**
 * A "named" list of cards, with a particular purpose (as opposed to just a collection of cards)
 */
class MutablePile(override val cards: MutableList<Card> = mutableListOf()) : Pile {
    fun copy() = MutablePile(cards.toMutableList())
}
