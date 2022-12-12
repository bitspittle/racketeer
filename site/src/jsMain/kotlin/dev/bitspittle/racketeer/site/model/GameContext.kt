package dev.bitspittle.racketeer.site.model

import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer

class GameContext(val data: GameData, val state: GameState, val describer: Describer)