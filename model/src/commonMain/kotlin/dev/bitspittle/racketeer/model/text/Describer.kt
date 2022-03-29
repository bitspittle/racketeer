package dev.bitspittle.racketeer.model.text

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameConfig

class Describers(private val config: GameConfig) {
    fun describeCash(cash: Int) = "${config.icons.cash} $cash"
    fun describeInfluence(influence: Int) = "${config.icons.influence} $influence"

    fun describe(template: CardTemplate, includeFlavor: Boolean = false): String {
        return buildString {
            append(template.name)
            if (template.cost.cash > 0) {
                append(' ')
                append(describeCash(template.cost.cash))
            }
            if (template.cost.influence > 0) {
                append(' ')
                append(describeInfluence(template.cost.influence))
            }

            if (includeFlavor) {
                appendLine()
                append(template.flavor)
            }
        }
    }

    fun describe(card: Card, includeFlavor: Boolean = false): String {
        return describe(card.template, includeFlavor)
    }
}
