package dev.bitspittle.racketeer.model.card

import kotlin.random.Random

enum class InsertStrategy {
    FRONT,
    BACK,
    RANDOM
}

fun MutableList<Card>.insert(cards: List<Card>, insertStrategy: InsertStrategy, random: Random = Random.Default) {
    when (insertStrategy) {
        InsertStrategy.FRONT -> cards.forEachIndexed { i, card -> this.add(i, card) }
        InsertStrategy.BACK -> cards.forEach { card -> this.add(card) }
        InsertStrategy.RANDOM -> cards.forEach { card ->
            val index = random.nextInt(this.size + 1)
            this.add(index, card)
        }
    }
}

fun MutableList<Card>.insert(card: Card, insertStrategy: InsertStrategy, random: Random = Random.Default) {
    this.insert(listOf(card), insertStrategy, random)
}
