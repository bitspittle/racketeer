package dev.bitspittle.racketeer.model.text

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.UpgradeType
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

    private fun StringBuilder.describeCardBody(template: CardTemplate, upgrades: Set<UpgradeType> = emptySet()) {
        appendLine() // Finish title
        appendLine() // Newline
        append(
            template.flavor.replace("$", data.icons.cash)
                .replace("&", data.icons.influence)
                .replace("%", data.icons.luck)
                .replace("*", data.icons.vp)
        )

        if (upgrades.isNotEmpty()) {
            appendLine() // Finish previous section
            appendLine() // Newline
            if (upgrades.contains(UpgradeType.CASH)) {
                append("${data.upgradeNames.cash}: +1${data.icons.cash}")
            }
            if (upgrades.contains(UpgradeType.INFLUENCE)) {
                append("${data.upgradeNames.influence}: +1${data.icons.influence}")
            }
            if (upgrades.contains(UpgradeType.LUCK)) {
                append("${data.upgradeNames.luck}: +1${data.icons.luck}")
            }
            if (upgrades.contains(UpgradeType.PATIENCE)) {
                append("${data.upgradeNames.patience}: If still in hand, this isn't discard at end of turn")
            }
        }

        if (template.actions.isNotEmpty()) {
            appendLine() // Finish previous section
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
            if (concise) {
                // Only show costs when browsing in the shop
                if (template.cost > 0) {
                    append(" ${describeCash(template.cost)}")
                }
            }

            if (!concise) {
                append(" [Tier ${template.tier + 1}]")
                describeCardBody(template)
            }
        }
    }

    fun describe(card: Card, count: Int? = null, concise: Boolean = false): String {
        return buildString {
            if (!concise) {
                if (card.upgrades.contains(UpgradeType.CASH)) {
                    append("${data.upgradeNames.cash} ")
                }
                if (card.upgrades.contains(UpgradeType.INFLUENCE)) {
                    append("${data.upgradeNames.influence} ")
                }
                if (card.upgrades.contains(UpgradeType.LUCK)) {
                    append("${data.upgradeNames.luck} ")
                }
                if (card.upgrades.contains(UpgradeType.PATIENCE)) {
                    append("${data.upgradeNames.patience} ")
                }
            }
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
                describeCardBody(card.template, card.upgrades)
            }
        }
    }
}
