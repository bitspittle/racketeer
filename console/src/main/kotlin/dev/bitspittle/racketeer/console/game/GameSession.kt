package dev.bitspittle.racketeer.console.game

import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.console.view.views.PreDrawView
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer
import kotlinx.coroutines.runBlocking

class GameSession(
    private val gameData: GameData
) {
    fun start() = session {
        section {
            textLine()

            bold {
                red {
                    bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                        textLine(gameData.title)
                    }
                }
            }

            textLine()
        }.run()

        val env = Environment()
        env.installDefaults()
        Evaluator().let { evaluator ->
            runBlocking {
                gameData.globalActions.forEach { action ->
                    evaluator.evaluate(env, action)
                }
            }
        }

        val viewStack = ViewStackImpl()
        var shouldQuit = false
        val ctx = GameContext(
            gameData,
            Describer(gameData),
            GameState(gameData),
            compiledActions = gameData.cards.associateWith { card -> card.actions.map { Expr.parse(it) } },
            viewStack,
            object : App {
                override fun quit() {
                    shouldQuit = true
                }
            }
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