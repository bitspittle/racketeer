package dev.bitspittle.racketeer.site.components.sections.menu.menus.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.components.forms.Button
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.components.sections.menu.*
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import org.jetbrains.compose.web.dom.*

class MainMenu(private val params: GameMenuParams) : Menu {
    override val title = "Main"

    @Composable
    override fun renderContent(actions: MenuActions) {
        if (params.ctx.settings.admin.enabled) {
            MenuButton(actions, AdminMenu(params))
        }

        MenuButton(actions, BrowseAllCardsMenu(params))
        MenuButton(actions, ReviewHistoryMenu(params))

        run {
            var showConfirmQuestion by remember { mutableStateOf(false) }
            Button(onClick = { showConfirmQuestion = true }) { Text("Restart") }

            if (showConfirmQuestion) {
                YesNoDialog(
                    "Are you sure?",
                ) { yesNo ->
                    showConfirmQuestion = false
                    if (yesNo == YesNo.YES) {
                        params.restart()
                    }
                }
            }
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
                        with(params) {
                            Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                            params.quit()
                        }
                    }
                }
            }
        }
    }
}
