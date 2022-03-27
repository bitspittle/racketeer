package dev.bitspittle.racketeer.model.card

import dev.bitspittle.racketeer.model.action.Action
import dev.bitspittle.racketeer.model.game.GameIcons
import kotlinx.serialization.Serializable

@Serializable
data class CardTemplate(
    val name: String,
    val flavor: String,
    val actions: List<Action>,
    val cash: Int = 0,
    val influence: Int = 0,
    val victoryPoints: Int = 0,
) {
    fun instantiate() = Card(this)
}

class Card internal constructor(val template: CardTemplate)

class CardDescriber(private val icons: GameIcons) {
    fun toDisplayString(cardTemplate: CardTemplate): String {
        return buildString {
            append(cardTemplate.name)
            if (cardTemplate.cash > 0) {
                append(" ${icons.cash} ${cardTemplate.cash}")
            }
            if (cardTemplate.influence > 0) {
                append(" ${icons.influence} ${cardTemplate.influence}")
            }
        }
    }

    fun toDisplayString(card: Card): String = toDisplayString(card.template)
}