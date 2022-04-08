package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState

/**
 * Misc. values and functionality provided by the game implementation needed by our scripting system
 */
interface GameService {
    val gameData: GameData
    val gameState: GameState
    val cardQueue: CardQueue?

    fun log(message: String)

    fun expectCardQueue() = cardQueue ?: throw IllegalStateException("CardQueue should exist while running cards")
}