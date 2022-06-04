package dev.bitspittle.racketeer.model.game

interface ExprEnqueuer {
    fun enqueue(gameState: GameState, codeLines: List<String>)
}

fun ExprEnqueuer.enqueue(gameState: GameState, code: String) {
    enqueue(gameState, listOf(code))
}
