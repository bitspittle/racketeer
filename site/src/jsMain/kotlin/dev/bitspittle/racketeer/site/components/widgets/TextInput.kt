package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.G
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLInputElement

val TextInputLabelStyle = ComponentStyle.base("text-input-label") {
    Modifier
        .fontSize(G.Font.Sizes.ExtraSmall)
        .color(Colors.Grey)
}

val TextInputStyle = ComponentStyle.base("text-input") {
    Modifier
        .margin(bottom = 10.px)
        .fontSize(G.Font.Sizes.Normal)
        .borderStyle(LineStyle.Solid)
}

/** A text input box with a descriptive label above it. */
@Composable
fun LabeledTextInput(
    label: String,
    labelModifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    mask: Boolean = false,
    ref: ((HTMLInputElement) -> Unit)? = null,
    onCommit: () -> Unit = {},
    onValueChanged: (String) -> Unit
) {
    Column {
        SpanText(label, TextInputLabelStyle.toModifier().then(labelModifier))
        TextInput(inputModifier, mask, ref, onCommit, onValueChanged)
    }
}

/** An uncontrolled text input box. */
@Composable
fun TextInput(modifier: Modifier = Modifier, mask: Boolean = false, ref: ((HTMLInputElement) -> Unit)? = null, onCommit: () -> Unit = {}, onValueChanged: (String) -> Unit) {
    Input(
        if (mask) InputType.Password else InputType.Text,
        attrs = TextInputStyle.toModifier().then(modifier).toAttrs {
            if (ref != null) {
                this.ref { element ->
                    ref(element)
                    onDispose { }
                }
            }
            onInput { onValueChanged(it.value) }
            onKeyUp { evt ->
                if (evt.code == "Enter") {
                    evt.preventDefault()
                    onCommit()
                }
            }
        }
    )
}

/** A controlled text input box. */
@Composable
fun TextInput(text: String, modifier: Modifier = Modifier, mask: Boolean = false, ref: ((HTMLInputElement) -> Unit)? = null, onCommit: () -> Unit = {}, onValueChanged: (String) -> Unit) {
    Input(
        if (mask) InputType.Password else InputType.Text,
        attrs = TextInputStyle.toModifier().then(modifier).toAttrs {
            if (ref != null) {
                this.ref { element ->
                    ref(element)
                    onDispose { }
                }
            }
            value(text)
            onInput { onValueChanged(it.value) }
            onKeyUp { evt ->
                if (evt.code == "Enter") {
                    evt.preventDefault()
                    onCommit()
                }
            }
        }
    )
}