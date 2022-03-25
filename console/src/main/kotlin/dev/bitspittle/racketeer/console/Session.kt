package dev.bitspittle.racketeer.console

import dev.bitspittle.racketeer.model.GameData
import dev.bitspittle.racketeer.model.GameState

class Session(
    private val gameData: GameData
) {
    private val gameState = GameState(gameData.config)


}