package dev.bitspittle.racketeer.site.model.user

import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameState

class GameStats(
    val vp: Int,
    val features: Set<Feature.Type>,
) {
    companion object {
        fun from(state: GameState) = GameStats(state.vp, state.features)
    }
}
