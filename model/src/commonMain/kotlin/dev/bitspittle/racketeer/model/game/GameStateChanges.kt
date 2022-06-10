@file:Suppress("DEPRECATION") // I'm not going to replace capitalize with their complex recommendation...

package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.building.BuildingProperty
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.effect.Tweak
import dev.bitspittle.racketeer.model.effect.warningExpr
import dev.bitspittle.racketeer.model.text.Describer

/**
 * A collection of related [GameStateChange]s.
 *
 * By grouping them, you can see the root change which kicked off secondary changes, and you can see how game resources
 * (e.g. cash, influence, etc.) changed compared to a previous game state snapshot.
 */
class GameStateChanges {
    private val _changes = mutableListOf<GameStateChange>()
    val changes: List<GameStateChange> = _changes

    var handSize: Int = 0
        private set
    var cash: Int = 0
        private set
    var influence: Int = 0
        private set
    var luck: Int = 0
        private set
    var vp: Int = 0
        private set

    fun add(change: GameStateChange) {
        _changes.add(change)
    }

    fun snapshotResources(state: GameState) {
        handSize = state.handSize
        cash = state.cash
        influence = state.influence
        luck = state.luck
        vp = state.vp
    }

    private fun List<GameStateChange>.mergeRelated(): List<GameStateChange> {
        val changes = this.toMutableList()

        // Convert "MoveCards" to "MoveCard" when possible, it reads better
        for (i in changes.indices) {
            val change = changes[i]
            if (change is GameStateChange.MoveCards && change.cards.size == 1) {
                changes[i] = GameStateChange.MoveCard(change.cards[0], change.intoPile, change.listStrategy)
            }
        }

        // Sometimes cards can bounce around quickly due to init actions. Lets marge those together...
        changes
            .filterIsInstance<GameStateChange.MoveCard>()
            .groupBy { it.card }
            .filter { (_, cardMoves) -> cardMoves.size > 1 }
            .forEach { (_, cardMoves) ->
                val transientMoves = cardMoves.dropLast(1)
                changes.removeAll { transientMoves.contains(it) }
            }

        return changes
    }

    fun toSummaryText(describer: Describer, state: GameState): String? {
        if (_changes.isEmpty()) return null

        return buildString {
            val changes = _changes.mergeRelated()
            changes.forEach { change ->
                when (change) {
                    is GameStateChange.ShuffleDiscardIntoDeck -> report(describer, state, change)
                    is GameStateChange.Draw -> report(describer, state, change)
                    is GameStateChange.Play -> Unit // No need to report, obvious from user actions
                    is GameStateChange.MoveCards -> report(describer, state, change)
                    is GameStateChange.MoveCard -> report(describer, state, change)
                    is GameStateChange.Shuffle -> report(describer, state, change)
                    is GameStateChange.AddCardAmount -> report(describer, state, change)
                    is GameStateChange.UpgradeCard -> report(describer, change)
                    is GameStateChange.AddTrait -> report(describer, change)
                    is GameStateChange.RemoveTrait -> report(describer, change)
                    is GameStateChange.AddBuildingAmount -> report(describer, change)
                    is GameStateChange.AddGameAmount -> Unit // Reported below, in aggregate
                    is GameStateChange.SetGameData -> Unit // Game data is only for use by the designers, don't report it
                    is GameStateChange.AddEffect -> report(change)
                    is GameStateChange.AddGameTweak -> report(describer, change)
                    is GameStateChange.AddShopTweak -> report(describer, change)
                    is GameStateChange.Buy -> Unit // No need to report, obvious from user actions
                    is GameStateChange.RestockShop -> report(change)
                    is GameStateChange.UpgradeShop -> report(change)
                    is GameStateChange.AddBlueprint -> report(change)
                    is GameStateChange.Build -> report(change)
                    is GameStateChange.Activate -> Unit // No need to report, obvious from user actions
                    is GameStateChange.EndTurn -> Unit // No need to report, obvious from user actions
                    is GameStateChange.GameOver -> Unit // Marker game state, no need to report
                }
            }

            run { // Report deltas
                var handSize: Int = 0
                var cash: Int = 0
                var influence: Int = 0
                var luck: Int = 0
                var vp: Int = 0

                changes.filterIsInstance<GameStateChange.AddGameAmount>().forEach { change ->
                    when (change.property) {
                        GameProperty.CASH -> cash += change.amount
                        GameProperty.VP -> TODO()
                        GameProperty.INFLUENCE -> TODO()
                        GameProperty.LUCK -> TODO()
                        GameProperty.HAND_SIZE -> TODO()
                    }

                }
            }
        }
    }
//        (cash == 0 && influence == 0 && luck == 0 && vp == 0 && movedCards.isEmpty()).ifFalse {
//            buildString {
//                if (isEmpty()) {
//                    append("No changes caused by this action.")
//                    return@buildString
//                }
//
//                movedCards.takeIf { it.isNotEmpty() }?.let { movedCards ->
//                    movedCards.forEach { (piles, cards) ->
//                        if (this.isNotEmpty()) appendLine()
//
//                        val (pileFrom, pileTo) = piles
//                        val pileToName = describer.describePile(state, pileTo)
//                        if (pileFrom == null) {
//                            append("Created into $pileTo: ${cards.size} card(s)")
//                        } else {
//                            val pileFromName = describer.describePile(state, pileFrom)
//                            append("Moved from $pileFromName to $pileToName: ${cards.size} card(s)")
//                        }
//                    }
//                }
//
//                cash.takeIf { it != 0 }?.let { amount ->
//                    if (this.isNotEmpty()) appendLine()
//                    append(if (amount > 0) "Earned: " else "Spent: ")
//                    append(describer.describeCash(amount.absoluteValue))
//                }
//
//                influence.takeIf { it != 0 }?.let { amount ->
//                    if (this.isNotEmpty()) appendLine()
//                    append(if (amount > 0) "Gained: " else "Lost: ")
//                    append(describer.describeInfluence(amount.absoluteValue))
//                }
//
//                luck.takeIf { it != 0 }?.let { amount ->
//                    if (this.isNotEmpty()) appendLine()
//                    append(if (amount > 0) "Gained: " else "Lost: ")
//                    append(describer.describeLuck(amount.absoluteValue))
//                }
//
//                vp.takeIf { it != 0 }?.let { amount ->
//                    if (this.isNotEmpty()) appendLine()
//                    append(if (amount > 0) "Gained: " else "Lost: ")
//                    append(describer.describeVictoryPoints(amount.absoluteValue))
//                }
//            }
//        }

