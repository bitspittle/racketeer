package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.building.allPassiveActions
import dev.bitspittle.racketeer.model.card.allInitActions
import dev.bitspittle.racketeer.model.card.allPassiveActions
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.scripting.types.BuildingEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.CardEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.ExprEnqueuerImpl
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.CardGroup
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlin.random.Random

@Page
@Composable
fun HomePage() {

    PageLayout("Do Crimes") {
        var gameData by remember { mutableStateOf<GameData?>(null) }

        window.fetch("gamedata.yaml").then {
            it.text().then { responseText ->
                gameData = GameData.decodeFromString(responseText)
            }
        }

        @Suppress("NAME_SHADOWING")
        gameData?.let { gameData ->
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
                override val random: Random = copyableRandom.invoke()
                override val logger = logger
            })

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

            val gameState = MutableGameState(gameData, mutableSetOf(Feature.Type.BUILDINGS), enqueuers, copyableRandom)
            GameBoard(gameState)
        } ?: run {
            Text("Please wait, loading...")
        }
    }
}

@Composable
private fun GameBoard(gameState: GameState) {
    // Temporary hack for testing card movement. Will be improved in a followup commit.
    var onStreet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(top = 5.em), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.gap(20.px)) {
            CardGroup("The Street") {
                if (onStreet) {
                    Card(onClick = { onStreet = false })
                }
            }
            CardGroup("Your Hand") {
                if (!onStreet) {
                    Card(onClick = { onStreet = true })
                }
            }
        }
    }
}