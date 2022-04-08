package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.model.action.ActionRunner
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer

class GameContext(
    val data: GameData,
    val describer: Describer,
    var state: GameState,
    val env: Environment,
    val actionRunner: ActionRunner,
    val viewStack: ViewStack,
    val app: App
)