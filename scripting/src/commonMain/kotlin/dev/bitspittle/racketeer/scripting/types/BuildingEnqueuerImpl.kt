package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingEnqueuer
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.utils.setValuesFrom

class BuildingEnqueuerImpl(
    private val env: Environment,
    private val exprCache: ExprCache,
    private val actionQueue: ActionQueue,
) : BuildingEnqueuer {
    private fun enqueueActions(gameState: GameState, building: Building, actions: List<String>) {
        if (actions.isEmpty()) return

        val evaluator = Evaluator()
        actionQueue.enqueue(
            init = {
                env.pushScope()
                env.setValuesFrom(gameState)
                env.setValuesFrom(building)
            },
            tearDown = {
                env.popScope()
            },
            actions = actions.map {
                { evaluator.evaluate(env, exprCache.parse(it)) }
            }
        )
    }

    override fun enqueueInitActions(gameState: GameState, building: Building) = enqueueActions(gameState, building, building.blueprint.initActions)
    override fun enqueueActivateActions(gameState: GameState, building: Building) = enqueueActions(gameState, building, building.blueprint.activateActions)
    override fun enqueuePassiveActions(gameState: GameState, building: Building) = enqueueActions(gameState, building, building.blueprint.passiveActions)
}
