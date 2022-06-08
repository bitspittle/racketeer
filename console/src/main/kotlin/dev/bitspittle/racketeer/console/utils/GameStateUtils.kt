package dev.bitspittle.racketeer.console.utils

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.notifyBuilt
import dev.bitspittle.racketeer.console.game.notifyOwnership
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.scripting.types.CancelPlayException

suspend fun GameContext.createNewGame(features: Set<Feature.Type> = setOf(Feature.Type.BUILDINGS)) {
    state = MutableGameState(data, features, enqueuers)

    enqueuers.expr.enqueue(state, data.initActions)
    enqueuers.actionQueue.runEnqueuedActions()

    state.deck.cards.forEach { card ->
        userStats.cards.notifyOwnership(card)
    }
}

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
                GameStateDiff(prevState, nextState).reportTo(data, describer, app.logger)
            }

            // Update user stats based on new history
            state.history.drop(prevState.history.size).forEach { change ->
                when (change) {
                    is GameStateChange.MoveCard -> {
                        if (prevState.pileFor(change.card) == null) {
                            userStats.cards.notifyOwnership(change.card)
                        }
                    }
                    is GameStateChange.MoveCards -> {
                        change.cards.forEach { card ->
                            if (prevState.pileFor(card) == null) {
                                userStats.cards.notifyOwnership(card)
                            }
                        }
                    }
                    is GameStateChange.Build -> {
                        userStats.buildings.notifyBuilt(change.blueprint)
                    }
                    else -> Unit // Doesn't affect user stats
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
    data,
    describer,
    state,
).encodeToYaml()
