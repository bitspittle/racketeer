package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.screens.GameScreen
import dev.bitspittle.racketeer.site.components.screens.TitleScreen
import dev.bitspittle.racketeer.site.components.sections.Choice
import dev.bitspittle.racketeer.site.components.sections.SelectedModifier
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.*
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private sealed interface GameStartupState {
    object FetchingData : GameStartupState
    class DataFetched(val gameData: GameData) : GameStartupState
    class SelectingFeatures(val gameData: GameData, val initCtx: suspend GameContext.() -> Unit) : GameStartupState
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
                        requestNewGame = {
                            startupState = GameStartupState.SelectingFeatures(gameData) { startNewGame() }
                        },
                        requestCreateGameContext = { initCtx ->
                            startupState = GameStartupState.CreatingContext(gameData, initCtx)
                        }
                    )
                }
            }
            is GameStartupState.SelectingFeatures -> {
                (startupState as GameStartupState.SelectingFeatures).apply {
                    fun goBack() {
                        startupState = GameStartupState.DataFetched(gameData)
                    }

                    Modal(
                        ref = inputRef {
                            if (code == "Escape") { goBack(); true } else false
                        },
                        title = "Select game feature(s)",
                        bottomRow = {
                            Button(onClick = {
                                goBack()
                            }) {Text("Go Back") }

                            Button(onClick = {
                                Data.save(Data.Keys.Settings, settings)

                                startupState = GameStartupState.CreatingContext(gameData, initCtx)
                            }) {Text("Continue") }
                        }
                    ) {
                        run {
                            val buildingFeature = remember { gameData.features.first { it.type == Feature.Type.BUILDINGS } }
                            var selected by remember { mutableStateOf(settings.features.buildings) }

                            if (selected != settings.features.buildings) {
                                settings.features.buildings = selected
                                events.emitAsync(scope, Event.SettingsChanged(settings))
                            }
                            Button(
                                onClick = { selected = !selected },
                                Modifier.thenIf(selected, SelectedModifier)
                            ) {
                                Text(buildingFeature.name)
                            }
                            Tooltip(ElementTarget.PreviousSibling, buildingFeature.description)
                        }
                    }
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
                val onRestartRequested: () -> Unit = {
                    // TODO: Add analytics here to indicate a restart was requested
                    startupState = GameStartupState.SelectingFeatures(gameCtx.data) {
                        state = MutableGameState(data, state.features, enqueuers)
                        startNewGame()
                    }
                }

                GameScreen(
                    scope,
                    events,
                    gameCtx,
                    onRestartRequested,
                    onQuitRequested,
                )
            }
        }

        choiceCtx?.let { ctx -> Choice(ctx) }
    }
}
