package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.racketeer.model.game.GameState

/**
 * Misc. values and functionality provided by the game implementation needed by our scripting system
 */
interface GameService {
    val gameState: GameState

    fun log(message: String)
}