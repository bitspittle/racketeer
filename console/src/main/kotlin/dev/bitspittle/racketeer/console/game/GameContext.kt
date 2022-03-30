package dev.bitspittle.racketeer.console.game

import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer

class GameContext(
    val data: GameData,
    val describers: Describer,
    var state: GameState,
    val viewStack: ViewStack,
    val app: App
)