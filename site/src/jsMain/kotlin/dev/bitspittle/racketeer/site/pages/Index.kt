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
import dev.bitspittle.racketeer.site.model.GameContext
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private sealed interface GameStartupState {
    object FetchingData : GameStartupState
    class DataFetched(val gameData: GameData) : GameStartupState
//    object CreatingContext : GameStartupState
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
                Box(
                    Modifier.fillMaxSize().cursor(Cursor.Progress).padding(15.percent),
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
                val gameData = (startupState as GameStartupState.DataFetched).gameData
                TitleScreen(
                    scope,
                    gameData,
                    handleChoice = {
                        choiceCtx = it.also {
                            it.onChosen {
                                choiceCtx = null
                            }
                        }
                    },
                    gameRequested = { ctx ->
                        startupState = GameStartupState.ContextCreated(ctx)
                    }
                )
            }
            is GameStartupState.ContextCreated -> {
                key(forceRecomposition) {
                    val gameCtx = (startupState as GameStartupState.ContextCreated).gameContext
                    GameScreen(scope, gameCtx) { ++forceRecomposition }
                }
            }
        }

        choiceCtx?.let { ctx -> Choice(ctx) }
    }
}