    private fun ListStrategy.toDesc(): String {
        return when (this) {
            ListStrategy.FRONT -> "to the front of"
            ListStrategy.BACK -> "into"
            ListStrategy.RANDOM -> "randomly into"
        }
    }

    private fun StringBuilder.reportLine(message: String) {
        if (this.isNotEmpty()) {
            appendLine()
        }
        append("- $message")
    }

    private fun StringBuilder.report(describer: Describer, state: GameState, change: GameStateChange.ShuffleDiscardIntoDeck) = change.apply {
        val discardDesc = describer.describePile(state, state.discard)
        val deckDesc = describer.describePile(state, state.deck)
        reportLine("${discardDesc.capitalize()} (${state.discard.cards.size}) was reshuffled into $deckDesc to refill it.")
    }

    private fun StringBuilder.report(describer: Describer, state: GameState, change: GameStateChange.Draw) = change.apply {
        val count = count!! // Count will always be set AFTER a Draw change is applied
        if (count > 0) {
            val deckDesc = describer.describePile(state, state.deck)
            val handDesc = describer.describePile(state, state.hand)
            if (count > 1) {
                reportLine("$count cards were drawn from $deckDesc into $handDesc.")
            } else {
                check(count == 1)
                reportLine("A card was drawn from $deckDesc into $handDesc.")
            }
        }
    }

    private fun StringBuilder.report(describer: Describer, state: GameState, change: GameStateChange.MoveCards) = change.apply {
        if (intoPile == state.graveyard) {
            reportLine("${cards.size} cards were removed from the game.")
        } else {
            val pileToDesc = describer.describePile(state, intoPile)
            cards
                .groupBy { card -> state.pileFor(card) }
                // Ignore requests which end up moving a card back into its own pile; it looks weird.
                // This could happen as part of a bigger collection of cards across piles being moved.
                .filter { (pile, _) -> pile == null || pile.id != intoPile.id }
                .forEach { (pile, cards) ->
                    if (cards.size > 1) {
                        if (pile != null) {
                            val pileFromDesc = describer.describePile(state, pile)
                            reportLine("${cards.size} cards moved from $pileFromDesc ${listStrategy.toDesc()} $pileToDesc.")
                        } else {
                            reportLine("${cards.size} cards were created and moved ${listStrategy.toDesc()} $pileToDesc.")
                        }
                    } else {
                        report(describer, state, GameStateChange.MoveCard(cards[0], intoPile, listStrategy))
                    }
                }
        }
    }

    private fun StringBuilder.report(describer: Describer, state: GameState, change: GameStateChange.MoveCard) = change.apply {
        val cardTitle = card.template.name

        if (intoPile == state.graveyard) {
            reportLine("$cardTitle was removed from the game.")
        } else {
            val pileToDesc = describer.describePile(state, intoPile)

            val pileFrom = state.pileFor(card)
            if (pileFrom != null) {
                // Ignore requests which end up moving a card within its own pile; it looks weird
                if (pileFrom.id != intoPile.id) {
                    val pileFromDesc = describer.describePile(state, pileFrom)
                    reportLine("$cardTitle was moved from $pileFromDesc ${listStrategy.toDesc()} $pileToDesc.")
                }
            } else {
                reportLine("$cardTitle was created and moved ${listStrategy.toDesc()} $pileToDesc.")
            }
        }
    }

