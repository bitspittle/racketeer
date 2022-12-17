package dev.bitspittle.racketeer.model.text

import dev.bitspittle.limp.utils.ifTrue
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.pile.Pile

private fun GameData.iconMappings() = mapOf(
    '$' to icons.cash,
    '&' to icons.influence,
    '%' to icons.luck,
    '*' to icons.vp,
)

class Describer(private val data: GameData, private val showDebugInfo: () -> Boolean) {
    /**
     * Run through some target [text] and convert some predefined ascii characters to emoji icons.
     *
     * @param maxExpand A numeric count for which it will be automatically expanded into a series of emoji values,
     *   e.g. 3$ -> $$$, if `maxExpand` >= 3 (otherwise, it would be left as 3$)
     */
    // Programmer's note: This method isn't very elegant, but it is usually fed pretty short strings which have
    // very few matches, so the actual amount of new strings we're churning out is few to none, and performance-wise,
    // we don't really have to worry about the inefficiency of running through short strings over and over.
    fun convertIcons(text: String, maxExpand: Int = 4): String {
        val iconMappings = data.iconMappings()

        return buildString {
            var lastNumber: Int? = null
            for (c in text) {
                if (c.isDigit()) {
                    if (lastNumber != null) {
                        lastNumber *= 10
                    } else {
                        lastNumber = 0
                    }
                    lastNumber += c.digitToInt()
                } else {
                    if (c in iconMappings.keys) {
                        if (lastNumber != null && lastNumber <= maxExpand) {
                            repeat(lastNumber) { append(iconMappings[c]) }
                        } else {
                            if (lastNumber != null) {
                                append(lastNumber)
                            }
                            append(iconMappings[c])
                        }
                    } else {
                        if (lastNumber != null) {
                            append(lastNumber)
                        }
                        append(c)
                    }
                    lastNumber = null
                }
            }

            // This should never with happen with our data, as we don't have text that
            // ends with numbers and no puncutation, but we handle it just in case...
            if (lastNumber != null) {
                append(lastNumber)
            }
        }
    }
    fun describeCash(cash: Int) = "${data.icons.cash}$cash"
    fun describeInfluence(influence: Int) = "${data.icons.influence}$influence"
    fun describeLuck(luck: Int) = "${data.icons.luck}$luck"
    fun describeVictoryPoints(vp: Int) = "${data.icons.vp}$vp"

    fun describeTraitTitle(trait: TraitType): String {
        return when (trait) {
            TraitType.EXPENDABLE -> data.traitNames.expendable
            TraitType.SUSPICIOUS -> data.traitNames.suspicious
            TraitType.SWIFT -> data.traitNames.swift
        }
    }

    private fun describeTraitBody(trait: TraitType): String {
        return when (trait) {
            TraitType.EXPENDABLE -> "${data.traitNames.expendable}: When played, burn this card."
            TraitType.SUSPICIOUS -> "${data.traitNames.suspicious}: When played, move to jail."
            TraitType.SWIFT -> "${data.traitNames.swift}: When you get this, goes into your hand."
        }
    }

    fun describeUpgradeTitle(upgrade: UpgradeType, icons: Boolean = true): String {
        return when (upgrade) {
            UpgradeType.CASH -> if (icons) data.icons.cash else data.upgradeNames.cash
            UpgradeType.INFLUENCE -> if (icons) data.icons.influence else data.upgradeNames.influence
            UpgradeType.LUCK -> if (icons) data.icons.luck else data.upgradeNames.luck
            UpgradeType.VETERAN -> if (icons) data.icons.veteran else data.upgradeNames.veteran
        }
    }

    private fun describeUpgradeBody(upgrade: UpgradeType): String {
        return when (upgrade) {
            UpgradeType.CASH -> "${data.upgradeNames.cash}: +1${data.icons.cash}."
            UpgradeType.INFLUENCE -> "${data.upgradeNames.influence}: +1${data.icons.influence}."
            UpgradeType.LUCK -> "${data.upgradeNames.luck}: +1${data.icons.luck}."
            UpgradeType.VETERAN -> "${data.upgradeNames.veteran}: Draw a card, then discard one."
        }
    }

    private fun describeTraitsBody(traits: Set<TraitType>): String {
        return TraitType.values()
            .filter { trait -> traits.contains(trait) }
            .joinToString("\n") { trait -> describeTraitBody(trait) }
    }

    private fun describeUpgradesIcons(upgrades: Set<UpgradeType>): String? {
        return UpgradeType.values()
            .filter { upgrade -> upgrades.contains(upgrade) }
            .joinToString("") { upgrade ->
                describeUpgradeTitle(upgrade, icons = true) }.takeIf { it.isNotEmpty()
            }
    }

