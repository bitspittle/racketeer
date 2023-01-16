package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.Width
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.firebase.database.ServerValue
import dev.bitspittle.firebase.database.encodeKey
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import dev.bitspittle.racketeer.site.components.layouts.TitleLayout
import dev.bitspittle.racketeer.site.components.widgets.OkDialog
import dev.bitspittle.racketeer.site.model.account.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun WaitlistScreen(firebase: FirebaseData, account: Account, data: GameData, scope: CoroutineScope, onClose: () -> Unit) {
    val db = firebase.db
    val waitlistRef = db.ref("/waitlist/emails/${account.email!!.lowercase().encodeKey()}")

    var alreadyWaitlisted: Boolean? by remember { mutableStateOf(null) }
    scope.launch {
        alreadyWaitlisted = waitlistRef.get().exists()
    }

    TitleLayout(data) {
        Text("Thank you for your interest!")
        Row(Modifier.flexWrap(FlexWrap.Wrap)) {
            Text("${data.title} is a single player digital card game inspired by games like Dominion. ")
            Text("It is currently locked for pre-release play testing.")
        }

        if (alreadyWaitlisted == false) {
            Row(Modifier.flexWrap(FlexWrap.Wrap)) {
                Text("If you'd like to be considered for a future wave of play testing, please press the ")
                Text("Register button below, and your email (${account.email}) will be added to a wait list.")
            }
        } else if (alreadyWaitlisted == true) {
            Text("Your email (${account.email}) has already been registered.")
        }

        if (alreadyWaitlisted != null) {
            Row(Modifier.margin(top = 2.cssRem).gap(10.px).width(Width.FitContent).align(Alignment.CenterHorizontally)) {
                if (alreadyWaitlisted == false) {

                    Button(onClick = { onClose() }, ref = ref { it.focus() }) {
                        Text("Not now")
                    }

                    run {
                        var showRegisteredDialog by remember { mutableStateOf(false) }
                        Button(onClick = {
                            scope.launch {
                                waitlistRef.set(ServerValue.timestamp())
                                showRegisteredDialog = true
                            }
                        }) {
                            Text("Register")
                        }

                        if (showRegisteredDialog) {
                            OkDialog(
                                "Thank You",
                                "Your information has been registered!\n\nWe are working quickly to enable new waves of play testing, and we appreciate your patience.",
                                onClose = onClose
                            )
                        }
                    }
                } else {
                    Button(onClick = { onClose() }, ref = ref { it.focus() }) { Text("OK") }
                }
            }
        }
    }
}
