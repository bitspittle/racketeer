package dev.bitspittle.racketeer.console

import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.render.offscreen
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.console.view.views.PreDrawView
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describers

class GameSession(
    private val gameData: GameData
) {
    fun start() = session {
        section {
            textLine()

            bold {
                red {
                    bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                        textLine(gameData.config.title)
                    }
                }
            }

            textLine()
        }.run()

        val viewStack = ViewStackImpl()
        var shouldQuit = false
        val ctx = GameContext(
            gameData.config,
            Describers(gameData.config),
            GameState(gameData),
            viewStack,
            quit = { shouldQuit = true }
        )

        viewStack.pushView(PreDrawView(ctx))
        section {
            viewStack.currentView.renderInto(this)
        }.runUntilSignal {
            onKeyPressed {
                if (viewStack.currentView.handleKey(key)) {
                    rerender()
                }

                if (shouldQuit) {
                    signal()
                }
            }
        }
    }
}