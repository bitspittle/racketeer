package dev.bitspittle.racketeer.console.game

import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.console.view.views.PreDrawView
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.installGameLogic
import dev.bitspittle.racketeer.scripting.types.GameService
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

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

        val latestLogs = mutableListOf<String>()
        var shouldQuit = false
        val app = object : App {
            override fun quit() {
                shouldQuit = true
            }

            override fun log(message: String) {
                latestLogs.add(message)
            }
        }

        val env = Environment()
        env.installDefaults(object : LangService {
            override val random = Random.Default
            override fun log(message: String) {
                app.log(message)
            }
        })
        Evaluator().let { evaluator ->
            // Safe to call runBlocking at this point because limp defaults all promise not to suspend
            runBlocking {
                gameData.globalActions.forEach { action ->
                    evaluator.evaluate(env, action)
                }
            }
        }

        val viewStack = ViewStackImpl()
        val ctx = GameContext(
            gameData,
            Describer(gameData),
            GameState(gameData),
            env,
            compiledActions = gameData.cards.associateWith { card -> card.actions.map { Expr.parse(it) } },
            viewStack,
            app,
        )
        env.installGameLogic(object : GameService {
            override val gameData = ctx.data
            override val gameState get() = ctx.state

            override fun log(message: String) {
                app.log(message)
            }
        })

        viewStack.pushView(PreDrawView(ctx))
        section {
            viewStack.currentView.renderInto(this)
            if (latestLogs.isNotEmpty()) {
                textLine()
                yellow {
                    textLine(latestLogs.joinToString("\n\n"))
                }
                latestLogs.clear()
            }
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