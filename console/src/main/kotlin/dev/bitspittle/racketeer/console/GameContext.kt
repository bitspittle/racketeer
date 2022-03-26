package dev.bitspittle.racketeer.console

import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.model.text.Describers
import dev.bitspittle.racketeer.model.game.GameConfig
import dev.bitspittle.racketeer.model.game.GameState

class GameContext(
    val config: GameConfig,
    val describers: Describers,
    val state: GameState,
    val viewStack: ViewStack
)