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
import dev.bitspittle.firebase.database.encodeKey
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.screens.GameScreen
import dev.bitspittle.racketeer.site.components.screens.LoginScreen
import dev.bitspittle.racketeer.site.components.screens.TitleScreen
import dev.bitspittle.racketeer.site.components.screens.WaitlistScreen
import dev.bitspittle.racketeer.site.components.sections.Choice
import dev.bitspittle.racketeer.site.components.sections.SelectedModifier
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.*
import dev.bitspittle.racketeer.site.model.account.Account
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private sealed interface GameStartupState {
    object FetchingData : GameStartupState
    class LoggingIn(val gameData: GameData) : GameStartupState
    class VerifyAccount(val gameData: GameData, val account: Account) : GameStartupState
    class ShowWaitlistMessage(val gameData: GameData, val account: Account) : GameStartupState
    class TitleScreen(val gameData: GameData, val account: Account) : GameStartupState
    class SelectingFeatures(val gameData: GameData, val account: Account, val initCtx: suspend GameContext.() -> Unit) : GameStartupState
    class CreatingContext(val gameData: GameData, val account: Account, val initCtx: suspend GameContext.() -> Unit) : GameStartupState
    class ContextCreated(val gameContext: GameContext) : GameStartupState
}

@Page
@Composable
fun HomePage() {
    PageLayout {
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

        LaunchedEffect(Unit) {
            firebase.auth.onAuthStateChanged { user ->
                if (user == null) {
                    Data.delete(Data.Keys.Account)
                    startupState = GameStartupState.FetchingData
                }
            }
        }

        fun requestNewGame(gameData: GameData, account: Account, initCtx: GameContext.() -> Unit = {}) {
            startupState = if (settings.unlocks.buildings) {
                GameStartupState.SelectingFeatures(gameData, account) {
                    this.initCtx()
                    startNewGame()
                }
            } else {
                GameStartupState.CreatingContext(gameData, account) {
                    this.initCtx()
                    startNewGame()
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

                // Defer logic or else Compose misses it for some reason
                LaunchedEffect(Unit) {
                    Data.loadRaw(Data.Keys.GameData)?.let { (_, gameDataStr) ->
                        try {
                            startupState = GameStartupState.LoggingIn(GameData.decodeFromString(gameDataStr))
                        } catch (ex: Exception) {
                            Data.delete(Data.Keys.GameData)
                            println("Could not load gamedata.yaml override. Ignoring it.\n\n$ex")
                        }
                    }

                    // The following will not be true if the game data was successfully loaded from memory first.
                    if (startupState is GameStartupState.FetchingData) {
                        window.fetch("gamedata.yaml").then { response ->
                            response.text().then { responseText ->
                                startupState = GameStartupState.LoggingIn(GameData.decodeFromString(responseText))
                            }
                        }
                    }
                }
            }

            is GameStartupState.LoggingIn -> (startupState as GameStartupState.LoggingIn).apply {
                var showLoginScreen by remember { mutableStateOf(false) }
                // Defer logic or else Compose misses it for some reason
                LaunchedEffect(Unit) {
                    Data.load(Data.Keys.Account)?.value?.let { account ->
                        startupState = GameStartupState.VerifyAccount(gameData, account)
                    } ?: run { showLoginScreen = true }
                }

                if (showLoginScreen) {
                    LoginScreen(firebase, gameData, scope, onLoggedIn = { account ->
                        Data.save(Data.Keys.Account, account)
                        startupState = GameStartupState.VerifyAccount(gameData, account)
                    })
                }
            }

            is GameStartupState.VerifyAccount -> (startupState as GameStartupState.VerifyAccount).apply {
                scope.launch {
                    val db = firebase.db
                    val isAllowedEmail =
                        db.ref("/allowlist/emails/${account.email!!.lowercase().encodeKey()}").get().exists()

                    startupState = if (isAllowedEmail) {
                        GameStartupState.TitleScreen(gameData, account)
                    } else {
                        GameStartupState.ShowWaitlistMessage(gameData, account)
                    }
                }
            }

            is GameStartupState.ShowWaitlistMessage -> (startupState as GameStartupState.ShowWaitlistMessage).apply {
                WaitlistScreen(firebase, account, gameData, scope, onClose = {
                    scope.launch { firebase.auth.signOut() }
                })
            }

            is GameStartupState.TitleScreen -> (startupState as GameStartupState.TitleScreen).apply {
                document.title = gameData.title

                val describer = Describer(gameData, showDebugInfo = { false })
                val tooltipParser = TooltipParser(gameData, describer)
                val stubLogger = MemoryLogger()

                TitleScreen(
                    scope,
                    PopupParams(
                        firebase,
                        gameData,
                        account,
                        settings,
                        userStats,
                        stubLogger,
                        describer,
                        tooltipParser
                    ),
                    events,
                    requestNewGame = { requestNewGame(gameData, account) },
                    requestResumeGame = { initCtx ->
                        startupState = GameStartupState.CreatingContext(gameData, account, initCtx)
                    }
                )
            }
            is GameStartupState.SelectingFeatures -> (startupState as GameStartupState.SelectingFeatures).apply {
                fun goBack() {
                    startupState = GameStartupState.TitleScreen(gameData, account)
                }

                Modal(
                    ref = inputRef {
                        if (code == "Escape") {
                            goBack(); true
                        } else false
                    },
                    title = "Select game feature(s)",
                    bottomRow = {
                        Button(onClick = {
                            goBack()
                        }) { Text("Go Back") }

                        Button(onClick = {
                            Data.save(Data.Keys.Settings, settings)

                            startupState = GameStartupState.CreatingContext(gameData, account, initCtx)
                        }) { Text("Continue") }
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
            is GameStartupState.CreatingContext -> (startupState as GameStartupState.CreatingContext).apply {
                LaunchedEffect(startupState) {
                    startupState = GameStartupState.ContextCreated(
                        createGameConext(
                            firebase,
                            gameData,
                            account,
                            settings,
                            userStats,
                            handleChoice
                        ).apply { initCtx() })
                }
            }
            is GameStartupState.ContextCreated -> (startupState as GameStartupState.ContextCreated).apply {
                val onQuitRequested: () -> Unit = { startupState = GameStartupState.TitleScreen(gameContext.data, gameContext.account) }
                val onRestartRequested: () -> Unit = {
                    requestNewGame(gameContext.data, gameContext.account) {
                        state = MutableGameState(data, state.features, enqueuers)
                    }
                }

                GameScreen(
                    scope,
                    events,
                    gameContext,
                    onRestartRequested,
                    onQuitRequested,
                )
            }
        }

        choiceCtx?.let { ctx -> Choice(ctx) }
    }
}