    private fun StringBuilder.report(describer: Describer, state: GameState, change: GameStateChange.Shuffle) = change.apply {
        val pileDesc = describer.describePile(state, pile)
        reportLine("${pileDesc.capitalize()} was shuffled.")
    }

    private fun StringBuilder.report(describer: Describer, state: GameState, change: GameStateChange.AddCardAmount) = change.apply {
        // Only report changes for owned cards. If a card is in the store or jail, the user shouldn't know about what's happening to it.
        if (!card.isOwned(state)) return@apply

        when (property) {
            CardProperty.COUNTER -> {
                when {
                    amount > 0 -> reportLine("${card.template.name} added $amount counter(s).")
                    amount < 0 -> reportLine("${card.template.name} removed ${-amount} counter(s).")
                }
            }
            CardProperty.VP -> {
                when {
                    amount > 0 -> reportLine("${card.template.name} added ${describer.describeVictoryPoints(amount)}.")
                    amount < 0 -> reportLine("${card.template.name} lost ${describer.describeVictoryPoints(-amount)}.")
                }
            }
            CardProperty.VP_PASSIVE -> {
                when {
                    amount > 0 -> reportLine("${card.template.name} increased by ${describer.describeVictoryPoints(amount)}.")
                    amount < 0 -> reportLine("${card.template.name} decreased by ${describer.describeVictoryPoints(-amount)}.")
                }
            }
            else -> error("Unexpected card property: ${property}.")
        }
    }

    private fun StringBuilder.report(describer: Describer, change: GameStateChange.UpgradeCard) = change.apply {
        reportLine("${card.template.name} was upgraded, adding: ${describer.describeUpgradeTitle(upgradeType, icons = false)}.")
    }

    private fun StringBuilder.report(describer: Describer, change: GameStateChange.AddTrait) = change.apply {
        reportLine("${card.template.name} added: ${describer.describeTraitTitle(traitType)}.")
    }

    private fun StringBuilder.report(describer: Describer, change: GameStateChange.RemoveTrait) = change.apply {
        reportLine("${card.template.name} removed: ${describer.describeTraitTitle(traitType)}.")
    }

    private fun StringBuilder.report(describer: Describer, change: GameStateChange.AddBuildingAmount) = change.apply {
        val name = building.blueprint.name
        when (property) {
            BuildingProperty.COUNTER -> {
                when {
                    amount > 0 -> reportLine("$name added $amount counter(s).")
                    amount < 0 -> reportLine("$name removed ${-amount} counter(s).")
                }
            }
            BuildingProperty.VP -> {
                when {
                    amount > 0 -> reportLine("$name added ${describer.describeVictoryPoints(amount)}.")
                    amount < 0 -> reportLine("$name lost ${describer.describeVictoryPoints(-amount)}.")
                }
            }
            BuildingProperty.VP_PASSIVE -> {
                when {
                    amount > 0 -> reportLine("$name increased by ${describer.describeVictoryPoints(amount)}.")
                    amount < 0 -> reportLine("$name decreased by ${describer.describeVictoryPoints(-amount)}.")
                }
            }
            else -> error("Unexpected building property: ${property}.")
        }
    }

    private fun StringBuilder.report(change: GameStateChange.AddEffect) = change.apply {
        reportLine("You added the following effect:\n  ${effect.desc ?: effect.warningExpr}")
    }

    private fun StringBuilder.report(describer: Describer, tweak: Tweak) {
        // Report tweaks as "effects" since that reads better to the user.
        reportLine("You added the following effect:\n  ${describer.convertIcons(tweak.desc)}")
    }

    private fun StringBuilder.report(describer: Describer, change: GameStateChange.AddGameTweak) = change.apply {
        report(describer, tweak)
    }

    private fun StringBuilder.report(describer: Describer, change: GameStateChange.AddShopTweak) = change.apply {
        report(describer, tweak)
    }

    private fun StringBuilder.report(change: GameStateChange.RestockShop) = change.apply {
        reportLine("The shop was restocked.")
    }

    private fun StringBuilder.report(change: GameStateChange.UpgradeShop) = change.apply {
        reportLine("The shop was upgraded.")
    }

    private fun StringBuilder.report(change: GameStateChange.AddBlueprint) = change.apply {
        reportLine("You added the following blueprint: ${change.blueprint.name}.")
    }

    private fun StringBuilder.report(change: GameStateChange.Build) = change.apply {
        reportLine("${blueprint.name} was built.")
    }
}
