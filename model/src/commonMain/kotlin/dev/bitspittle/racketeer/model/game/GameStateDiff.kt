package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.text.Describer

/** Create a diff between two snapshots of a game state in time, useful for reporting changes to the user */
@Suppress("JoinDeclarationAndAssignment")
class GameStateDiff(val before: GameState, val after: GameState) {
    companion object {
        fun areEquivalent(before: GameState, after: GameState): Boolean {
            return GameStateDiff(before, after).hasNoChanges()
        }
    }

    fun hasNoChanges(): Boolean {
        return cash == 0
                && influence == 0
                && luck == 0
                && vp == 0
                && movedCards.isEmpty()
                && createdCards.isEmpty()
                && destroyedCards.isEmpty()
    }

    val cash: Int
    val influence: Int
    val luck: Int
    val vp: Int
    val movedCards: Map<Pair<Pile, Pile>, List<Card>>
    val createdCards: List<Card>
    val destroyedCards: List<Card>

    init {
        cash = after.cash - before.cash
        influence = after.influence - before.influence
        luck = after.luck - before.luck
        vp = after.vp - before.vp

        val allCardsBefore = (before.allPiles.flatMap { it.cards }).associateBy { it.id }
        val allCardsAfter = (after.allPiles.flatMap { it.cards }).associateBy { it.id }

        movedCards = allCardsAfter
            .asSequence()
            .filter { (id, _) -> allCardsBefore.contains(id) }
            .map { (_, card) -> card }
            .groupBy { card ->
                before.pileFor(card)!! to after.pileFor(card)!!
            }
            .filter { (transfer, _) ->
                transfer.first.id != transfer.second.id
            }

        createdCards = allCardsAfter.filter { (id, _) -> !allCardsBefore.contains(id) }.map { (_, card) -> card }
        destroyedCards = allCardsBefore.filter { (id, _) -> !allCardsAfter.contains(id) }.map { (_, card) -> card }
    }
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
            reportMovedCards()
            reportCreatedAndDestroyedCards()
            reportResources()
        }
        if (report.isNotEmpty()) {
            logger.info(report)
        }
    }

    private fun StringBuilder.reportLine(message: String) {
        appendLine("- $message")
    }

    private fun StringBuilder.reportResources() {
        when {
            diff.cash > 0 -> reportLine("You earned ${describer.describeCash(diff.cash)}.")
            diff.cash < 0 -> {
                if (diff.after.turn == diff.before.turn) {
                    reportLine("You spent ${describer.describeCash(-diff.cash)}.")
                } else {
                    check(diff.after.cash == 0)
                    reportLine("You lost ${describer.describeCash(-diff.cash)} that wasn't spent by the end of your turn.")
                }
            }
            else -> Unit
        }

        when {
            diff.influence > 0 -> reportLine("You earned ${describer.describeInfluence(diff.influence)}.")
            diff.influence < 0 -> reportLine("You spent ${describer.describeInfluence(-diff.luck)}.")
        }

        when {
            diff.luck > 0 -> reportLine("You earned ${describer.describeLuck(diff.luck)}.")
            diff.luck < 0 -> reportLine("You spent ${describer.describeLuck(-diff.luck)}.")
        }

        when {
            diff.vp > 0 -> reportLine("You gained ${describer.describeLuck(diff.vp)}.")
            diff.vp < 0 -> reportLine("You lost ${describer.describeLuck(-diff.vp)}.")
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun StringBuilder.reportMovedCards() {
        val reshuffleTransfer = diff.before.discard to diff.after.deck
        var reshuffleHappened = false
        // Report discard reshuffling in aggregate, since it's not very interesting to see 20 "X moved from discard
        // to your deck" notifications when you draw cards in the late game
        diff.movedCards[reshuffleTransfer]?.let {
            reportLine("Your discard pile (${diff.before.discard.cards.size}) was reshuffled into your deck to refill it.")
            reshuffleHappened = true
        }

        diff.movedCards
            .filter { (transfer, _) -> transfer != reshuffleTransfer }
            .forEach { (transfer, cards) ->
                val (pileFrom, pileTo) = transfer.let { transfer ->
                    // In the early game, your deck is so small that when you draw cards on the second round, you will
                    // trigger a discard reshuffle that goes into your deck and THEN into your hand. It's confusing to
                    // report this as "A card went from the discard pile into your hand" even though that's technically
                    // true. But to the user, they think more the card should have gone from the discard pile into their
                    // deck, and then the deck into their hand.
                    // We only do this interception if a reshuffle happened. Otherwise, maybe there will be a card whose
                    // effect is to recover a card from the discard pile.
                    if (transfer.first == diff.before.discard && reshuffleHappened) {
                        diff.after.deck to transfer.second
                    } else transfer
                }
                cards.forEach { card ->
                    val cardTitle = describer.describe(card, concise = true)
                    val pileFromDesc = describer.describe(diff.before, pileFrom, title = false)
                    val pileToDesc = describer.describe(diff.after, pileTo, title = false)
                    reportLine("$cardTitle moved from $pileFromDesc into $pileToDesc.")
                }
            }
    }

    private fun StringBuilder.reportCreatedAndDestroyedCards() {
        diff.createdCards.sortedBy { it.template.name }.forEach { card ->
            val cardTitle = describer.describe(card, concise = true)
            val pileDesc = describer.describe(diff.after, diff.after.pileFor(card)!!, title = false)
            reportLine("$cardTitle was created and placed into $pileDesc.")
        }

        diff.destroyedCards.sortedBy { it.template.name }.forEach { card ->
            val cardTitle = describer.describe(card, concise = true)
            reportLine("$cardTitle was removed from the game.")
        }
    }
}
