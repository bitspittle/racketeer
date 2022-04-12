package dev.bitspittle.racketeer.model.text

import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.game.GameData

class Describer(private val data: GameData) {
    fun convertIcons(text: String): String {
        return text.replace("$", data.icons.cash)
            .replace("&", data.icons.influence)
            .replace("%", data.icons.luck)
            .replace("*", data.icons.vp)
    }
    fun describeCash(cash: Int) = "${data.icons.cash}$cash"
    fun describeInfluence(influence: Int) = "${data.icons.influence}$influence"
    fun describeLuck(luck: Int) = "${data.icons.luck}$luck"
    fun describeVictoryPoints(vp: Int) = "${data.icons.vp}$vp"
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
        append(convertIcons(template.flavor))

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
            if (upgrades.contains(UpgradeType.UNDERCOVER)) {
                append("${data.upgradeNames.undercover}: If still in hand, this isn't discard at end of turn")
            }
        }

        if (template.initActions.isNotEmpty()) {
            appendLine() // Finish previous section
            appendLine() // Newline
            appendLine("When card first enters play:")
            template.initActions.forEachIndexed { i, action ->
                append("~ $action")
                if (i < template.initActions.lastIndex) {
                    appendLine()
                }
            }
        }
        if (template.playActions.isNotEmpty()) {
            appendLine() // Finish previous section
            appendLine() // Newline
            appendLine("When card is played:")
            template.playActions.forEachIndexed { i, action ->
                append("~ $action")
                if (i < template.playActions.lastIndex) {
                    appendLine()
                }
            }
        }
        template.allPassiveActions.takeIf { it.isNotEmpty() }?.let { allPassiveActions ->
            appendLine() // Finish previous section
            appendLine() // Newline
            appendLine("Passive actions:")
            allPassiveActions.forEachIndexed { i, action ->
                append("~ $action")
                if (i < allPassiveActions.lastIndex) {
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
                if (card.upgrades.contains(UpgradeType.UNDERCOVER)) {
                    append("${data.upgradeNames.undercover} ")
                }
            }
            append(card.template.name)

            if (concise) {
                if (count != null) {
                    append(" x$count")
                }
            }

            if (card.vpTotal > 0) {
                append(" ${describeVictoryPoints(card.vpTotal)}")
            }

            if (!concise) {
                describeCardBody(card.template, card.upgrades)
            }
        }
    }
}
