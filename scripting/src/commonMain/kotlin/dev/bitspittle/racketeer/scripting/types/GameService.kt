package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler

/**
 * Misc. values and functionality provided by the game implementation needed by our scripting system
 */
interface GameService {
    val gameData: GameData
    val describer: Describer
    val gameState: GameState
    val enqueuers: Enqueuers
    val chooseHandler: ChooseHandler

    val logger: Logger

    suspend fun addGameChange(change: GameStateChange)
}
