package dev.bitspittle.racketeer.console.utils

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.game.GameStateDiff
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
            block().also { GameStateDiff(describer, prevState, nextState).reportTo(app.logger) }
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