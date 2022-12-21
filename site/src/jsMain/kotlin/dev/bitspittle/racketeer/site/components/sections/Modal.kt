package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import org.jetbrains.compose.web.css.*

val ModalStyle = ComponentStyle.base("modal") {
    Modifier
        .position(Position.Absolute)
        .backgroundColor(Colors.Black.copyf(alpha = 0.7f))
        .top(0.px).bottom(0.px).left(0.px).right(0.px)
}

val ModalContentStyle = ComponentStyle.base("modal-content") {
    Modifier
        .backgroundColor(Colors.White)
        .margin(top = 50.px)
        .padding(20.px)
        .borderRadius(5.percent)
}

@Composable
fun Modal(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(ModalStyle.toModifier(), contentAlignment = Alignment.TopCenter) {
        Box(ModalContentStyle.toModifier().then(modifier)) {
            content()
        }
    }
}