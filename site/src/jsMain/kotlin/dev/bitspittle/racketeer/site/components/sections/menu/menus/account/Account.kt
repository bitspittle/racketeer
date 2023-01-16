package dev.bitspittle.racketeer.site.components.sections.menu.menus.account

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.*

class AccountMenu(private val params: PopupParams) : Menu {
    override val title = "Account"

    override val topRow: @Composable RowScope.() -> Unit = {
        params.account.email?.let { email ->
            SpanText("You are signed is as ${email}.", Modifier.fillMaxWidth().textAlign(TextAlign.Center))
        }
    }

    @Composable
    override fun renderContent(actions: MenuActions) {
        run {
            val scope = rememberCoroutineScope()
            var showConfirmDialog by remember { mutableStateOf(false) }

            Button(onClick = { showConfirmDialog = true }) {
                Text("Log Out")
            }

            if (showConfirmDialog) {
                YesNoDialog(
                    "Log Out?",
                ) { yesNo ->
                    showConfirmDialog = false
                    if (yesNo == YesNo.YES) {
                        scope.launch { params.firebase.auth.signOut() }
                        Data.delete(Data.Keys.Account)
                        actions.close()
                    }
                }
            }
        }

        run {
            val scope = rememberCoroutineScope()
            var showConfirmDialog by remember { mutableStateOf(false) }

            Button(onClick = { showConfirmDialog = true }) {
                Text("Delete Account")
            }

            if (showConfirmDialog) {
                YesNoDialog(
                    "Confirm Deletion",
                    question = "This is a destructive action and not reversible! Consider logging out instead.\n\nAre you sure you wish to proceed?"
                ) { yesNo ->
                    showConfirmDialog = false
                    if (yesNo == YesNo.YES) {
                        scope.launch { params.firebase.auth.currentUser?.delete() }
                        Data.delete(Data.Keys.Account)
                        actions.close()
                    }
                }
            }
        }
    }
}
