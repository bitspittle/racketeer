package dev.bitspittle.racketeer.site.components.sections.menu

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.silk.components.forms.Button
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.encodeToYaml
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.util.toFilenameString
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.GameUpdater
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.yamlkt.Yaml
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.js.Date

interface GameMenuEntry {
    class Params(
        val scope: CoroutineScope,
        val ctx: GameContext,
        val updater: GameUpdater,
        val visit: (GameMenuEntry) -> Unit,
        val requestClose: () -> Unit,
        val requestQuit: () -> Unit,
    )

    val title: String

    @Composable
    fun renderContent(params: Params)

    object Main : GameMenuEntry {
        override val title = "Main"

        @Composable
        override fun renderContent(params: Params) {
            if (params.ctx.settings.admin.enabled) {
                Button(onClick = { params.visit(Admin) }) { Text(Admin.title) }
            }

            run {
                var showConfirmQuestion by remember { mutableStateOf(false) }
                Button(onClick = { showConfirmQuestion = true }) { Text("Save and Quit") }

                if (showConfirmQuestion) {
                    YesNoDialog(
                        "Are you sure?",
                    ) { yesNo ->
                        showConfirmQuestion = false
                        if (yesNo == YesNo.YES) {
                            params.apply {
                                Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                                requestQuit()
                            }
                        }
                    }
                }
            }
        }

        object Admin : GameMenuEntry {
            override val title = "Admin"

            @Composable
            override fun renderContent(params: Params) {
                Button(onClick = { params.visit(CreateCard) }) { Text(CreateCard.title) }
                Button(onClick = { params.visit(BuildBuilding) }) { Text(BuildBuilding.title) }
                Button(onClick = { params.visit(SaveManagement) }) { Text(SaveManagement.title)}
                Button(
                    onClick = {
                        window.open("https://docs.google.com/spreadsheets/d/1iG38W0xl2UzRHhQX_GvJWg3zZndqY-UkKAVaWzNiLKg/edit#gid=200941839", target = "_blank")
                        params.requestClose()
                    },
                ) { Text("Open API Sheet") }
            }

            object CreateCard : GameMenuEntry {
                override val title = "Create Card"

                @Composable
                override fun renderContent(params: Params) {
                    with(params) {
                        ctx.data.cards.sortedBy { it.name }.forEach { card ->
                            Button(onClick = {
                                updater.runStateChangingAction {
                                    ctx.state.apply(
                                        GameStateChange.MoveCard(
                                            ctx.state,
                                            card.instantiate(),
                                            ctx.state.hand,
                                            ListStrategy.FRONT
                                        )
                                    )
                                }
                                requestClose()
                            }) { Text(card.name) }
                            installPopup(ctx, card)
                        }
                    }
                }
            }

            object BuildBuilding : GameMenuEntry {
                override val title = "Build Building"

                @Composable
                override fun renderContent(params: Params) {
                    with(params) {
                        ctx.data.blueprints.sortedBy { it.name }.forEach { blueprint ->
                            Button(
                                onClick = {
                                    // Run this command in two separate state changing actions; you need to own the blueprint before you can
                                    // build it.
                                    updater.runStateChangingActions(
                                        {
                                            if (!ctx.state.blueprints.contains(blueprint)) {
                                                ctx.state.apply(GameStateChange.AddBlueprint(blueprint))
                                            }
                                        },
                                        {
                                            check(ctx.state.blueprints.indexOf(blueprint) >= 0)
                                            ctx.state.apply(GameStateChange.Build(blueprint, free = true))
                                        },
                                    )
                                    requestClose()
                                },
                                enabled = ctx.state.buildings.none { it.blueprint === blueprint }
                            ) { Text(blueprint.name) }
                            installPopup(params.ctx, blueprint)
                        }
                    }
                }
            }

            object SaveManagement : GameMenuEntry {
                override val title = "Save Management"

                @Composable
                override fun renderContent(params: Params) {
                    Button(
                        onClick = {
                            params.apply {
                                Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                                ctx.logger.debug("Game saved.")
                                requestClose()
                            }
                        },
                    ) { Text("Save now") }

                    Button(
                        onClick = {
                            params.apply {
                                val snapshot = GameSnapshot.from(ctx.describer, ctx.state)
                                Data.save(Data.Keys.Quicksave, snapshot)

                                val snapshotBlob = Blob(arrayOf(snapshot.encodeToYaml()), BlobPropertyBag(type = "text/yaml"))
                                val url = URL.createObjectURL(snapshotBlob)
                                val tempAnchor = (document.createElement("a") as HTMLAnchorElement).apply {
                                    style.display = "none"
                                    href = url
                                    download = "do-crimes_${Date().toFilenameString()}.dcr"
                                }
                                document.body!!.append(tempAnchor)
                                tempAnchor.click()
                                URL.revokeObjectURL(url)
                                tempAnchor.remove()

                                requestClose()
                            }
                        },
                    ) { Text("Download save") }

                    Button(
                        onClick = {
                            params.apply {
                                val tempInput = (document.createElement("input") as HTMLInputElement).apply {
                                    type = "file"
                                    style.display = "none"
                                    accept = ".dcr"
                                    multiple = false
                                }

                                tempInput.onchange = { changeEvt ->
                                    val file = changeEvt.target.asDynamic().files[0] as File

                                    val reader = FileReader()
                                    reader.onload = { loadEvt ->
                                        val content = loadEvt.target.asDynamic().result as String
                                        scope.launch {
                                            val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), content)
                                            snapshot.create(ctx.data, ctx.env, ctx.enqueuers) { newState ->
                                                ctx.state = newState
                                            }

                                            requestClose()
                                        }
                                    }
                                    reader.readAsText(file, "UTF-8")
                                }

                                document.body!!.append(tempInput)
                                tempInput.click()
                                tempInput.remove()
                            }
                        },
                    ) { Text("Load a save") }

                }
            }
        }
    }
}

@Composable
fun GameMenu(scope: CoroutineScope, ctx: GameContext, gameUpdater: GameUpdater, closeRequested: () -> Unit, quitRequested: () -> Unit) {
    val menuStack = remember { mutableStateListOf<GameMenuEntry>(GameMenuEntry.Main) }

    fun goBack() {
        if (menuStack.size >= 2) {
            menuStack.removeLast()
        } else closeRequested()
    }

    Modal(
        ref = inputRef { code ->
            if (code == "Escape") {
                goBack()
                true
            } else false
        },
        titleRow = {
            Spacer(); Text(menuStack.joinToString(" > ") { it.title }); Spacer()
        },
        bottomRow = {
            Button(onClick = { goBack() }) {
                Text(if (menuStack.size >= 2) "Go Back" else "Close")
            }
        },
    ) {
        menuStack.last()
            .renderContent(
                GameMenuEntry.Params(
                    scope,
                    ctx,
                    gameUpdater,
                    { entry -> menuStack.add(entry) },
                    closeRequested,
                    requestQuit = {
                        closeRequested()
                        quitRequested()
                    }
                )
            )
    }

}
