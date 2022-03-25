package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class CardTemplate(
    val name: String,
    val cash: Int = 0,
    val influence: Int = 0,
) {
    fun instantiate() = Card(this)

    override fun toString(): String {
        return buildString {
            append(name)
            if (cash > 0) {
                append(" \uD83E\uDE99 $cash")
            }
            if (influence > 0) {
                append(" \uD83E\uDD1D $influence")
            }
        }
    }
}

class Card internal constructor(val template: CardTemplate) {
    override fun toString(): String {
        return template.toString()
    }
}