    private fun describeUpgradesBody(upgrades: Set<UpgradeType>): String {
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

    private fun StringBuilder.appendCardBody(template: CardTemplate, traits: Set<TraitType> = template.traitTypes, upgrades: Set<UpgradeType> = emptySet(), vp: Int = template.vp, counter: Int = 0, includeFlavor: Boolean = false) {
        if (vp != 0) {
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
        append(convertIcons(template.description.ability))
        if (includeFlavor) {
            template.description.flavor?.let { flavor ->
                appendLine() // Finish ability sentence
                appendLine() // Newline
                append("${data.icons.flavor} $flavor")
            }
        }

        if (traits.isNotEmpty() || upgrades.isNotEmpty()) {
            appendLine() // Finish previous section
            appendLine() // Newline
            val traitsBody = describeTraitsBody(traits)
            val upgradesBody = describeUpgradesBody(upgrades)

            append(traitsBody)
            if (traitsBody.isNotEmpty() && upgradesBody.isNotEmpty()) appendLine()
            append(upgradesBody)
        }

        if (showDebugInfo()) {
            template.allInitActions.takeIf { it.isNotEmpty() }?.let { initActions ->
                appendLine() // Finish previous section
                appendLine() // Newline
                appendLine("When first enters play:")
                initActions.forEachIndexed { i, action ->
                    append("- $action")
                    if (i < initActions.lastIndex) {
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

    fun describeCardTitle(template: CardTemplate): String {
        return buildString {
            appendCardName(template.name, upgrades = emptySet())
        }
    }

    fun describeCardBody(template: CardTemplate, showCost: Boolean = false, includeFlavor: Boolean = false): String {
        return buildString {
            appendCardName(template.name, upgrades = emptySet(), price = template.cost.takeIf { showCost })
            appendCardBody(template, includeFlavor = includeFlavor)
        }
    }

    private fun StringBuilder.appendCardName(name: String, upgrades: Set<UpgradeType>, useIcons: Boolean = true, totalVp: Int? = null, price: Int? = null) {
        val upgradesIcons = useIcons.ifTrue { describeUpgradesIcons(upgrades) }
        if (upgradesIcons != null) {
            append(upgradesIcons)
            append(' ')
        }
        append(name)
        if (totalVp != null && totalVp != 0) {
            append(" ${describeVictoryPoints(totalVp)}")
        }

        if (price != null && price > 0) {
            append(" ${describeCash(price)}")
        }
    }

    private fun StringBuilder.appendCardName(card: Card, useIcons: Boolean = true, totalVp: Int? = null, price: Int? = null) {
        appendCardName(card.template.name, card.upgrades, useIcons, totalVp, price)
    }

    fun describeCardGroupTitle(cards: List<Card>): String {
        require(cards.isNotEmpty())
        val representativeCard = cards.first().template.instantiate()
        return buildString {
            appendCardName(representativeCard)
            append(" x${cards.size}")
        }
    }

    fun describeCardGroupBody(cards: List<Card>): String {
        require(cards.isNotEmpty())
        val representativeCard = cards.first().template.instantiate()
        return buildString {
            appendCardName(representativeCard)
            appendCardBody(representativeCard.template)
        }
    }

    fun describeCardTitle(card: Card): String {
        return buildString {
            appendCardName(card, useIcons = true, card.vpTotal)
        }
    }

    fun describeCardBody(card: Card, showCost: Boolean = false): String {
        return buildString {
            appendCardName(card, useIcons = false, card.vpTotal, card.template.cost.takeIf { showCost })
            // Don't re-report victory points, they were already reported as part of the name
            appendCardBody(card.template, card.traits, card.upgrades, counter = card.counter, vp = 0)
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
        if (!blueprint.activationCost.isFree()) {
            appendLine("Activation cost: ${describeActivationCost(blueprint)}")
        }
    }

    private fun StringBuilder.appendBlueprintBody(blueprint: Blueprint, includeFlavor: Boolean = false) {
        append(convertIcons(blueprint.description.ability))
        if (includeFlavor) {
            blueprint.description.flavor?.let { flavor ->
                appendLine() // Finish ability
                appendLine() // Newline
                append("${data.icons.flavor} $flavor")
            }
        }

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

    fun describeBlueprintTitle(blueprint: Blueprint): String {
        return buildString {
            append(blueprint.name)
        }
    }

    fun describeBlueprintBody(blueprint: Blueprint, includeFlavor: Boolean = false, showBuildCost: Boolean = false): String {
        return buildString {
            append(blueprint.name)
            if (showBuildCost) {
                describeBuildCost(blueprint).takeIf { it.isNotEmpty() }?.let { cost ->
                    append(' ')
                    append(cost)
                }
            }
            append(" [${data.rarities[blueprint.rarity].name}]")
            appendLine()

            appendActivationCost(blueprint)
            appendLine() // Newline
            appendBlueprintBody(blueprint, includeFlavor)
        }
    }

    private fun StringBuilder.appendBuildingName(building: Building) {
        append(building.blueprint.name)
        if (building.vpTotal != 0) {
            append(" ${describeVictoryPoints(building.vpTotal)}")
        }
    }

    fun describeBuildingTitle(building: Building): String {
        return buildString {
            appendBuildingName(building)
        }
    }

    fun describeBuildingBody(building: Building, showActivatedState: Boolean = false): String {
        return buildString {
            appendBuildingName(building)
            append(" [${data.rarities[building.blueprint.rarity].name}]")

            appendLine()
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
