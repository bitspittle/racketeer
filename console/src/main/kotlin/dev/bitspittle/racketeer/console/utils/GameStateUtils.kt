package dev.bitspittle.racketeer.console.utils

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.notifyOwnership
import dev.bitspittle.racketeer.console.view.views.game.play.PreDrawView
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.scripting.types.CancelPlayException

fun GameContext.createNewGame() = MutableGameState(data, enqueuers)

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
            block().also {
                nextState.onBoardChanged()
                GameStateDiff(prevState, nextState).reportTo(describer, app.logger)
            }

            // Notify ownership if a card ever goes from being unowned (in the previous state of the game) to
            // owned now, by checking relevant game state changes that move cards into the game
            state.history.forEach { change ->
                when (change) {
                    is GameStateChange.GameStarted -> {
                        state.getOwnedCards().forEach { card -> cardStats.notifyOwnership(card) }
                    }
                    is GameStateChange.MoveCard -> {
                        if (prevState.pileFor(change.card) == null) {
                            cardStats.notifyOwnership(change.card)
                        }
                    }
                    is GameStateChange.MoveCards -> {
                        change.cards.forEach { card ->
                            if (prevState.pileFor(card) == null) {
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

fun GameContext.encodeToYaml() = GameSnapshot.from(
    describer,
    state,
    isPreDraw = viewStack.contains { view -> view is PreDrawView }
).encodeToYaml()
