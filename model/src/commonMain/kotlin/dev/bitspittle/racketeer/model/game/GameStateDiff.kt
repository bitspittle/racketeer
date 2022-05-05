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

//    private fun StringBuilder.reportChangedCards() {
//        diff.changedCards.forEach { (cardBefore, cardAfter) ->
//            (cardAfter.upgrades - cardBefore.upgrades).takeIf { it.isNotEmpty() }?.let { upgrades ->
//                reportLine("${cardAfter.template.name} was upgraded, adding: ${describer.describeUpgradesTitle(upgrades)}")
//            }
//
//            (cardBefore.upgrades - cardAfter.upgrades).takeIf { it.isNotEmpty() }?.let { downgrades ->
//                reportLine(
//                    "${cardAfter.template.name} was downgraded, removing: ${
//                        describer.describeUpgradesTitle(
//                            downgrades
//                        )
//                    }"
//                )
//            }
//
//            (cardAfter.vpTotal - cardBefore.vpTotal).let { vpDiff ->
//                when {
//                    vpDiff > 0 -> reportLine("${cardAfter.template.name} gained ${describer.describeVictoryPoints(vpDiff)}.")
//                    vpDiff < 0 -> reportLine("${cardAfter.template.name} lost ${describer.describeVictoryPoints(-vpDiff)}.")
//                }
//            }
//
//            (cardAfter.counter - cardBefore.counter).let { counterDiff ->
//                when {
//                    counterDiff > 0 -> reportLine("${cardAfter.template.name} added $counterDiff counter(s).")
//                    counterDiff < 0 -> reportLine("${cardAfter.template.name} removed ${-counterDiff} counter(s).")
//                }
//            }
//        }
//    }
//
//    private fun StringBuilder.reportResources() {
//        when {
//            diff.cash > 0 -> reportLine("You earned ${describer.describeCash(diff.cash)}.")
//            diff.cash < 0 -> {
//                if (diff.after.turn == diff.before.turn) {
//                    reportLine("You spent ${describer.describeCash(-diff.cash)}.")
//                } else {
//                    check(diff.after.cash == 0)
//                    reportLine("You lost ${describer.describeCash(-diff.cash)} that wasn't spent by the end of your turn.")
//                }
//            }
//            else -> Unit
//        }
//
//        when {
//            diff.influence > 0 -> reportLine("You earned ${describer.describeInfluence(diff.influence)}.")
//            diff.influence < 0 -> reportLine("You spent ${describer.describeInfluence(-diff.influence)}.")
//        }
//
//        when {
//            diff.luck > 0 -> reportLine("You earned ${describer.describeLuck(diff.luck)}.")
//            diff.luck < 0 -> reportLine("You spent ${describer.describeLuck(-diff.luck)}.")
//        }
//
//        when {
//            diff.handSize > 0 -> reportLine("Your hand size grew from ${diff.before.handSize} to ${diff.after.handSize} cards.")
//            diff.handSize < 0 -> reportLine("Your hand size shrunk from ${diff.before.handSize} to ${diff.after.handSize} cards.")
//        }
//
//        when {
//            diff.vp > 0 -> reportLine("You gained ${describer.describeVictoryPoints(diff.vp)}.")
//            diff.vp < 0 -> reportLine("You lost ${describer.describeVictoryPoints(-diff.vp)}.")
//        }
//    }
//
//    @Suppress("NAME_SHADOWING")
//    private fun StringBuilder.reportMovedCards() {
//        // A reshuffle is an interesting event. It (likely) happened if cards moved from the discard pile into the
//        // deck and some cards also ended up in your hand on the same turn. I saw likely because we may end up creating
//        // a card that causes this to happen as well, which will be really confusing.
//        // TODO: Refactor GameState into a series of events instead of us guessing like this
//        val reshuffleTransfer = diff.before.discard to diff.after.deck
//        val deckToHand = diff.before.deck to diff.after.hand
//        val discardToHand = diff.before.discard to diff.after.hand
//        val reshuffleHappened = diff.movedCards.contains(reshuffleTransfer)
//                && (diff.movedCards.contains(discardToHand) || diff.movedCards.contains(deckToHand))
//
//        val movedCards = if (reshuffleHappened) {
//            // In the early game, your deck is so small that when you draw cards on the second round, you will
//            // trigger a discard reshuffle that goes into your deck and THEN into your hand. It's confusing to
//            // report this as "A card went from the discard pile into your hand" even though that's technically
//            // true. To the user, they think more the card should have gone from the discard pile into their
//            // deck, and *then* from the deck into their hand.
//            // We only do this interception if a reshuffle happened. Otherwise, maybe there will be a card whose
//            // effect is to recover a card from the discard pile.
//
//            val movedCardsCopy = diff.movedCards.toMutableMap()
//
//            // Let's report the discard shuffle as a custom message, so it shows up first and reads
//            reportLine("Your discard pile (${diff.before.discard.cards.size}) was reshuffled into your deck to refill it.")
//            movedCardsCopy.remove(reshuffleTransfer) // Don't report it again, below.
//
//            val discardToHandCards = movedCardsCopy.remove(diff.before.discard to diff.after.hand) ?: emptyList()
//            val reframedTransfer = diff.before.deck to diff.after.hand
//            movedCardsCopy[reframedTransfer] = (movedCardsCopy[reframedTransfer] ?: emptyList()) + discardToHandCards
//            check(movedCardsCopy.getValue(reframedTransfer).isNotEmpty())
//
//            movedCardsCopy
//        } else {
//            diff.movedCards
//        }
//
//        movedCards
//            .forEach { (transfer, cards) ->
//                val (pileFrom, pileTo) = transfer
//                val pileFromDesc = describer.describePile(diff.before, pileFrom)
//                val pileToDesc = describer.describePile(diff.after, pileTo)
//                if (cards.size > 1) {
//                    reportLine("${cards.size} cards moved from $pileFromDesc into $pileToDesc.")
//                } else {
//                    val card = cards.single()
//                    val cardTitle = describer.describeCard(card, concise = true)
//                    reportLine("$cardTitle moved from $pileFromDesc into $pileToDesc.")
//                }
//            }
//    }
//
//    private fun StringBuilder.reportCreatedAndDestroyedCards() {
//        // When a game starts, we create an initial cards, but we shouldn't report that here. These updates are meant
//        // for cards created via explicit user actions.
//        if (diff.before.getOwnedCards().isNotEmpty()) {
//            diff.createdCards.sortedBy { it.template.name }.forEach { card ->
//                val cardTitle = describer.describeCard(card, concise = true)
//                val pileDesc = describer.describePile(diff.after, diff.after.pileFor(card)!!)
//                reportLine("$cardTitle was created and placed into $pileDesc.")
//            }
//        } else {
//            // For the initial turn, when we put cards into the user's hand, because there's no "before" state, we don't
//            // end up reporting it in `reportMovedCards`, so report it here as if there was always a deck.
//            val cards = diff.createdCards.filter { card -> diff.after.pileFor(card) == diff.after.hand }
//            val pileFromDesc = describer.describePile(diff.after, diff.after.deck)
//            val pileToDesc = describer.describePile(diff.after, diff.after.hand)
//            reportLine("${cards.size} cards moved from $pileFromDesc into $pileToDesc.")
//        }
//
//        diff.destroyedCards.sortedBy { it.template.name }.forEach { card ->
//            val cardTitle = describer.describeCard(card, concise = true)
//            reportLine("$cardTitle was removed from the game.")
//        }
//    }
//}
