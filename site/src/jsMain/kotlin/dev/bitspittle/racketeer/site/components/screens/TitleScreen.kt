package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.loadFileFromDisk
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.model.*
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.yamlkt.Yaml
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun TitleScreen(
    scope: CoroutineScope,
    settings: Settings,
    events: Events,
    requestNewGameContext: (init: suspend GameContext.() -> Unit) -> Unit
) {
    var showAdminOptions by remember { mutableStateOf(settings.admin.enabled) }
    LaunchedEffect(Unit) {
        events.collect { evt ->
            when (evt) {
                is Event.SettingsChanged -> showAdminOptions = evt.settings.admin.enabled
                else -> {}
            }
        }
    }

    Box(Modifier.fillMaxSize().padding(5.percent), contentAlignment = Alignment.TopCenter) {
        Column(FullWidthChildrenStyle.toModifier().gap(15.px)) {
            H1(Modifier.margin(bottom = 10.px).toAttrs()) { Text("Do Crimes") }

            run {
                var showProceedQuestion by remember { mutableStateOf(false) }
                fun proceed() {
                    requestNewGameContext { startNewGame() }
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
                            Data.delete(Data.Keys.Quicksave)
                            proceed()
                        }
                    }
                }
            }
            Button(
                onClick = {
                    requestNewGameContext {
                        val snapshot = Data.load(Data.Keys.Quicksave)!!
                        snapshot.value.create(data, env, enqueuers) { loadedState ->
                            state = loadedState
                        }
                        Data.delete(Data.Keys.Quicksave)
                    }
                },
                enabled = Data.exists(Data.Keys.Quicksave)
                ) { Text("Restore Game") }

            if (showAdminOptions) {
                Button(
                    onClick = {
                        document.loadFileFromDisk(".dcr") { content ->
                            scope.launch {
                                requestNewGameContext {
                                    val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), content)
                                    snapshot.create(data, env, enqueuers) { newState ->
                                        state = newState
                                    }
                                }
                            }
                        }
                    },
                ) { Text("Load Snapshot") }
            }
        }
    }
}
