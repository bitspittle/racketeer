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
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.model.ChoiceContext
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

                // Have to slightly defer the following logic or else Compose misses me updating the startupState
                // variable, probably because it's still laying out data right now.
                window.setTimeout({
                    Data.loadRaw(Data.Keys.GameData)?.let { (_, gameDataStr) ->
                        try {
                            startupState = GameStartupState.DataFetched(GameData.decodeFromString(gameDataStr))
                        } catch (ex: Exception) {
                            Data.delete(Data.Keys.GameData)
                            println("Could not load gamedata.yaml override. Ignoring it.\n\n$ex")
                        }
                    }

                    // The following will not be true if the game data was successfully loaded from memory first.
                    if (startupState is GameStartupState.FetchingData) {
                        window.fetch("gamedata.yaml").then { response ->
                            response.text().then { responseText ->
                                startupState = GameStartupState.DataFetched(GameData.decodeFromString(responseText))
                            }
                        }
                    }
                })
            }
            is GameStartupState.DataFetched -> {
                (startupState as GameStartupState.DataFetched).apply {
                    TitleScreen(
                        scope,
                        gameData.title,
                        settings,
                        events,
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
                val gameCtx = (startupState as GameStartupState.ContextCreated).gameContext
                val onQuitRequested: () -> Unit = { startupState = GameStartupState.DataFetched(gameCtx.data) }

                GameScreen(
                    scope,
                    events,
                    gameCtx,
                    onQuitRequested
                )
            }
        }

        choiceCtx?.let { ctx -> Choice(ctx) }
    }
}
