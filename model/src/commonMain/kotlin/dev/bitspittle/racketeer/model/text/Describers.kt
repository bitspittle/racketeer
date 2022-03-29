package dev.bitspittle.racketeer.model.text

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameConfig

class Describers(private val config: GameConfig) {
    fun describeCash(cash: Int) = "${config.icons.cash} $cash"
    fun describeInfluence(influence: Int) = "${config.icons.influence} $influence"
    fun describeLuck(luck: Int) = "${config.icons.luck} $luck"
    fun describeVictoryPoints(vp: Int) = "${config.icons.vp} $vp"

    fun describe(template: CardTemplate, concise: Boolean = false): String {
        return buildString {
            append(template.name)
            if (template.cost.cash > 0) {
                append(" ${describeCash(template.cost.cash)}")
            }
            if (template.cost.influence > 0) {
                append(" ${describeInfluence(template.cost.cash)}")
            }

            if (!concise) {
                appendLine() // Finish title
                appendLine() // Newline
                append(template.flavor)
            }
        }
    }

    fun describe(card: Card, count: Int? = null, concise: Boolean = false): String {
        return buildString {
            append(card.template.name)

            if (concise) {
                if (count != null) {
                    append(" x$count")
                }
            }

            if (card.vp > 0) {
                append(" ${describeVictoryPoints(card.vp)}")
            }

            if (!concise) {
                appendLine() // Finish title
                appendLine() // Newline
                append(card.template.flavor)
            }
        }
    }
}
