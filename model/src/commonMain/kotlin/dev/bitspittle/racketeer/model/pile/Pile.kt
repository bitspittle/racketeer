package dev.bitspittle.racketeer.model.pile

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import dev.bitspittle.racketeer.model.card.Card

/**
 * A meaningful group of cards, with a particular purpose (as opposed to just a random collection of cards)
 *
 * For example, the discard pile, or the deck.
 */
interface Pile {
    val id: Uuid
    val cards: List<Card>
}

class MutablePile private constructor(
    override val id: Uuid, override val cards: MutableList<Card>
) : Pile {
    constructor(cards: MutableList<Card> = mutableListOf()) : this(uuid4(), cards)
    fun copy() = MutablePile(id, cards.map { it.copy() }.toMutableList())
}
