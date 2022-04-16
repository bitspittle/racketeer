package dev.bitspittle.racketeer.model.game

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.shop.MutableShop
import dev.bitspittle.racketeer.model.shop.Shop
import dev.bitspittle.racketeer.model.text.Describer
import kotlin.math.max
import kotlin.random.Random

class GameStateDiff(private val describer: Describer, private val gameStateBefore: GameState, private val gameStateAfter: GameState) {
    init {
        require(gameStateBefore !== gameStateAfter)
    }

    fun reportTo(logger: Logger) {
        val report = buildString {
            (gameStateAfter.cash - gameStateBefore.cash).let { cashDiff ->
                when {
                    cashDiff > 0 -> appendLine("You earned ${describer.describeCash(cashDiff)}")
                    cashDiff < 0 -> appendLine("You spent ${describer.describeCash(-cashDiff)}")
                    else -> Unit
                }
            }

            (gameStateAfter.influence - gameStateBefore.influence).let { influenceDiff ->
                when {
                    influenceDiff > 0 -> appendLine("You earned ${describer.describeInfluence(influenceDiff)}")
                    influenceDiff < 0 -> appendLine("You spent ${describer.describeInfluence(-influenceDiff)}")
                    else -> Unit
                }
            }

            (gameStateAfter.luck - gameStateBefore.luck).let { luckDiff ->
                when {
                    luckDiff > 0 -> appendLine("You earned ${describer.describeLuck(luckDiff)}")
                    luckDiff < 0 -> appendLine("You spent ${describer.describeLuck(-luckDiff)}")
                    else -> Unit
                }
            }

            (gameStateAfter.vp - gameStateBefore.vp).let { vpDiff ->
                when {
                    vpDiff > 0 -> appendLine("You gained ${describer.describeVictoryPoints(vpDiff)}")
                    vpDiff < 0 -> appendLine("You lost ${describer.describeVictoryPoints(-vpDiff)}")
                    else -> Unit
                }
            }

//            val allCardsBefore = (gameStateBefore.getOwnedCards() + gameStateBefore.jail.cards).toSet()
//            val allCardsAfter = (gameStateAfter.getOwnedCards() + gameStateAfter.jail.cards).toSet()
//
//            val allCardIdsBefore = allCardsBefore.map { it.id }
//            val allCardIdsAfter = allCardsAfter.map { it.id }
        }
        if (report.isNotEmpty()) {
            logger.info(report)
        }
    }
}