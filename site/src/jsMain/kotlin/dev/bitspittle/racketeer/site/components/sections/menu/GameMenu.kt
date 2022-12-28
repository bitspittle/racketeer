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
            }

            object CreateCard : GameMenuEntry {
                override val title = "Create Card"

                @Composable
                override fun renderContent(params: Params) {
                    params.ctx.data.cards.sortedBy { it.name }.forEach { card ->
                        Button(onClick = {
                            params.updater.runStateChangingAction {
                                params.ctx.state.apply(
                                    GameStateChange.MoveCard(
                                        params.ctx.state,
                                        card.instantiate(),
                                        params.ctx.state.hand,
                                        ListStrategy.FRONT
                                    )
                                )
                            }
                            params.requestClose()
                        }) { Text(card.name) }
                        installPopup(params.ctx, card)
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
