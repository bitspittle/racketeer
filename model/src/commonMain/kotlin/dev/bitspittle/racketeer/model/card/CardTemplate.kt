package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class CardTemplate(
    val name: String,
    val cost: Int,
    val influence: Int,
) {
    fun instantiate() = Card(this)
}

class Card internal constructor(val template: CardTemplate)