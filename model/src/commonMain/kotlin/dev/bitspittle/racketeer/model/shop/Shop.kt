package dev.bitspittle.racketeer.model.shop

import dev.bitspittle.racketeer.model.card.CardTemplate

interface Shop {
    val tier: Int
    val cards: List<CardTemplate>
}

class MutableShop private constructor(tier: Int, stock: MutableList<CardTemplate>) : Shop {
    constructor() : this(0, mutableListOf())

    override var tier: Int = tier
        private set
    override val cards = stock

    fun copy() = MutableShop(tier, cards.toMutableList())
}