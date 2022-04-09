package dev.bitspittle.racketeer.model.text

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameData

class Describer(private val data: GameData) {
    fun describeCash(cash: Int) = "${data.icons.cash} $cash"
    fun describeInfluence(influence: Int) = "${data.icons.influence} $influence"
    fun describeLuck(luck: Int) = "${data.icons.luck} $luck"
    fun describeVictoryPoints(vp: Int) = "${data.icons.vp} $vp"
    fun describeRange(range: IntRange): String {
        return when {
            range.first == range.last -> range.first.toString()
            range.first <= 0 -> {
                when (range.last) {
                    Int.MAX_VALUE -> "any number of"
                    else -> "at most ${range.last}"
                }
            }
            else -> {
                check(range.first > 0)
                when (range.last) {
                    Int.MAX_VALUE -> "at least ${range.first}"
                    else -> "between ${range.first} and ${range.last}"
                }
            }
        }
    }

    private fun StringBuilder.describeCardBody(template: CardTemplate) {
        appendLine() // Finish title
        appendLine() // Newline
        append(
            template.flavor.replace("$", data.icons.cash)
                .replace("&", data.icons.influence)
                .replace("%", data.icons.luck)
                .replace("*", data.icons.vp)
        )
        if (template.actions.isNotEmpty()) {
            appendLine() // Finish desc
            appendLine() // Newline
            template.actions.forEachIndexed { i, action ->
                append("~ $action")
                if (i < template.actions.lastIndex) {
                    appendLine()
                }
            }
        }
    }

    fun describe(template: CardTemplate, concise: Boolean = false): String {
        return buildString {
            append(template.name)
            if (template.cost > 0) {
                append(" ${describeCash(template.cost)}")
            }

            if (!concise) {
                describeCardBody(template)
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
                describeCardBody(card.template)
            }
        }
    }
}
