package dev.bitspittle.racketeer.console

import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.model.text.Describers
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState

class GameSession(
    private val gameData: GameData
) {
    fun start() = session {
        val viewStack = ViewStackImpl()
        val ctx = GameContext(
            gameData.config,
            Describers(gameData.config),
            GameState(gameData.config),
            ViewStackImpl()
        )

        section {
            viewStack.currentView.render(ctx, this)
        }.runUntilSignal {
            onKeyPressed {
                if (key == Keys.Q) {
                    signal()
                }
            }
        }
    }
}