package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaGoogle
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.firebase.auth.AuthError
import dev.bitspittle.firebase.auth.GoogleAuthProvider
import dev.bitspittle.firebase.auth.Scope
import dev.bitspittle.firebase.auth.User
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import dev.bitspittle.racketeer.site.components.layouts.TitleLayout
import dev.bitspittle.racketeer.site.components.widgets.LabeledTextInput
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.components.widgets.OkDialog
import dev.bitspittle.racketeer.site.inputRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun LoginScreen(firebase: FirebaseData, data: GameData, scope: CoroutineScope, onLoggedIn: (user: User) -> Unit) {
    var showPleaseWaitOverlay by remember { mutableStateOf(false) }
    var error: AuthError? by remember { mutableStateOf(null) }

    TitleLayout(data) {
        run {
            Button(onClick = {
                scope.launch {
                    try {
                        val provider = GoogleAuthProvider()
                        provider.addScope(Scope.Google.Email)
                        showPleaseWaitOverlay = true
                        val credential = firebase.auth.signInWithPopup(provider)
                        onLoggedIn(credential.user)
                    } catch (e: AuthError) {
                        error = e
                    } finally {
                        showPleaseWaitOverlay = false
                    }
                }
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FaGoogle(Modifier.margin(right = 10.px))
                    SpanText("Login With Google")
                }
            }
        }

        run {
            var showLoginModal by remember { mutableStateOf(false) }

            Button(onClick = { showLoginModal = true }) {
                Text("Login With Email")
            }

            if (showLoginModal) {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                val canLogin = email.isNotBlank() && password.isNotBlank()
                fun login() {
                    if (!canLogin) return
                    scope.launch {
                        try {
                            showLoginModal = false
                            showPleaseWaitOverlay = true
                            val credential = firebase.auth.signInWithEmailAndPassword(email, password)
                            onLoggedIn(credential.user)
                        } catch (e: AuthError) {
                            error = e
                        } finally {
                            showPleaseWaitOverlay = false
                        }
                    }
                }

                Modal(
                    ref = inputRef {
                        if (code == "Escape") showLoginModal = false
                        true
                    },
                    title = "Login With Email",
                    bottomRow = {
                        Button(onClick = { showLoginModal = false }) { Text("Cancel") }
                        Button(onClick = { login() }, enabled = canLogin) { Text("Login") }
                    }
                ) {
                    LabeledTextInput(
                        "Email",
                        type = InputType.Email,
                        inputModifier = Modifier.fillMaxWidth(),
                        onValueChanged = { email = it },
                        onCommit = { login() },
                        ref = { element -> element.focus() }
                    )
                    LabeledTextInput(
                        "Password",
                        type = InputType.Password,
                        inputModifier = Modifier.fillMaxWidth(),
                        onValueChanged = { password = it },
                        onCommit = { login() },
                    )
                }
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Hr(Modifier.fillMaxWidth().toAttrs())
            SpanText("or", Modifier.padding(leftRight = 5.px).backgroundColor(Colors.White))
        }

        run {
            var showLoginModal by remember { mutableStateOf(false) }

            Button(onClick = { showLoginModal = true }) {
                Text("Create Account")
            }

            if (showLoginModal) {
                var email by remember { mutableStateOf("") }
                var password1 by remember { mutableStateOf("") }
                var password2 by remember { mutableStateOf("") }
                val canCreate = email.isNotBlank() && password1.isNotBlank() && password1 == password2
                fun createAccount() {
                    if (!canCreate) return
                    scope.launch {
                        try {
                            showLoginModal = false
                            showPleaseWaitOverlay = true
                            val credential = firebase.auth.createUserWithEmailAndPassword(email, password1)

                            credential.user.sendEmailVerification()
                            onLoggedIn(credential.user)
                        } catch (e: AuthError) {
                            error = e
                        } finally {
                            showPleaseWaitOverlay = false
                        }
                    }
                }

                Modal(
                    ref = inputRef {
                        if (code == "Escape") showLoginModal = false
                        true
                    },
                    title = "Create Account",
                    bottomRow = {
                        Button(onClick = { showLoginModal = false }) { Text("Cancel") }
                        Button(onClick = { createAccount() }, enabled = canCreate) { Text("Create") }
                    }
                ) {
                    LabeledTextInput(
                        "Email",
                        type = InputType.Email,
                        inputModifier = Modifier.fillMaxWidth(),
                        onValueChanged = { email = it },
                        onCommit = { createAccount() },
                        ref = { element -> element.focus() }
                    )
                    LabeledTextInput(
                        "Password",
                        type = InputType.Password,
                        inputModifier = Modifier.fillMaxWidth(),
                        onValueChanged = { password1 = it },
                        onCommit = { createAccount() },
                    )

                    LabeledTextInput(
                        "Verify password",
                        type = InputType.Password,
                        inputModifier = Modifier.fillMaxWidth(),
                        onValueChanged = { password2 = it },
                        onCommit = { createAccount() },
                    )
                }
            }
        }
    }

    if (showPleaseWaitOverlay) {
        Overlay(Modifier.cursor(Cursor.Progress))
    }

    if (error != null) {
        OkDialog("Login failed", error!!.message, onClose = { error = null })
    }
}
