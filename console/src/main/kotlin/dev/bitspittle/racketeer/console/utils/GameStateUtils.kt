package dev.bitspittle.racketeer.console.utils

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.notifyOwnership
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.GameStateDiff
import dev.bitspittle.racketeer.model.game.getOwnedCards
import dev.bitspittle.racketeer.model.game.reportTo
import dev.bitspittle.racketeer.scripting.types.CancelPlayException

/**
 * Run some actions that are expected to change the game state somehow and, if so, report it.
 *
 * If the block triggered by this method fails for any reason, the previous state will be restored.
 */
suspend fun GameContext.runStateChangingAction(block: suspend GameContext.() -> Unit) {
    val prevState = this.state
    val nextState = prevState.copy()

    env.scoped {
        try {
            state = nextState
            block().also { GameStateDiff(prevState, nextState).reportTo(describer, app.logger) }

            state.changes.forEach { change ->
                when (change) {
                    is GameStateChange.GameStarted -> {
                        state.getOwnedCards().forEach { card -> cardStats.notifyOwnership(card) }
                    }
                    is GameStateChange.MoveCard -> {
                        if (state.pileFor(change.card) == null) {
                            cardStats.notifyOwnership(change.card)
                        }
                    }
                    is GameStateChange.MoveCards -> {
                        change.cards.forEach { card ->
                            if (state.pileFor(card) == null) {
                                cardStats.notifyOwnership(card)
                            }
                        }
                    }
                    else -> Unit // Doesn't affect card stats
                }
            }
        } catch (ex: Exception) {
            state = prevState
            @Suppress("KotlinConstantConditions") // IntelliJ's warning is wrong here
            if (ex !is EvaluationException || ex.cause !is CancelPlayException) {
                throw ex
            }
        }
        finally {
            viewStack.currentView.refreshCommands()
        }
    }
}