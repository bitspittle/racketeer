package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState

/**
 * Misc. values and functionality provided by the game implementation needed by our scripting system
 */
interface GameService {
    val gameData: GameData
    val gameState: GameState
    val actionQueue: ActionQueue?

    fun log(message: String)
}