package dev.bitspittle.racketeer.model.location

import dev.bitspittle.racketeer.model.game.GameState

interface LocationEnqueuer {
    fun enqueueInitActions(gameState: GameState, location: Location)
    fun enqueueActivateActions(gameState: GameState, location: Location)
    fun enqueuePassiveActions(gameState: GameState, location: Location)
}
