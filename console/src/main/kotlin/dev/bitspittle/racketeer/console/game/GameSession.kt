package dev.bitspittle.racketeer.console.game

import com.varabyte.kotter.foundation.input.onInputChanged
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.console.command.commands.system.UserData
import dev.bitspittle.racketeer.console.user.CardStats
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.inAdminModeAndShowDebugInfo
import dev.bitspittle.racketeer.console.utils.DriveUploadService
import dev.bitspittle.racketeer.console.utils.UploadService
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.console.view.views.game.choose.ChooseItemsView
import dev.bitspittle.racketeer.console.view.views.game.choose.PickItemView
import dev.bitspittle.racketeer.console.view.views.system.TitleMenuView
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.types.CardQueueImpl
import dev.bitspittle.racketeer.scripting.types.GameService
import dev.bitspittle.racketeer.scripting.utils.installGameLogic
import kotlinx.coroutines.*
import net.mamoe.yamlkt.Yaml
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.readText
import kotlin.random.Random

class GameSession(
    private val gameData: GameData
) {
    fun start() = session {

        val userData = UserData(gameData.title.lowercase().replace(Regex("""\s"""), ""))
        val settings = try {
            Yaml.decodeFromString(Settings.serializer(), userData.pathForSettings().readText())
        } catch (ex: Exception) {
            Settings()
        }

        val logRenderers = mutableListOf<RenderScope.() -> Unit>()
        var handleQuit: () -> Unit = {}
        val app = object : App {
            override fun quit() {
                handleQuit()
            }

            override val properties = Properties().apply {
                load(GameSession::class.java.getResourceAsStream("/project.properties")!!)
            }

            override val logger = object : Logger {
                override fun info(message: String) {
                    logRenderers.add { green { textLine(message) } }
                }

                override fun warn(message: String) {
                    logRenderers.add { yellow { textLine(message) } }
                }

                override fun error(message: String) {
                    logRenderers.add { red { textLine(message) } }
                }

                override fun debug(message: String) {
                    if (settings.inAdminModeAndShowDebugInfo) {
                        logRenderers.add { magenta { textLine(message) } }
                    }
                }
            }

            override val userData = userData
            override val uploadService: UploadService = DriveUploadService(gameData.title)
        }

        lateinit var produceRandom: () -> Random

        val env = Environment()
        env.installDefaults(object : LangService {
            override val random get() = produceRandom()
            override val logger = app.logger
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

        val cardStats = CardStats.load(userData) ?: emptyList()

        val titleView = TitleMenuView(gameData, settings, cardStats, app, viewStack, env, cardQueue)
        produceRandom = { titleView.ctx.state.random() }

        var handleRerender: () -> Unit = {}
        titleView.ctx.let { ctx ->
            env.installGameLogic(object : GameService {
                override val gameData = ctx.data
                override val describer = ctx.describer
                override val gameState get() = ctx.state
                override val cardQueue get() = ctx.cardQueue
                override val chooseHandler
                    get() = object : ChooseHandler {
                        override suspend fun query(
                            prompt: String?,
                            list: List<Any>,
                            range: IntRange,
                            requiredChoice: Boolean
                        ): List<Any>? {
                            return suspendCoroutine { choices ->
                                viewStack.pushView(
                                    if (range.first == range.last && range.first == 1) {
                                        PickItemView(ctx, prompt, list, choices, requiredChoice)
                                    } else {
                                        ChooseItemsView(ctx, prompt, list, range, choices, requiredChoice)
                                    }
                                )
                                handleRerender()
                            }
                        }
                    }

                override val logger = object : Logger {
                    override fun info(message: String) {
                        logRenderers.add { green { textLine(message) } }
                    }

                    override fun warn(message: String) {
                        logRenderers.add { yellow { textLine(message) } }
                    }

                    override fun error(message: String) {
                        logRenderers.add { red { textLine(message) } }
                    }

                    override fun debug(message: String) {
                        logRenderers.add { magenta { textLine(message) } }
                    }
                }
            })
        }

        viewStack.pushView(titleView)
        section {
            viewStack.currentView.renderInto(this)
            logRenderers.forEach {
                textLine()
                it.invoke(this)
            }
        }.runUntilSignal {
            handleQuit = { signal() }
            handleRerender = { rerender() }

            suspend fun rerenderView() {
                // Minor hack, this is the wrong way to do this but at the same time it's not horrible...
                // Basically, we can launch multiple key handlers at the same time, and occasionally two of them
                // are related (one starts another and then blocks waiting for it to finish), both kicking off
                // two rerenders one right after the other, causing a stutter as screens are transitioning.
                // The delay here allows screen changes to "settle" before rerendering (although this does
                // end up with us running a redundant rerender, but whatever)
                delay(16)
                rerender()
            }

            onKeyPressed {
                logRenderers.clear()
                CoroutineScope(Dispatchers.IO).launch {
                    if (viewStack.currentView.handleKey(key)) {
                        rerenderView()
                    }
                }
            }

            onInputChanged {
                CoroutineScope(Dispatchers.IO).launch {
                    viewStack.currentView.handleInputChanged(input)
                    rerenderView()
                }
            }
            onInputEntered {
                CoroutineScope(Dispatchers.IO).launch {
                    viewStack.currentView.handleInputEntered(input) { setInput("") }
                    rerenderView()
                }
            }
        }
    }
}