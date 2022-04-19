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
    fun describeUpgradeTitle(upgrade: UpgradeType, icons: Boolean = true): String? {
        return when (upgrade) {
            UpgradeType.CASH -> if (icons) data.icons.cash else data.upgradeNames.cash
            UpgradeType.INFLUENCE -> if (icons) data.icons.influence else data.upgradeNames.influence
            UpgradeType.LUCK -> if (icons) data.icons.luck else data.upgradeNames.luck
            UpgradeType.JAILBIRD -> if (icons) null else data.upgradeNames.jailbird
            UpgradeType.UNDERCOVER -> if (icons) null else data.upgradeNames.undercover
        }
    }
    fun describeUpgradeBody(upgrade: UpgradeType): String {
        return when (upgrade) {
            UpgradeType.CASH -> "${data.upgradeNames.cash}: +1${data.icons.cash}"
            UpgradeType.INFLUENCE -> "${data.upgradeNames.influence}: +1${data.icons.influence}"
            UpgradeType.LUCK -> "${data.upgradeNames.luck}: +1${data.icons.luck}"
            UpgradeType.JAILBIRD -> "${data.upgradeNames.jailbird}: Provides victory points even in jail."
            UpgradeType.UNDERCOVER -> "${data.upgradeNames.undercover}: If still in hand, this isn't discard at end of turn."
        }
    }

    fun describeUpgradesTitle(upgrades: Set<UpgradeType>, icons: Boolean = false): String? {
        return UpgradeType.values()
            .filter { upgrade -> upgrades.contains(upgrade) }
            .mapNotNull { upgrade -> describeUpgradeTitle(upgrade, icons) }
            .joinToString(if (icons) "" else " ").takeIf { it.isNotEmpty() }
    }

    fun describeUpgradesBody(upgrades: Set<UpgradeType>): String {
        return UpgradeType.values()
            .filter { upgrade -> upgrades.contains(upgrade) }
            .joinToString("\n") { upgrade -> describeUpgradeBody(upgrade) }
    }

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

    private fun StringBuilder.appendCardBody(template: CardTemplate, upgrades: Set<UpgradeType> = emptySet(), vp: Int = template.vp, counter: Int = 0) {
        if (vp > 0) {
            append(" ${describeVictoryPoints(vp)}")
        }
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

        if (counter > 0) {
            appendLine("Counters: $counter")
        }

        appendLine() // Newline
        append(convertIcons(template.flavor))

        if (upgrades.isNotEmpty()) {
            appendLine() // Finish previous section
            appendLine() // Newline
            append(describeUpgradesBody(upgrades))
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
        val upgradesText = describeUpgradesTitle(upgrades, icons = concise)
        if (upgradesText != null) {
            append(upgradesText)
            append(' ')
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
                appendCardBody(card.template, card.upgrades, counter = card.counter)
            }
        }
    }

    fun describe(state: GameState, pile: Pile, title: Boolean = true): String {
        return if (title) {
            when (pile.id) {
                state.hand.id -> "Hand"
                state.deck.id -> "Deck"
                state.street.id -> "Street"
                state.discard.id -> "Discard"
                state.jail.id -> "Jail"
                else -> error("Unknown pile")
            } + " (${pile.cards.size})"
        } else {
            when (pile.id) {
                state.hand.id -> "your hand"
                state.deck.id -> "your deck"
                state.street.id -> "the street"
                state.discard.id -> "the discard pile"
                state.jail.id -> "jail"
                else -> error("Unknown pile")
            }
        }

    }
}
