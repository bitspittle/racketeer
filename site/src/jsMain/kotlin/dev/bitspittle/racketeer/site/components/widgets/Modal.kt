package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.theme.toSilkPalette
import org.jetbrains.compose.web.css.*

val ModalStyle = ComponentStyle.base("modal") {
    Modifier
        .backgroundColor(colorMode.toSilkPalette().background)
        .margin(top = 15.percent)
        .padding(20.px)
        .borderRadius(5.percent)
}

@Composable
fun Modal(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Overlay {
        Box(ModalStyle.toModifier().then(modifier)) {
            content()
        }
    }
}