package dev.bitspittle.racketeer.model.game

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.text.Describer

class GameStateDiff(
    private val describer: Describer,
    private val gameStateBefore: GameState,
    private val gameStateAfter: GameState
) {
    init {
        require(gameStateBefore !== gameStateAfter)
    }

    fun reportTo(logger: Logger) {
        val report = buildString {
            val allCardsBefore = (gameStateBefore.allPiles.flatMap { it.cards }).associateBy { it.id }
            val allCardsAfter = (gameStateAfter.allPiles.flatMap { it.cards }).associateBy { it.id }
            reportMovedCards(allCardsBefore, allCardsAfter)
            reportCreatedAndDestroyedCards(allCardsBefore, allCardsAfter)
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
        (gameStateAfter.cash - gameStateBefore.cash).let { cashDiff ->
            when {
                cashDiff > 0 -> reportLine("You earned ${describer.describeCash(cashDiff)}.")
                cashDiff < 0 -> {
                    if (gameStateAfter.turn == gameStateBefore.turn) {
                        reportLine("You spent ${describer.describeCash(-cashDiff)}.")
                    } else {
                        check(gameStateAfter.cash == 0)
                        reportLine("You lost ${describer.describeCash(-cashDiff)} that wasn't spent by the end of your turn.")
                    }
                }
                else -> Unit
            }
        }

        (gameStateAfter.influence - gameStateBefore.influence).let { influenceDiff ->
            when {
                influenceDiff > 0 -> reportLine("You earned ${describer.describeInfluence(influenceDiff)}.")
                influenceDiff < 0 -> reportLine("You spent ${describer.describeInfluence(-influenceDiff)}.")
                else -> Unit
            }
        }

        (gameStateAfter.luck - gameStateBefore.luck).let { luckDiff ->
            when {
                luckDiff > 0 -> reportLine("You earned ${describer.describeLuck(luckDiff)}.")
                luckDiff < 0 -> reportLine("You spent ${describer.describeLuck(-luckDiff)}.")
                else -> Unit
            }
        }

        (gameStateAfter.vp - gameStateBefore.vp).let { vpDiff ->
            when {
                vpDiff > 0 -> reportLine("You gained ${describer.describeVictoryPoints(vpDiff)}.")
                vpDiff < 0 -> reportLine("You lost ${describer.describeVictoryPoints(-vpDiff)}.")
                else -> Unit
            }
        }
    }

    private fun StringBuilder.reportMovedCards(
        allCardsBefore: Map<Uuid, Card>,
        allCardsAfter: Map<Uuid, Card>
    ) {
        // List of all cards that moved from some pile -> pile
        val pileTransfers = allCardsAfter
            .asSequence()
            .filter { (id, _) -> allCardsBefore.contains(id) }
            .map { (_, card) -> card }
            .groupBy { card ->
                gameStateBefore.pileFor(card)!! to gameStateAfter.pileFor(card)!!
            }
            .filter { (transfer, cards) ->
                transfer.first.id != transfer.second.id
            }

        val reshuffleTransfer = gameStateBefore.discard to gameStateAfter.deck
        var reshuffleHappened = false
        // Report discard reshuffling in aggregate, since it's not very interesting to see 20 "X moved from discard
        // to your deck" notifications when you draw cards in the late game
        pileTransfers[reshuffleTransfer]?.let {
            reportLine("Your discard pile (${gameStateBefore.discard.cards.size}) was reshuffled into your deck to refill it.")
            reshuffleHappened = true
        }

        pileTransfers
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
                    if (transfer.first == gameStateBefore.discard && reshuffleHappened) {
                        gameStateBefore.deck to transfer.second
                    } else transfer
                }
                cards.forEach { card ->
                    val cardTitle = describer.describe(card, concise = true)
                    val pileFromDesc = describer.describe(gameStateBefore, pileFrom, title = false)
                    val pileToDesc = describer.describe(gameStateAfter, pileTo, title = false)
                    reportLine("$cardTitle moved from $pileFromDesc into $pileToDesc.")
                }
            }
    }

    private fun StringBuilder.reportCreatedAndDestroyedCards(
        allCardsBefore: Map<Uuid, Card>,
        allCardsAfter: Map<Uuid, Card>
    ) {
        allCardsAfter.filter { (id, _) -> !allCardsBefore.contains(id) }.let { createdCards ->
            createdCards.values.sortedBy { it.template.name }.forEach { card ->
                val cardTitle = describer.describe(card, concise = true)
                val pileDesc = describer.describe(gameStateAfter, gameStateAfter.pileFor(card)!!, title = false)
                reportLine("$cardTitle was created and placed into $pileDesc.")
            }
        }

        allCardsBefore.filter { (id, _) -> !allCardsAfter.contains(id) }.let { destroyedCards ->
            destroyedCards.values.sortedBy { it.template.name }.forEach { card ->
                val cardTitle = describer.describe(card, concise = true)
                reportLine("$cardTitle was removed from the game.")
            }
        }
    }
}