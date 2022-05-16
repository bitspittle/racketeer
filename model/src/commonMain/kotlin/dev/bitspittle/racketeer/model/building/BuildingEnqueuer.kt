package dev.bitspittle.racketeer.model.building

import dev.bitspittle.racketeer.model.game.GameState

interface BuildingEnqueuer {
    // Note: This `canActivate` method kind of doesn't belong here, it's not really enqueueing anything, but it's a
    // convenient place where we already have everything we need to evaluate it here.
    suspend fun canActivate(gameState: GameState, building: Building): Boolean
    fun enqueueInitActions(gameState: GameState, building: Building)
    fun enqueueActivateActions(gameState: GameState, building: Building)
    fun enqueuePassiveActions(gameState: GameState, building: Building)
}
