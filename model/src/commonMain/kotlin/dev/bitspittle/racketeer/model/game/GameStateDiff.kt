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
    fun reportTo(logger: Logger) {
        val report = buildString {
            diff.changes.forEach { change ->
                when (change) {
                    is GameStateDelta.Init -> Unit // No need to report
                    is GameStateDelta.ShuffleDiscardIntoDeck -> {
                        reportLine("Your discard pile (${diff.before.discard.cards.size}) was reshuffled into your deck to refill it.")
                    }
                    is GameStateDelta.Draw -> Unit // No need to report, it will be handled by MoveCards
                    is GameStateDelta.MoveCards -> {
                        val pileToDesc = describer.describePile(diff.after, change.intoPile)
                        change.cards
                            .groupBy { card -> diff.after.pileFor(card) }
                            .forEach { (pile, cards) ->
                                if (pile != null) {
                                    val pileFromDesc = describer.describePile(diff.after, diff.after.deck)
                                    reportLine("${cards.size} card(s) moved from $pileFromDesc into $pileToDesc.")
                                } else {
                                    reportLine("${cards.size} card(s) were created and moved into $pileToDesc.")
                                }
                            }
                    }
                    is GameStateDelta.Play -> {} // No need to report
                    is GameStateDelta.MoveCard -> {
                        val cardTitle = change.card.template.name
                        val pileToDesc = describer.describePile(diff.after, change.intoPile)

                        val pileFrom = diff.before.pileFor(change.card)
                        if (pileFrom != null) {
                            val pileFromDesc = describer.describePile(diff.after, pileFrom)
                            reportLine("$cardTitle was moved from $pileFromDesc into $pileToDesc.")
                        } else {
                            reportLine("$cardTitle was created and moved into $pileToDesc.")
                        }
                    }
                    is GameStateDelta.RemoveCards -> {
                        change.cards.forEach { card ->
                            val cardTitle = card.template.name
                            reportLine("$cardTitle was removed from the game.")
                        }
                    }
                    is GameStateDelta.AddCardAmount -> {
                        when (change.property) {
                            CardProperty.COUNTER -> {
                                when {
                                    change.amount > 0 -> reportLine("${change.card.template.name} added ${change.amount} counter(s).")
                                    change.amount < 0 -> reportLine("${change.card.template.name} removed ${-change.amount} counter(s).")
                                }
                            }
                            CardProperty.VP -> {
                                when {
                                    change.amount > 0 -> reportLine("${change.card.template.name} added ${change.amount} victory point(s).")
                                    change.amount < 0 -> reportLine("${change.card.template.name} lost ${-change.amount} victory point(s).")
                                }
                            }
                            CardProperty.VP_PASSIVE -> {
                                when {
                                    change.amount > 0 -> reportLine("${change.card.template.name} grew by ${change.amount} victory point(s).")
                                    change.amount < 0 -> reportLine("${change.card.template.name} shrunk by ${-change.amount} victory point(s).")
                                }
                            }
                            else -> error("Unexpected card property: ${change.property}")
                        }
                    }
                    is GameStateDelta.UpgradeCard -> {
                        reportLine("${change.card.template.name} was upgraded, adding: ${change.upgradeType}.")
                    }
                    is GameStateDelta.AddGameAmount -> {
                        when (change.property) {
                            GameProperty.CASH -> {
                                when {
                                    change.amount > 0 -> reportLine("You earned ${describer.describeCash(change.amount)}.")
                                    change.amount < 0 -> reportLine("You spent ${describer.describeCash(-change.amount)}.")
                                }
                            }
                            GameProperty.INFLUENCE -> {
                                when {
                                    change.amount > 0 -> reportLine("You earned ${describer.describeInfluence(change.amount)}.")
                                    change.amount < 0 -> reportLine("You spent ${describer.describeInfluence(-change.amount)}.")
                                }
                            }
                            GameProperty.LUCK -> {
                                when {
                                    change.amount > 0 -> reportLine("You earned ${describer.describeLuck(change.amount)}.")
                                    change.amount < 0 -> reportLine("You spent ${describer.describeLuck(-change.amount)}.")
                                }
                            }
                            else -> error("Unexpected game property: ${change.property}.")
                        }
                    }
                    is GameStateDelta.AddStreetEffect -> {
                        reportLine("You added the following effect onto the street: ${change.effect.desc}.")
                    }
                    is GameStateDelta.AddShopExclusion -> {} // No need to report
                    is GameStateDelta.RestockShop -> {
                        reportLine("The shop was restocked.")
                    }
                    is GameStateDelta.UpgradeShop -> {
                        reportLine("The shop was upgraded.")
                    }
                    is GameStateDelta.EndTurn -> Unit // No need to report
                    is GameStateDelta.GameOver -> Unit // No need to report
                }
            }

            // VP is passively calculated, so we have to manually check it
            (diff.after.vp - diff.before.vp).let { vpDiff ->
                when {
                    vpDiff > 0 -> reportLine("You gained ${describer.describeVictoryPoints(vpDiff)}.")
                    vpDiff < 0 -> reportLine("You lost ${describer.describeVictoryPoints(-vpDiff)}.")
                }
            }

            // Hand size looks better calculated as absolutes than relative amounts:
            (diff.after.handSize - diff.before.handSize).let { handSizeDiff ->
                when {
                    handSizeDiff > 0 -> reportLine("Your hand size grew from ${diff.before.handSize} to ${diff.after.handSize} cards.")
                    handSizeDiff < 0 -> reportLine("Your hand size shrunk from ${diff.before.handSize} to ${diff.after.handSize} cards.")
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
