package dev.bitspittle.racketeer.model.text

import com.benasher44.uuid.uuid4
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState

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

        append(" [Tier ${template.tier + 1}]")
        append(" [${data.rarities[template.rarity].name}]")
        appendLine() // Finish title

        // Show types -- and use the data.cardTypes list instead of the card.types list as they are capitalized
        // correctly AND in the desired order.
        appendLine(
            data.cardTypes.mapNotNull { typeName ->
                if (template.types.any { typeName.equals(it, ignoreCase = true) }) typeName else null
            }.joinToString(prefix = "(", postfix = ")")
        )

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
                append("- $action")
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
                append("- $action")
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
                append("- $action")
                if (i < allPassiveActions.lastIndex) {
                    appendLine()
                }
            }
        }
    }

    fun describe(template: CardTemplate, showCash: Boolean = false, concise: Boolean = false): String {
        return buildString {
            appendCardName(template.name, emptySet(), concise)
            if (showCash) {
                append(" ${describeCash(template.cost)}")
            }

            if (!concise) {
                appendCardBody(template)
            }
        }
    }
    private fun StringBuilder.appendCardName(name: String, upgrades: Set<UpgradeType>, concise: Boolean) {
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
    }

    private fun StringBuilder.appendCardName(card: Card, concise: Boolean) {
        appendCardName(card.template.name, card.upgrades, concise)
    }

    fun describe(cards: List<Card>, concise: Boolean = false): String {
        require(cards.isNotEmpty())
        val representativeCard = cards.first().copy(uuid4(), vpBase = 0, vpBonus = 0, upgrades = emptySet())
        return buildString {
            appendCardName(representativeCard, concise)
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

    fun describe(card: Card, concise: Boolean = false): String {
        return buildString {
            appendCardName(card, concise)

            if (card.vpTotal > 0) {
                append(" ${describeVictoryPoints(card.vpTotal)}")
            }

            if (!concise) {
                appendCardBody(card.template, card.upgrades)
            }
        }
    }

    fun describe(state: GameState, pile: Pile, title: Boolean = true): String {
        return if (title) {
            when (pile) {
                state.hand -> "Hand"
                state.deck -> "Deck"
                state.street -> "Street"
                state.discard -> "Discard"
                state.jail -> "Jail"
                else -> error("Unknown pile")
            } + " (${pile.cards.size})"
        } else {
            when (pile) {
                state.hand -> "your hand"
                state.deck -> "your deck"
                state.street -> "the street"
                state.discard -> "the discard pile"
                state.jail -> "jail"
                else -> error("Unknown pile")
            }
        }

    }
}
