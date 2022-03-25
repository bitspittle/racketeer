package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.model.game.GameState

abstract class View {
    fun render(gameState: GameState, scope: RenderScope) {
        scope.apply {
            textLine("\uD83E\uDD1D ${gameState.influence} ⭐ ${gameState.victoryPoints}")
        }
    }
}