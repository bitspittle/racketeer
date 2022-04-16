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
                cashDiff > 0 -> reportLine("You earned ${describer.describeCash(cashDiff)}")
                cashDiff < 0 -> reportLine("You spent ${describer.describeCash(-cashDiff)}")
                else -> Unit
            }
        }

        (gameStateAfter.influence - gameStateBefore.influence).let { influenceDiff ->
            when {
                influenceDiff > 0 -> reportLine("You earned ${describer.describeInfluence(influenceDiff)}")
                influenceDiff < 0 -> reportLine("You spent ${describer.describeInfluence(-influenceDiff)}")
                else -> Unit
            }
        }

        (gameStateAfter.luck - gameStateBefore.luck).let { luckDiff ->
            when {
                luckDiff > 0 -> reportLine("You earned ${describer.describeLuck(luckDiff)}")
                luckDiff < 0 -> reportLine("You spent ${describer.describeLuck(-luckDiff)}")
                else -> Unit
            }
        }

        (gameStateAfter.vp - gameStateBefore.vp).let { vpDiff ->
            when {
                vpDiff > 0 -> reportLine("You gained ${describer.describeVictoryPoints(vpDiff)}")
                vpDiff < 0 -> reportLine("You lost ${describer.describeVictoryPoints(-vpDiff)}")
                else -> Unit
            }
        }
    }

    private fun StringBuilder.reportMovedCards(
        allCardsBefore: Map<Uuid, Card>,
        allCardsAfter: Map<Uuid, Card>
    ) {
        allCardsAfter
            .asSequence()
            .filter { (id, _) -> allCardsBefore.contains(id) }
            .filter { (id, card) ->
                gameStateBefore.pileFor(allCardsBefore.getValue(id))!!.id != gameStateAfter.pileFor(card)!!.id
            }.map { (_, card) -> card }
            .forEach { card ->
                val cardTitle = describer.describe(card, concise = true)
                val pileFromDesc = describer.describe(gameStateBefore, gameStateBefore.pileFor(card)!!, title = false)
                val pileToDesc = describer.describe(gameStateAfter, gameStateAfter.pileFor(card)!!, title = false)
                reportLine("\"$cardTitle\" moved from $pileFromDesc to $pileToDesc.")
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
                reportLine("\"$cardTitle\" was created and placed into $pileDesc.")
            }
        }

        allCardsBefore.filter { (id, _) -> !allCardsAfter.contains(id) }.let { destroyedCards ->
            destroyedCards.values.sortedBy { it.template.name }.forEach { card ->
                val cardTitle = describer.describe(card, concise = true)
                reportLine("\"$cardTitle\" was removed from the game.")
            }
        }
    }
}