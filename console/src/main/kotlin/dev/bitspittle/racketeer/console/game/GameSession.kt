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
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.console.view.views.game.ChooseItemsView
import dev.bitspittle.racketeer.console.view.views.game.PickItemView
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.types.CardQueueImpl
import dev.bitspittle.racketeer.scripting.types.GameService
import dev.bitspittle.racketeer.scripting.utils.installGameLogic
import kotlinx.coroutines.*
import kotlin.coroutines.suspendCoroutine
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
        var handleQuit: () -> Unit = {}
        val app = object : App {
            override fun quit() {
                handleQuit()
            }

            override fun log(message: String) {
                latestLogs.add(message)
            }
        }

        val env = Environment()
        val logger = object : Logger {
            override fun log(message: String) {
                app.log(message)
            }
        }
        env.installDefaults(object : LangService {
            override val random = Random.Default
            override val logger = logger
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
        // Compile early to suss out any syntax errors
        gameData.cards.flatMap { it.playActions }.forEach { Expr.parse(it) }
        val cardQueue = CardQueueImpl(env)
        val ctx = GameContext(
            gameData,
            Describer(gameData),
            GameState(gameData, cardQueue),
            env,
            cardQueue,
            viewStack,
            app,
        )

        var handleRerender: () -> Unit = {}
        env.installGameLogic(object : GameService {
            override val gameData = ctx.data
            override val describer = ctx.describer
            override val gameState get() = ctx.state
            override val cardQueue get() = ctx.cardQueue
            override val chooseHandler
                get() = object : ChooseHandler {
                    override suspend fun query(prompt: String?, list: List<Any>, range: IntRange): List<Any> {
                        return suspendCoroutine { choices ->
                            viewStack.pushView(
                                if (range.first == range.last && range.first == 1) {
                                    PickItemView(ctx, prompt, list, choices)
                                } else {
                                    ChooseItemsView(ctx, prompt, list, range, choices)
                                }
                            )
                            handleRerender()
                        }
                    }
                }

            override val logger = logger

        })

        viewStack.pushView(PreDrawView(ctx))
        section {
            viewStack.currentView.renderInto(this)
            if (latestLogs.isNotEmpty()) {
                textLine()
                yellow {
                    textLine(latestLogs.joinToString("\n\n"))
                }
            }
        }.runUntilSignal {
            handleQuit = { signal() }
            handleRerender = { rerender() }
            onKeyPressed {
                latestLogs.clear()
                CoroutineScope(Dispatchers.IO).launch {
                    if (viewStack.currentView.handleKey(key)) {
                        // Minor hack, this is the wrong way to do this but at the same time it's not horrible...
                        // Basically, we can launch multiple key handlers at the same time, and occasionally two of them
                        // are related (one starts another and then blocks waiting for it to finish), both kicking off
                        // two rerenders one right after the other, causing a stutter as screens are transitioning.
                        // The delay here allows screen changes to "settle" before rerendering (although this does
                        // end up with us running a redundant rerender, but whatever)
                        delay(50)
                        rerender()
                    }
                }
            }
        }
    }
}