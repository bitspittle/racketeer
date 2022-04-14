package dev.bitspittle.racketeer.model.text

import com.benasher44.uuid.uuid4
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

    private fun StringBuilder.appendCardBody(template: CardTemplate, upgrades: Set<UpgradeType> = emptySet()) {
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

    fun describe(template: CardTemplate, padName: Int = 0, showCash: Boolean = false, concise: Boolean = false): String {
        return buildString {
            appendCardName(template.name, emptySet(), padName, concise)
            if (showCash) {
                append(" ${describeCash(template.cost)}")
            }

            if (!concise) {
                append(" [Tier ${template.tier + 1}]")
                appendCardBody(template)
            }
        }
    }
    private fun StringBuilder.appendCardName(name: String, upgrades: Set<UpgradeType>, pad: Int, concise: Boolean) {
        val nameStart = this.length

        if (!concise) {
            if (upgrades.contains(UpgradeType.CASH)) {
                append("${data.upgradeNames.cash} ")
            }
            if (upgrades.contains(UpgradeType.INFLUENCE)) {
                append("${data.upgradeNames.influence} ")
            }
            if (upgrades.contains(UpgradeType.LUCK)) {
                append("${data.upgradeNames.luck} ")
            }
            if (upgrades.contains(UpgradeType.UNDERCOVER)) {
                append("${data.upgradeNames.undercover} ")
            }
        }
        else {
            if (upgrades.contains(UpgradeType.CASH)) {
                append(data.icons.cash)
            }
            if (upgrades.contains(UpgradeType.INFLUENCE)) {
                append(data.icons.influence)
            }
            if (upgrades.contains(UpgradeType.LUCK)) {
                append(data.icons.luck)
            }
            if (upgrades.contains(UpgradeType.UNDERCOVER)) {
                append(data.icons.undercover)
            }
            if (upgrades.isNotEmpty()) {
                append(' ')
            }
        }
        append(name)
        val nameEnd = this.length
        repeat((pad - (nameEnd - nameStart)).coerceAtLeast(0)) { append(' ') }
    }


    private fun StringBuilder.appendCardName(card: Card, pad: Int, concise: Boolean) {
        appendCardName(card.template.name, card.upgrades, pad, concise)
    }

    fun describe(cards: List<Card>, concise: Boolean = false): String {
        require(cards.isNotEmpty())
        val representativeCard = cards.first().copy(uuid4(), vpBase = 0, vpBonus = 0, upgrades = emptySet())
        return buildString {
            appendCardName(representativeCard, pad = 0, concise)
            if (concise) {
                append(" x${cards.size}")
                val vpSum = cards.sumOf { it.vpTotal }
                if (vpSum > 0) {
                    append(" ${describeVictoryPoints(vpSum)}")
                }
            }
            if (!concise) {
                appendCardBody(representativeCard.template)
            }
        }
    }

    fun describe(card: Card, namePad: Int = 0, concise: Boolean = false): String {
        return buildString {
            appendCardName(card, namePad, concise)

            if (card.vpTotal > 0) {
                append(" ${describeVictoryPoints(card.vpTotal)}")
            }

            if (!concise) {
                appendCardBody(card.template, card.upgrades)
            }
        }
    }
}
