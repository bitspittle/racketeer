package dev.bitspittle.racketeer.console

import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import dev.bitspittle.racketeer.console.view.ViewManager
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState

class GameSession(
    private val gameData: GameData
) {
    fun start() = session {
        val viewManager = ViewManager()
        val gameState = GameState(gameData.config)

        section {
            viewManager.currentView.render(gameState, this)
        }.runUntilSignal {
            onKeyPressed {
                if (key == Keys.Q) {
                    signal()
                }
            }
        }
    }
}