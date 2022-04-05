package dev.bitspittle.racketeer.model.card

import dev.bitspittle.limp.types.ListStrategy
import kotlin.random.Random

fun MutableList<Card>.insert(cards: List<Card>, listStrategy: ListStrategy, random: Random = Random.Default) {
    when (listStrategy) {
        ListStrategy.FRONT -> cards.forEachIndexed { i, card -> this.add(i, card) }
        ListStrategy.BACK -> cards.forEach { card -> this.add(card) }
        ListStrategy.RANDOM -> cards.forEach { card ->
            val index = random.nextInt(this.size + 1)
            this.add(index, card)
        }
    }
}
