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
            if (template.cash > 0) {
                append(' ')
                append(describeCash(template.cash))
            }
            if (template.influence > 0) {
                append(' ')
                append(describeInfluence(template.influence))
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
