package dev.bitspittle.racketeer.site.components.sections.menu.menus.user

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.components.forms.Button
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.sections.menu.MenuButton
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.model.user.clear
import org.jetbrains.compose.web.dom.*

class UserDataMenu(private val params: PopupParams, private val allowClearing: Boolean = false) : Menu {
    override val title = "User Data"

    @Composable
    override fun renderContent(actions: MenuActions) {
        MenuButton(actions, CardListMenu(params))
        MenuButton(actions, BuildingListMenu(params))
        MenuButton(actions, UnlocksMenu(params))

        if (allowClearing) {
            var showConfirmDialog by remember { mutableStateOf(false) }

            Button(onClick = { showConfirmDialog = true }) {
                Text("Clear All Data")
            }

            if (showConfirmDialog) {
                YesNoDialog(
                    "Continue?",
                    "This will clear all data!\n\nThat includes unlocks, stats for cards and buildings, and any game in progress.\n\nAre you absolutely sure?"
                ) { yesNo ->
                    showConfirmDialog = false
                    if (yesNo == YesNo.YES) {
                        Data.delete(Data.Keys.Quicksave)
                        Data.delete(Data.Keys.UserStats)
                        params.userStats.clear()
                        actions.close()
                    }
                }
            }
        }
    }
}
