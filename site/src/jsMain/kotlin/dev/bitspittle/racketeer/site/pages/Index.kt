package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.building.allPassiveActions
import dev.bitspittle.racketeer.model.card.allInitActions
import dev.bitspittle.racketeer.model.card.allPassiveActions
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.types.BuildingEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.CardEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.ExprEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.GameService
import dev.bitspittle.racketeer.scripting.utils.installGameLogic
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.CardGroup
import dev.bitspittle.racketeer.site.model.GameContext
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

suspend fun createNewGame(gameData: GameData): GameContext {
    // TODO: Set the outputs of the logger to some element on the game board
    val logger = object : Logger {
        override fun info(message: String) {
            println("ℹ️: $message")
        }

        override fun warn(message: String) {
            println("⚠️: $message")
        }

        override fun error(message: String) {
            println("⛔: $message")
        }

        override fun debug(message: String) {
            println("⚙️: $message")
        }
    }

    val copyableRandom = CopyableRandom()
    val env = Environment()
    env.installDefaults(object : LangService {
        override val random = copyableRandom.invoke()
        override val logger = logger
    })

    // Evaluate global actions found in the gamedata
    Evaluator().let { evaluator ->
        // Safe to call runBlocking at this point because limp defaults all promise not to suspend
        gameData.globalActions.forEach { action ->
            evaluator.evaluate(env, action)
        }
    }

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

    val actionQueue = ActionQueue()
    val enqueuers = Enqueuers(
        actionQueue,
        ExprEnqueuerImpl(env, exprCache, actionQueue),
        CardEnqueuerImpl(env, exprCache, actionQueue),
        BuildingEnqueuerImpl(env, exprCache, actionQueue),
    )

    val gameState = MutableGameState(gameData, mutableSetOf(), enqueuers, copyableRandom)
    val describer = Describer(gameData, showDebugInfo = { true })
    env.installGameLogic(object : GameService {
        override val gameData = gameData
        override val describer = describer
        override val gameState = gameState
        override val enqueuers = enqueuers
        override val chooseHandler = object : ChooseHandler {
            override suspend fun query(
                prompt: String?,
                list: List<Any>,
                range: IntRange,
                requiredChoice: Boolean
            ): List<Any>? {
                // TODO: Implement this somehow
                return null
            }
        }
        override val logger = logger
    })

    gameState.recordChanges {
        gameState.apply(GameStateChange.GameStart())
        enqueuers.expr.enqueue(gameState, gameData.initActions)
        enqueuers.actionQueue.runEnqueuedActions()
        gameState.apply(GameStateChange.Draw())
    }

    return GameContext(gameData, gameState, describer)
}

@Page
@Composable
fun HomePage() {
    PageLayout("Do Crimes") {
        val scope = rememberCoroutineScope()
        var ctx by remember { mutableStateOf<GameContext?>(null) }

        @Suppress("NAME_SHADOWING")
        ctx?.let { ctx ->
            GameBoard(ctx)
        } ?: run {
            Text("Please wait, loading...")
            window.fetch("gamedata.yaml").then { response ->
                response.text().then { responseText ->
                    val gameData = GameData.decodeFromString(responseText)
                    scope.launch {
                        ctx = createNewGame(gameData)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameBoard(ctx: GameContext) {
    Box(Modifier.fillMaxSize().padding(top = 5.em), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.gap(20.px)) {
            CardGroup("Street") {
                ctx.state.street.cards.forEach { card ->
                    Card(ctx, card, onClick = {})
                }
            }
            CardGroup("Hand") {
                ctx.state.hand.cards.forEach { card ->
                    Card(ctx, card, onClick = {})
                }
            }
        }
    }
}