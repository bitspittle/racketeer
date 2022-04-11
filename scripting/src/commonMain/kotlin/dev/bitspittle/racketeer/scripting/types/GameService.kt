package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler

/**
 * Misc. values and functionality provided by the game implementation needed by our scripting system
 */
interface GameService {
    val gameData: GameData
    val describer: Describer
    val gameState: GameState
    val cardQueue: CardQueue?
    val chooseHandler: ChooseHandler

    fun log(message: String)

    fun expectCardQueue() = cardQueue ?: throw IllegalStateException("CardQueue should exist while running cards")
}
