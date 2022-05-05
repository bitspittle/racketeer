package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.text.Describer

/** Create a diff between two snapshots of a game state in time, useful for reporting changes to the user */
@Suppress("JoinDeclarationAndAssignment")
class GameStateDiff(val before: GameState, val after: GameState) {
    fun hasNoChanges(): Boolean {
        return before.history.size == after.history.size
    }

    val changes = after.history.drop(before.history.size)
}

fun GameStateDiff.reportTo(describer: Describer, logger: Logger) {
    GameStateDiffReporter(describer, this).reportTo(logger)
}

private class GameStateDiffReporter(
    private val describer: Describer,
    private val diff: GameStateDiff
) {
    private fun StringBuilder.report(delta: GameStateDelta.ShuffleDiscardIntoDeck) = delta.apply {
        reportLine("Your discard pile (${diff.before.discard.cards.size}) was reshuffled into your deck to refill it.")
    }

    private fun StringBuilder.report(delta: GameStateDelta.MoveCards) = delta.apply {
        val pileToDesc = describer.describePile(diff.after, intoPile)
        if (cards.size > 1) {
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
        } else {
            report(GameStateDelta.MoveCard(cards.first(), intoPile, listStrategy))
        }
    }

    private fun StringBuilder.report(delta: GameStateDelta.MoveCard) = delta.apply {
        val cardTitle = delta.card.template.name
        val pileToDesc = describer.describePile(diff.after, delta.intoPile)

        val pileFrom = diff.before.pileFor(delta.card)
        if (pileFrom != null) {
            val pileFromDesc = describer.describePile(diff.before, pileFrom)
            reportLine("$cardTitle was moved from $pileFromDesc into $pileToDesc.")
        } else {
            reportLine("$cardTitle was created and moved into $pileToDesc.")
        }
    }

    private fun StringBuilder.report(delta: GameStateDelta.RemoveCards) = delta.apply {
        delta.cards.forEach { card ->
            val cardTitle = card.template.name
            reportLine("$cardTitle was removed from the game.")
        }
    }

    private fun StringBuilder.report(delta: GameStateDelta.AddCardAmount) = delta.apply {
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

    private fun StringBuilder.report(delta: GameStateDelta.UpgradeCard) = delta.apply {
        reportLine("${card.template.name} was upgraded, adding: ${upgradeType}.")
    }

    private fun StringBuilder.report(delta: GameStateDelta.AddGameAmount) = delta.apply {
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

    private fun StringBuilder.report(delta: GameStateDelta.AddStreetEffect) = delta.apply {
        reportLine("You added the following effect onto the street: ${effect.desc}.")
    }

    private fun StringBuilder.report(delta: GameStateDelta.RestockShop) = delta.apply {
        reportLine("The shop was restocked.")
    }

    private fun StringBuilder.report(delta: GameStateDelta.UpgradeShop) = delta.apply {
        reportLine("The shop was upgraded.")
    }

    fun reportTo(logger: Logger) {
        val report = buildString {
            diff.changes.forEach { change ->
                when (change) {
                    is GameStateDelta.Init -> Unit // Background magic, should be invisible to the user
                    is GameStateDelta.ShuffleDiscardIntoDeck -> report(change)
                    is GameStateDelta.Draw -> Unit // No need to report, it will be handled by MoveCards
                    is GameStateDelta.MoveCards -> report(change)
                    is GameStateDelta.Play -> {} // No need to report, obvious from user actions
                    is GameStateDelta.MoveCard -> report(change)
                    is GameStateDelta.RemoveCards -> report(change)
                    is GameStateDelta.AddCardAmount -> report(change)
                    is GameStateDelta.UpgradeCard -> report(change)
                    is GameStateDelta.AddGameAmount -> report(change)
                    is GameStateDelta.AddStreetEffect -> report(change)
                    is GameStateDelta.AddShopExclusion -> Unit // Background magic, should be invisible to the user
                    is GameStateDelta.RestockShop -> report(change)
                    is GameStateDelta.UpgradeShop -> report(change)
                    is GameStateDelta.EndTurn -> Unit // No need to report, obvious from user actions
                    is GameStateDelta.GameOver -> Unit // No need to report, just a marker change
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
