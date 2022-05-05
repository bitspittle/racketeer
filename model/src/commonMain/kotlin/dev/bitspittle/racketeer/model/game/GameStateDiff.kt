package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.text.Describer

/** Create a diff between two snapshots of a game state in time, useful for reporting changes to the user */
@Suppress("JoinDeclarationAndAssignment")
class GameStateDiff(val before: GameState, val after: GameState)

fun GameStateDiff.hasNoChanges() = after.changes.isEmpty()

fun GameStateDiff.reportTo(describer: Describer, logger: Logger) {
    GameStateDiffReporter(describer, this).reportTo(logger)
}

private class GameStateDiffReporter(
    private val describer: Describer,
    private val diff: GameStateDiff
) {
    private fun StringBuilder.report(change: GameStateChange.ShuffleDiscardIntoDeck) = change.apply {
        val discardDesc = describer.describePile(diff.before, diff.before.discard)
        val deckDesc = describer.describePile(diff.after, diff.after.deck)
        reportLine("${discardDesc.capitalize()} (${diff.before.discard.cards.size}) was reshuffled into $deckDesc to refill it.")
    }

    private fun StringBuilder.report(change: GameStateChange.Draw) = change.apply {
        if (count > 0) {
            val deckDesc = describer.describePile(diff.before, diff.before.deck)
            val handDesc = describer.describePile(diff.after, diff.after.hand)
            reportLine("$count card(s) were drawn from $deckDesc into $handDesc.")
        }
    }

    private fun StringBuilder.report(change: GameStateChange.MoveCards) = change.apply {
        val pileToDesc = describer.describePile(diff.after, intoPile)
        cards
            .groupBy { card -> diff.before.pileFor(card) }
            .forEach { (pile, cards) ->
                if (pile != null) {
                    val pileFromDesc = describer.describePile(diff.before, pile)
                    reportLine("${cards.size} cards moved from $pileFromDesc into $pileToDesc.")
                } else {
                    reportLine("${cards.size} cards were created and moved into $pileToDesc.")
                }
            }
    }

    private fun StringBuilder.report(change: GameStateChange.MoveCard) = change.apply {
        val cardTitle = card.template.name
        val pileToDesc = describer.describePile(diff.after, intoPile)

        val pileFrom = diff.before.pileFor(card)
        if (pileFrom != null) {
            val pileFromDesc = describer.describePile(diff.before, pileFrom)
            reportLine("$cardTitle was moved from $pileFromDesc into $pileToDesc.")
        } else {
            reportLine("$cardTitle was created and moved into $pileToDesc.")
        }
    }

    private fun StringBuilder.report(change: GameStateChange.Shuffle) = change.apply {
        val pileDesc = describer.describePile(diff.after, pile)
        reportLine("${pileDesc.capitalize()} was shuffled.")
    }

    private fun StringBuilder.report(change: GameStateChange.RemoveCards) = change.apply {
        cards.forEach { card ->
            val cardTitle = card.template.name
            reportLine("$cardTitle was removed from the game.")
        }
    }

    private fun StringBuilder.report(change: GameStateChange.AddCardAmount) = change.apply {
        when (property) {
            CardProperty.COUNTER -> {
                when {
                    amount > 0 -> reportLine("${card.template.name} added $amount counter(s).")
                    amount < 0 -> reportLine("${card.template.name} removed ${-amount} counter(s).")
                }
            }
            CardProperty.VP -> {
                when {
                    amount > 0 -> reportLine("${card.template.name} added $amount victory point(s).")
                    amount < 0 -> reportLine("${card.template.name} lost ${-amount} victory point(s).")
                }
            }
            CardProperty.VP_PASSIVE -> {
                when {
                    amount > 0 -> reportLine("${card.template.name} grew by $amount victory point(s).")
                    amount < 0 -> reportLine("${card.template.name} shrunk by ${-amount} victory point(s).")
                }
            }
            else -> error("Unexpected card property: ${property}.")
        }
    }

    private fun StringBuilder.report(change: GameStateChange.UpgradeCard) = change.apply {
        reportLine("${card.template.name} was upgraded, adding: ${upgradeType}.")
    }

    private fun StringBuilder.report(change: GameStateChange.AddGameAmount) = change.apply {
        when (property) {
            GameProperty.CASH -> {
                when {
                    amount > 0 -> reportLine("You earned ${describer.describeCash(amount)}.")
                    amount < 0 -> reportLine("You spent ${describer.describeCash(-amount)}.")
                }
            }
            GameProperty.INFLUENCE -> {
                when {
                    amount > 0 -> reportLine("You earned ${describer.describeInfluence(amount)}.")
                    amount < 0 -> reportLine("You spent ${describer.describeInfluence(-amount)}.")
                }
            }
            GameProperty.LUCK -> {
                when {
                    amount > 0 -> reportLine("You earned ${describer.describeLuck(amount)}.")
                    amount < 0 -> reportLine("You spent ${describer.describeLuck(-amount)}.")
                }
            }
            else -> error("Unexpected game property: ${property}.")
        }
    }

    private fun StringBuilder.report(change: GameStateChange.AddStreetEffect) = change.apply {
        reportLine("You added the following effect onto the street: ${effect.desc}.")
    }

    private fun StringBuilder.report(change: GameStateChange.RestockShop) = change.apply {
        reportLine("The shop was restocked.")
    }

    private fun StringBuilder.report(change: GameStateChange.UpgradeShop) = change.apply {
        reportLine("The shop was upgraded.")
    }

    fun reportTo(logger: Logger) {
        val report = buildString {
            val changes = diff.after.changes.toMutableList()

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

            changes.forEach { change ->
                when (change) {
                    is GameStateChange.GameStarted -> Unit // Marker game state, no need to report
                    is GameStateChange.ShuffleDiscardIntoDeck -> report(change)
                    is GameStateChange.Draw -> report(change)
                    is GameStateChange.Play -> {} // No need to report, obvious from user actions
                    is GameStateChange.MoveCards -> report(change)
                    is GameStateChange.MoveCard -> report(change)
                    is GameStateChange.Shuffle -> report(change)
                    is GameStateChange.RemoveCards -> report(change)
                    is GameStateChange.AddCardAmount -> report(change)
                    is GameStateChange.UpgradeCard -> report(change)
                    is GameStateChange.AddGameAmount -> report(change)
                    is GameStateChange.AddStreetEffect -> report(change)
                    is GameStateChange.AddShopExclusion -> Unit // Background magic, should be invisible to the user
                    is GameStateChange.RestockShop -> report(change)
                    is GameStateChange.UpgradeShop -> report(change)
                    is GameStateChange.EndTurn -> Unit // No need to report, obvious from user actions
                    is GameStateChange.GameOver -> Unit // Marker game state, no need to report
                }
            }

            // Hand size message looks better when presented as a diff (absolute is more interesting than relative):
            (diff.after.handSize - diff.before.handSize).let { handSizeDiff ->
                when {
                    handSizeDiff > 0 -> reportLine("Your hand size grew from ${diff.before.handSize} to ${diff.after.handSize} cards.")
                    handSizeDiff < 0 -> reportLine("Your hand size shrunk from ${diff.before.handSize} to ${diff.after.handSize} cards.")
                }
            }

            // VP is passively calculated, so we have to manually check it
            (diff.after.vp - diff.before.vp).let { vpDiff ->
                when {
                    vpDiff > 0 -> reportLine("You gained ${describer.describeVictoryPoints(vpDiff)}.")
                    vpDiff < 0 -> reportLine("You lost ${describer.describeVictoryPoints(-vpDiff)}.")
                }
            }
        }

        if (report.isNotEmpty()) {
            logger.info(report)
        }
    }

    private fun StringBuilder.reportLine(message: String) {
        appendLine("- $message")
    }
}
