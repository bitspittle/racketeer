package dev.bitspittle.racketeer.model.game

interface ExprEnqueuer {
    fun enqueue(gameState: GameState, code: String)
}
