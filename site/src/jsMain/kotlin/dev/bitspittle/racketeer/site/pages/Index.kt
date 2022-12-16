package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.sections.Choice
import dev.bitspittle.racketeer.site.components.sections.GameBoard
import dev.bitspittle.racketeer.site.model.ChoiceContext
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.createNewGame
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private sealed interface GameStartupState {
    object FetchingData : GameStartupState
    class DataFetched(val gameData: GameData) : GameStartupState
    object CreatingContext : GameStartupState
    class ContextCreated(val gameContext: GameContext) : GameStartupState
}

@Page
@Composable
fun HomePage() {
    PageLayout("Do Crimes") {
        val scope = rememberCoroutineScope()
        var startupState by remember { mutableStateOf<GameStartupState>(GameStartupState.FetchingData) }
        var choiceCtx by remember { mutableStateOf<ChoiceContext?>(null) }
        var forceRecomposition by remember { mutableStateOf(0) }

        when (startupState) {
            GameStartupState.FetchingData -> {
                Text("Please wait, loading...")
                window.fetch("gamedata.yaml").then { response ->
                    response.text().then { responseText ->
                        startupState = GameStartupState.DataFetched(GameData.decodeFromString(responseText))
                    }
                }
            }
            is GameStartupState.DataFetched -> {
                val gameData = (startupState as GameStartupState.DataFetched).gameData
                startupState = GameStartupState.CreatingContext
                scope.launch {
                    val gameContext = createNewGame(gameData, handleChoice = {
                        choiceCtx = it.also {
                            it.onChosen { choiceCtx = null }
                        }
                    })
                    startupState = GameStartupState.ContextCreated(gameContext)
                }
            }
            GameStartupState.CreatingContext -> {
                // Do nothing.
                // State change to ContextCreated occurs in callback in DataFetched case
            }
            is GameStartupState.ContextCreated -> {
                key(forceRecomposition) {
                    val gameCtx = (startupState as GameStartupState.ContextCreated).gameContext
                    GameBoard(scope, gameCtx) { ++forceRecomposition }
                }
            }
        }

        choiceCtx?.let { ctx -> Choice(ctx) }
    }
}
