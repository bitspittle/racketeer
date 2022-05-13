package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingEnqueuer
import dev.bitspittle.racketeer.scripting.utils.setValuesFrom

class BuildingEnqueuerImpl(
    private val env: Environment,
    private val exprCache: ExprCache,
    private val actionQueue: ActionQueue,
) : BuildingEnqueuer {
    private fun enqueueActions(gameState: GameState, actions: List<String>) {
        if (actions.isEmpty()) return

        val evaluator = Evaluator()
        actions.forEach { action ->
            actionQueue.enqueue {
                env.scoped {
                    env.setValuesFrom(gameState)
                    evaluator.evaluate(env, exprCache.parse(action))
                }
            }
        }
    }

    override fun enqueueInitActions(gameState: GameState, building: Building) = enqueueActions(gameState, building.blueprint.initActions)
    override fun enqueueActivateActions(gameState: GameState, building: Building) = enqueueActions(gameState, building.blueprint.activateActions)
    override fun enqueuePassiveActions(gameState: GameState, building: Building) = enqueueActions(gameState, building.blueprint.passiveActions)
}
