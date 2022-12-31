package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.screens.GameScreen
import dev.bitspittle.racketeer.site.components.screens.TitleScreen
import dev.bitspittle.racketeer.site.components.sections.Choice
import dev.bitspittle.racketeer.site.model.ChoiceContext
import dev.bitspittle.racketeer.site.model.Event
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.createGameConext
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private sealed interface GameStartupState {
    object FetchingData : GameStartupState
    class DataFetched(val gameData: GameData) : GameStartupState
    class CreatingContext(val gameData: GameData, val initCtx: suspend GameContext.() -> Unit) : GameStartupState
    class ContextCreated(val gameContext: GameContext) : GameStartupState
}

@Page
@Composable
fun HomePage() {
    PageLayout("Do Crimes") {
        var startupState by remember { mutableStateOf<GameStartupState>(GameStartupState.FetchingData) }
        var choiceCtx by remember { mutableStateOf<ChoiceContext?>(null) }
        val handleChoice: (ChoiceContext) -> Unit = remember {
            {
                choiceCtx = it.also {
                    it.onChosen {
                        choiceCtx = null
                    }
                }
            }
        }

        when (startupState) {
            GameStartupState.FetchingData -> {
                Box(
                    Modifier.fillMaxSize().cursor(Cursor.Progress).padding(5.percent),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("Please wait, loading...")
                }
                window.fetch("gamedata.yaml").then { response ->
                    response.text().then { responseText ->
                        startupState = GameStartupState.DataFetched(GameData.decodeFromString(responseText))
                    }
                }
            }
            is GameStartupState.DataFetched -> {
                (startupState as GameStartupState.DataFetched).apply {
                    TitleScreen(
                        settings,
                        requestNewGameContext = { initCtx ->
                            startupState = GameStartupState.CreatingContext(gameData, initCtx)
                        }
                    )
                }
            }
            is GameStartupState.CreatingContext -> {
                (startupState as GameStartupState.CreatingContext).apply {
                    LaunchedEffect(startupState) {
                        startupState = GameStartupState.ContextCreated(
                            createGameConext(
                                gameData,
                                settings,
                                handleChoice
                            ).apply { initCtx() })
                    }
                }
            }
            is GameStartupState.ContextCreated -> {
                var updateGameScreen by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    events.collect { evt ->
                        when (evt) {
                            is Event.GameStateUpdated -> updateGameScreen++
                            else -> {}
                        }
                    }
                }

                key(updateGameScreen) {
                    val gameCtx = (startupState as GameStartupState.ContextCreated).gameContext
                    GameScreen(
                        scope,
                        events,
                        gameCtx,
                        onQuitRequested = { startupState = GameStartupState.DataFetched(gameCtx.data) }
                    )
                }
            }
        }

        choiceCtx?.let { ctx -> Choice(ctx) }
    }
}
