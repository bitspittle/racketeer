package dev.bitspittle.racketeer.console.game

import com.varabyte.kotter.foundation.collections.liveListOf
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
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir
import dev.bitspittle.racketeer.console.user.*
import dev.bitspittle.racketeer.console.utils.DriveCloudFileService
import dev.bitspittle.racketeer.console.utils.CloudFileService
import dev.bitspittle.racketeer.console.utils.UploadThrottleCategory
import dev.bitspittle.racketeer.console.utils.wrap
import dev.bitspittle.racketeer.console.view.ViewStackImpl
import dev.bitspittle.racketeer.console.view.views.game.choose.ChooseItemsView
import dev.bitspittle.racketeer.console.view.views.game.choose.PickItemView
import dev.bitspittle.racketeer.console.view.views.game.choose.ReviewItemsView
import dev.bitspittle.racketeer.console.view.views.system.TitleMenuView
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.building.allPassiveActions
import dev.bitspittle.racketeer.model.card.allPassiveActions
import dev.bitspittle.racketeer.model.card.allInitActions
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameStateStub
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.types.BuildingEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.CardEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.ExprEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.GameService
import dev.bitspittle.racketeer.scripting.utils.installGameLogic
import kotlinx.coroutines.*
import net.mamoe.yamlkt.Yaml
import java.time.Duration
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.readText
import kotlin.random.Random

class GameSession(
    private val gameData: GameData
) {
    fun start() = session {

        val userDataDir = UserDataDir(gameData.title.lowercase().replace(Regex("""\s"""), ""))
        val settings = try {
            Yaml.decodeFromString(Settings.serializer(), userDataDir.pathForSettings().readText())
        } catch (ex: Exception) {
            Settings()
        }

        val logRenderers = liveListOf<RenderScope.() -> Unit>()
        var handleQuit: () -> Unit = {}
        val app = object : App {
            override fun quit() {
                handleQuit()
            }

            override val properties = Properties().apply {
                load(GameSession::class.java.getResourceAsStream("/project.properties")!!)
            }

            override val logger = object : Logger {
                private fun RenderScope.textLines(messages: Iterable<String>) {
                    for (message in messages) {
                        if (message.isNotBlank()) {
                            textLine(" - $message".wrap())
                        } else {
                            textLine()
                        }
                    }
                }
                private fun RenderScope.textLines(messages: String) = textLines(messages.split('\n'))

                override fun info(message: String) {
                    logRenderers.add { green { textLines(message) } }
                }

                override fun warn(message: String) {
                    logRenderers.add { yellow { textLines(message) } }
                }

                override fun error(message: String) {
                    logRenderers.add { red { textLines(message) } }
                }

                override fun debug(message: String) {
                    if (settings.admin.enabled) {
                        logRenderers.add { magenta { textLines(message) } }
                    }
                }
            }

            override val userDataDir = userDataDir
            override val cloudFileService: CloudFileService = DriveCloudFileService(
                gameData.title,
                // If games are broken (e.g. infinite cards), we don't want to keep reporting crashes because users can
                // get into a sort of death spiral as the system begins exploding under the pressure. Instead, crash
                // reporting is for catching mistakes we made in cards, not in the underlying engine (there are better
                // ways to report those sorts of issues, e.g. getting in touch with the developer and reporting a bug).
                //
                // At this point, a normal game clocks in at around 100K, so we give ourselves a bit of a buffer on top
                // of that, something that should limit the chance of cutting out real game crashes. file sizes go up in
                // the future, we'll want to review this value.
                throttleSizes = mapOf(UploadThrottleCategory.CRASH_REPORT to 500 * 1024)
            )

            override var isUpdateAvailable: Boolean = false
                private set

            init {
                lateinit var checkForUpdate: () -> Unit
                fun waitAndCheckAgain() {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(Duration.ofMinutes(30).toMillis())
                        checkForUpdate()
                    }
                }
                checkForUpdate = {
                    cloudFileService.download("version.txt",
                        onDownloaded = { versionStr ->
                            isUpdateAvailable = this.version < Version(versionStr.trim())
                            if (!isUpdateAvailable) {
                                waitAndCheckAgain()
                            }
                        },
                        onFailed = { waitAndCheckAgain() }
                    )
                }
                checkForUpdate()
            }
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

        val exprCache = ExprCache()
        // Compile early to suss out any syntax errors at startup time instead of runtime
        gameData.cards.flatMap { it.allInitActions }.forEach { exprCache.parse(it) }
        gameData.cards.flatMap { it.allPassiveActions }.forEach { exprCache.parse(it) }
        gameData.cards.flatMap { it.playActions }.forEach { exprCache.parse(it) }
        gameData.blueprints.flatMap { it.initActions }.forEach { exprCache.parse(it) }
        gameData.blueprints.flatMap { it.allPassiveActions }.forEach { exprCache.parse(it) }
        gameData.blueprints.flatMap { it.activateActions }.forEach { exprCache.parse(it) }
        gameData.blueprints.map { it.canActivate }.filter { it.isNotBlank() }.forEach { exprCache.parse(it) }
        gameData.initActions.forEach { exprCache.parse(it) }

        val ctx = run {
            val actionQueue = ActionQueue()
            val enqueuers = Enqueuers(
                actionQueue,
                ExprEnqueuerImpl(env, exprCache, actionQueue),
                CardEnqueuerImpl(env, exprCache, actionQueue),
                BuildingEnqueuerImpl(env, exprCache, actionQueue),
            )

            GameContext(
                gameData,
                settings,
                UserStats.loadFrom(userDataDir),
                Describer(gameData, showDebugInfo = { settings.inAdminModeAndShowCode }),
                GameStateStub,
                env,
                enqueuers,
                viewStack,
                app
            )
        }


        val titleView = TitleMenuView(ctx)
        produceRandom = { ctx.state.random() }

        var handleRerender: () -> Unit = {}
        env.installGameLogic(object : GameService {
            override val gameData = ctx.data
            override val describer = ctx.describer
            override val gameState get() = ctx.state
            override val enqueuers = ctx.enqueuers
            override val chooseHandler = object : ChooseHandler {
                override suspend fun query(
                    prompt: String?,
                    list: List<Any>,
                    range: IntRange,
                    requiredChoice: Boolean
                ): List<Any>? {
                    return suspendCoroutine { choices ->
                        viewStack.pushView(
                            if (range.first == range.last && range.first == list.size) {
                                // You have to pick exactly the whole list. Not much of a choice actually!
                                ReviewItemsView(ctx, prompt, list, choices, requiredChoice)
                            } else if (range.first == range.last && range.first == 1) {
                                // You have to pick exactly one item from a list, no need for a confirm button.
                                PickItemView(ctx, prompt, list, choices, requiredChoice)
                            } else {
                                // Normal multi-select choose screen.
                                ChooseItemsView(ctx, prompt, list, range, choices, requiredChoice)
                            }
                        )
                        handleRerender()
                    }
                }
            }

            override val logger = app.logger
        })

        viewStack.pushView(titleView)
        section {
            try {
                // Due to threading issues (a rerender getting requested while the gamestate is changing), this might
                // crash when reading data that's being concurrently written to. Note great but just ignore the
                // exception for now, as we will get another rerender request soon
                viewStack.currentView.renderInto(this)
            } catch(ignored: Exception) { }

            if (logRenderers.isNotEmpty()) {
                textLine()
                logRenderers.forEach { it.invoke(this) }
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