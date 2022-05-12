package dev.bitspittle.racketeer.model.card

import dev.bitspittle.racketeer.model.game.GameState

interface CardEnqueuer {
    fun enqueueInitActions(gameState: GameState, card: Card)
    fun enqueuePlayActions(gameState: GameState, card: Card)
    fun enqueuePassiveActions(gameState: GameState, card: Card)
}
