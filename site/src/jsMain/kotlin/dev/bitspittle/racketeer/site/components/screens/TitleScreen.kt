package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.firebase.analytics.Analytics
import dev.bitspittle.racketeer.site.components.layouts.TitleLayout
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.menus.account.AccountMenu
import dev.bitspittle.racketeer.site.components.sections.menu.menus.user.UserDataMenu
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.util.loadSnapshotFromDisk
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.model.Event
import dev.bitspittle.racketeer.site.model.Events
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.createGameConext
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
fun TitleScreen(
    scope: CoroutineScope,
    params: PopupParams,
    events: Events,
    requestNewGame: () -> Unit,
    requestResumeGame: (init: suspend GameContext.() -> Unit) -> Unit
) {
    var showAdminOptions by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        events.collect { evt ->
            when (evt) {
                is Event.AccountChanged -> showAdminOptions = evt.account?.isAdmin ?: false
                else -> {}
            }
        }
    }

    TitleLayout(params.data) {
        run {
            var showProceedQuestion by remember { mutableStateOf(false) }
            fun proceed() {
                requestNewGame()
            }

            Button(onClick = {
                if (Data.exists(Data.Keys.Quicksave)) {
                    showProceedQuestion = true
                } else proceed()
            }) { Text("New Game") }

            if (showProceedQuestion) {
                YesNoDialog(
                    "Continue?",
                    "Once you confirm, the existing quick save from your last game will be deleted.\n\nIf you don't want this to happen, go back and choose \"Restore Game\" instead."
                ) { yesNo ->
                    showProceedQuestion = false
                    if (yesNo == YesNo.YES) {
                        // Grab the game that the person aborted, saving info about it before discarding it forever
                        // (But do this carefully to make sure we don't brick the user from being able to start a
                        // game if their quicksave doesn't load)
                        scope.launch {
                            try {
                                val dummyCtx = createGameConext(
                                    params.firebase,
                                    params.data,
                                    params.events,
                                    params.account,
                                    params.settings,
                                    params.userStats,
                                    handleChoice = { error("Unexpected choice made when loading data") })
                                val snapshot = Data.load(Data.Keys.Quicksave)!!
                                snapshot.value.create(dummyCtx.data, dummyCtx.env, dummyCtx.enqueuers) { loadedState ->
                                    params.firebase.analytics.log(Analytics.Event.LevelEnd(loadedState.id.toString(), success = false))
                                }
                            } catch (ignored: Exception) {
                                // Shouldn't happen, but maybe the user tried to load a very old legacy save or
                                // something? In that case, too bad -- we lost the data
                            } finally {
                                Data.delete(Data.Keys.Quicksave)
                                proceed()
                            }
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                requestResumeGame {
                    val snapshot = Data.load(Data.Keys.Quicksave)!!
                    snapshot.value.create(data, env, enqueuers) { loadedState ->
                        state = loadedState
                    }
                    Data.delete(Data.Keys.Quicksave)
                }
            },
            enabled = Data.exists(Data.Keys.Quicksave)
        ) { Text("Restore Game") }

        run {
            var showUserDataMenu by remember { mutableStateOf(false) }

            Button(onClick = { showUserDataMenu = true }) { Text("User Data") }
            if (showUserDataMenu) {
                Menu(
                    closeRequested = { showUserDataMenu = false },
                    UserDataMenu(params, allowClearing = true)
                )
            }
        }

        run {
            var showAccountMenu by remember { mutableStateOf(false) }

            Button(onClick = { showAccountMenu = true }) { Text("Account") }
            if (showAccountMenu) {
                Menu(
                    closeRequested = { showAccountMenu = false },
                    AccountMenu(params)
                )
            }
        }

        Button(onClick = {
            window.open(
                "https://docs.google.com/document/d/1Mv8Pzcc-nERLG4lU2dEs8tTi-2H9tVypEkA8AWBxQE4",
                "_blank"
            )
        }) { Text("How To Play") }

        if (showAdminOptions) {
            Button(
                onClick = {
                    document.loadSnapshotFromDisk(
                        scope,
                        provideGameContext = {
                            val result = CompletableDeferred<GameContext>()
                            requestResumeGame {
                                result.complete(this)
                            }
                            result.await()
                        }
                    )
                },
            ) { Text("Load Snapshot") }
        }

        // Always show the drag/drop gamedata.yaml options last, so they don't get jammed in the middle of the menu
        if (showAdminOptions) {
            Button(
                onClick = {
                    Data.delete(Data.Keys.GameData)
                    window.location.reload()
                },
                Modifier.margin(top = 50.px),
                enabled = Data.exists(Data.Keys.GameData)
            ) {
                Text("Clear Game Data Override")
            }

            Box(
                ReadOnlyStyle
                    .toModifier()
                    .height(100.px)
                    .onDragOver { evt ->
                        evt.preventDefault() // Allow drop
                    }
                    .onDrop { evt ->
                        evt.preventDefault() // We're handling the drop

                        val file = evt.dataTransfer!!.files[0]!!
                        val reader = FileReader()
                        reader.onload = { loadEvt ->
                            val content = loadEvt.target.asDynamic().result as String
                            Data.saveRaw(Data.Keys.GameData, content)
                            window.location.reload()
                            Unit
                        }
                        reader.readAsText(file, "UTF-8")
                    }
                    .cursor(Cursor.Crosshair),
                contentAlignment = Alignment.Center
            ) {
                Text("Drag gamedata.yaml here")
            }
        }
    }
}
