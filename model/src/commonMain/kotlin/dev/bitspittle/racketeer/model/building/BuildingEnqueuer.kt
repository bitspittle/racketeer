package dev.bitspittle.racketeer.model.building

import dev.bitspittle.racketeer.model.game.GameState

interface BuildingEnqueuer {
    fun enqueueInitActions(gameState: GameState, building: Building)
    fun enqueueActivateActions(gameState: GameState, building: Building)
    fun enqueuePassiveActions(gameState: GameState, building: Building)
}
