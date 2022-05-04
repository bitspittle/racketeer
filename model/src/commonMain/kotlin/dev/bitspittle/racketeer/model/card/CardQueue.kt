package dev.bitspittle.racketeer.model.card

import dev.bitspittle.racketeer.model.game.MutableGameState

interface CardQueue {
    fun enqueueInitActions(card: Card)
    fun enqueuePlayActions(card: Card)
    fun enqueuePassiveActions(card: Card)
    fun clear()
    suspend fun runEnqueuedActions(gameState: MutableGameState)
}
