package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaGoogle
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.firebase.auth.AuthError
import dev.bitspittle.firebase.auth.GoogleAuthProvider
import dev.bitspittle.firebase.auth.Scope
import dev.bitspittle.firebase.util.FirebaseError
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import dev.bitspittle.racketeer.site.components.widgets.OkDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun LoginScreen(firebase: FirebaseData, scope: CoroutineScope) {
    Box(Modifier.fillMaxSize().padding(5.percent), contentAlignment = Alignment.TopCenter) {
        Column(FullWidthChildrenStyle.toModifier().gap(15.px)) {
            run {
                var error: AuthError? by remember { mutableStateOf(null) }
                Button(onClick = {
                    scope.launch {
                        try {
                            val provider = GoogleAuthProvider()
                            provider.addScope(Scope.Google.Email)
                            firebase.auth.signInWithPopup(provider)
                        } catch (e: AuthError) {
                            error = e
                        }
                    }
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FaGoogle(Modifier.margin(right = 10.px))
                        SpanText("Login With Google");
                    }
                }

                if (error != null) {
                    OkDialog("Login failed", error!!.message, onClose = { error = null })
                }
            }

            run {
                Button(onClick = {
                    scope.launch {
//                        firebase.auth.signInWithEmailAndPassword()
                    }
                }) { Row { SpanText("Login With Email"); } }
            }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Hr(Modifier.fillMaxWidth().toAttrs())
                SpanText("or", Modifier.padding(leftRight = 5.px).backgroundColor(Colors.White))
            }
            Button(onClick = {}) { Row { SpanText("Create Email Account"); } }
        }
    }
}
