package dev.bitspittle.racketeer.model.card

import dev.bitspittle.racketeer.model.game.GameState

interface CardQueue {
    fun enqueueInitActions(card: Card)
    fun enqueuePlayActions(card: Card)
    fun enqueuePassiveActions(card: Card)
    fun clear()
    suspend fun runEnqueuedActions(gameState: GameState)
    val isRunning: Boolean
}
