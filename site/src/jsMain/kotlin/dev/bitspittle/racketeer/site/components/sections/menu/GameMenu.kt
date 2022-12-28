package dev.bitspittle.racketeer.site.components.sections.menu

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.GameUpdater
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.*

val MenuButtonsStyle = ComponentStyle("menu-buttons") {
    base { Modifier.fillMaxWidth() }
    cssRule(" *") { Modifier.fillMaxWidth() }
}

interface GameMenuEntry {
    class Params(
        val ctx: GameContext,
        val updater: GameUpdater,
        val visit: (GameMenuEntry) -> Unit,
        val requestClose: () -> Unit,
    )

    val title: String

    @Composable
    fun renderContent(params: Params)

    object Main : GameMenuEntry {
        override val title = "Main"

        @Composable
        override fun renderContent(params: Params) {
            Button(onClick = { params.visit(Admin) }) { Text("Admin") }
        }

        object Admin : GameMenuEntry {
            override val title = "Admin"

            @Composable
            override fun renderContent(params: Params) {
                Button(onClick = { params.visit(CreateCard) }) { Text("Create Card") }
                Button(onClick = { params.visit(BuildBuilding) }) { Text("Build Building") }
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
        }
    }
}

@Composable
fun GameMenu(ctx: GameContext, gameUpdater: GameUpdater, closeRequested: () -> Unit) {
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
                Text("Go Back")
            }
        },
    ) {
        menuStack.last()
            .renderContent(GameMenuEntry.Params(ctx, gameUpdater, { entry -> menuStack.add(entry) }, closeRequested))
    }

}
