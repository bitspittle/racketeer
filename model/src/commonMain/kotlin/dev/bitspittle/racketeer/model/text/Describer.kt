package dev.bitspittle.racketeer.model.text

import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.pile.Pile

private fun GameData.iconMappings() = mapOf(
    "$" to icons.cash,
    "&" to icons.influence,
    "%" to icons.luck,
    "*" to icons.vp,
)

class Describer(private val data: GameData, private val showDebugInfo: () -> Boolean) {
    // Programmer's note: This method isn't very elegant, but it is usually fed pretty short strings which have
    // very few matches, so the actual amount of new strings we're churning out is few to none, and performance-wise,
    // we don't really have to worry about the inefficiency of running through short strings over and over.
    fun convertIcons(text: String): String {
        @Suppress("NAME_SHADOWING")
        var text = text

        val iconMappings = data.iconMappings()

        for (i in 1..4) {
            // Change something like 2% to %%, 4$ to $$$$
            for (asciiIcon in iconMappings.keys) {
                // .. but don't match two digit numbers, e.g. 12$
                text = text.replace(Regex("(?<=\\D)$i\\$asciiIcon"), Regex.escapeReplacement(asciiIcon.repeat(i)))
            }
        }

        iconMappings.forEach { (asciiIcon, icon) ->
            text = text.replace(asciiIcon, icon)
        }

        return text
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
            UpgradeType.VETERAN -> if (icons) data.icons.card else data.upgradeNames.veteran
        }
    }
    fun describeUpgradeBody(upgrade: UpgradeType): String {
        return when (upgrade) {
            UpgradeType.CASH -> "${data.upgradeNames.cash}: +1${data.icons.cash}."
            UpgradeType.INFLUENCE -> "${data.upgradeNames.influence}: +1${data.icons.influence}."
            UpgradeType.LUCK -> "${data.upgradeNames.luck}: +1${data.icons.luck}."
            UpgradeType.VETERAN -> "${data.upgradeNames.veteran}: Draw an extra card."
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
            range.first == range.last -> "exactly ${range.first}"
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

        if (showDebugInfo()) {
            if (template.initActions.isNotEmpty()) {
                appendLine() // Finish previous section
                appendLine() // Newline
                appendLine("When first enters play:")
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
                appendLine("When played:")
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
    }

    fun describeCard(template: CardTemplate, showCash: Boolean = false, concise: Boolean = false): String {
        return buildString {
            appendCardName(template.name, emptySet(), concise)
            if (showCash && template.cost > 0) {
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

    fun describeCard(cards: List<Card>, concise: Boolean = false): String {
        require(cards.isNotEmpty())
        val representativeCard = cards.first().template.instantiate()
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

    fun describeCard(card: Card, concise: Boolean = false): String {
        return buildString {
            appendCardName(card, concise)

            if (card.vpTotal > 0) {
                append(" ${describeVictoryPoints(card.vpTotal)}")
            }

            if (!concise) {
                // Don't re-report victory points, they were already reported as part of the name
                appendCardBody(card.template, card.upgrades, counter = card.counter, vp = 0)
            }
        }
    }

    fun describePileTitle(state: GameState, pile: Pile, withSize: Boolean = false): String {
        return when (pile.id) {
            state.hand.id -> "Hand"
            state.deck.id -> "Deck"
            state.street.id -> "Street"
            state.discard.id -> "Discard"
            state.jail.id -> "Jail"
            state.graveyard.id -> "Graveyard"
            else -> error("Unknown pile")
        } + if (withSize) " (${pile.cards.size})" else ""
    }

    fun describePile(state: GameState, pile: Pile): String {
        return when (pile.id) {
            state.hand.id -> "your hand"
            state.deck.id -> "your deck"
            state.street.id -> "the street"
            state.discard.id -> "the discard pile"
            state.jail.id -> "jail"
            state.graveyard.id -> "the graveyard"
            else -> error("Unknown pile")
        }
    }

    fun describeBuildCost(blueprint: Blueprint): String {
        return buildString {
            if (blueprint.buildCost.cash > 0) {
                append(describeCash(blueprint.buildCost.cash))
            }

            if (blueprint.buildCost.influence > 0) {
                if (this.isNotEmpty()) append(' ')
                append(describeInfluence(blueprint.buildCost.influence))
            }
        }
    }

    fun describeActivationCost(blueprint: Blueprint): String {
        return buildString {
            blueprint.activationCost.let { cost ->
                if (cost.cash > 0) {
                    append(describeCash(cost.cash))
                }

                if (cost.influence > 0) {
                    if (this.isNotEmpty()) append(' ')
                    append(describeInfluence(cost.influence))
                }

                if (cost.luck > 0) {
                    if (this.isNotEmpty()) append(' ')
                    append(describeLuck(cost.luck))
                }
            }
        }
    }

    private fun StringBuilder.appendActivationCost(blueprint: Blueprint) {
        appendLine("Activation cost: ${describeActivationCost(blueprint)}")
    }

    private fun StringBuilder.appendBlueprintBody(blueprint: Blueprint) {
        append(convertIcons(blueprint.flavor))

        if (showDebugInfo()) {
            if (blueprint.canActivate.isNotBlank()) {
                appendLine() // Finish previous section
                appendLine() // Newline
                append("Activation test: ${blueprint.canActivate}")
            }

            if (blueprint.initActions.isNotEmpty()) {
                appendLine() // Finish previous section
                appendLine() // Newline
                appendLine("When first built:")
                blueprint.initActions.forEachIndexed { i, action ->
                    append("- $action")
                    if (i < blueprint.initActions.lastIndex) {
                        appendLine()
                    }
                }
            }
            if (blueprint.activateActions.isNotEmpty()) {
                appendLine() // Finish previous section
                appendLine() // Newline
                appendLine("When activated:")
                blueprint.activateActions.forEachIndexed { i, action ->
                    append("- $action")
                    if (i < blueprint.activateActions.lastIndex) {
                        appendLine()
                    }
                }
            }
            if (blueprint.passiveActions.isNotEmpty()) {
                appendLine() // Finish previous section
                appendLine() // Newline
                appendLine("Passive actions:")
                blueprint.passiveActions.forEachIndexed { i, action ->
                    append("- $action")
                    if (i < blueprint.passiveActions.lastIndex) {
                        appendLine()
                    }
                }
            }
        }
    }

    fun describeBlueprint(blueprint: Blueprint, concise: Boolean = false): String {
        return buildString {
            append(blueprint.name)

            if (!concise) {
                appendLine() // Finish name row
                appendActivationCost(blueprint)
                appendLine() // Newline
                appendBlueprintBody(blueprint)
            }
        }
    }

    fun describeBuilding(building: Building, showActivatedState: Boolean = false, concise: Boolean = false): String {
        return buildString {
            append(building.blueprint.name)

            if (building.vpTotal > 0) {
                append(" ${describeVictoryPoints(building.vpTotal)}")
            }

            if (!concise) {
                appendLine() // Finish name row
                appendActivationCost(building.blueprint)
                if (showActivatedState) {
                    appendLine("Activated this turn? " + if (building.isActivated) "Yes" else "No")
                }
                if (building.counter > 0) {
                    appendLine("Counters: ${building.counter}")
                }

                appendLine()

                appendBlueprintBody(building.blueprint)
            }
        }
    }
}
