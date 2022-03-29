package dev.bitspittle.racketeer.model.card

import dev.bitspittle.racketeer.model.action.Action
import kotlinx.serialization.Serializable

/**
 * @param cash How much this card costs in money
 * @param influence How much this card costs in influence points
 * @param victoryPoints How many points this card is worth for VP as long as it is not thrown in jail at some point
 */
@Serializable
data class CardTemplate(
    val name: String,
    val flavor: String,
    val actions: List<Action>,
    val tier: Int = 1,
    val cash: Int = 0,
    val influence: Int = 0,
    val victoryPoints: Int = 0,
) {
    fun instantiate() = Card(this)
}

class Card internal constructor(val template: CardTemplate) {
    /**
     * Cards can earn victory points over the course of the game.
     */
    var victoryPoints = template.victoryPoints
        private set
}
