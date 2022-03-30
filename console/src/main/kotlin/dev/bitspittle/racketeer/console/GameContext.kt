package dev.bitspittle.racketeer.console

import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describers

class GameContext(
    val data: GameData,
    val describers: Describers,
    var state: GameState,
    val viewStack: ViewStack,
    val quit: () -> Unit,
) {
    // Exposed directly, for convenience
    val config = data.config
}