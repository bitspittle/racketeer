package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.G
import org.jetbrains.compose.web.css.*

val LabeledBoxStyle = ComponentStyle.base("labeled-box") {
    Modifier
        .border(width = 1.px, style = LineStyle.Solid, color = Colors.Black)
        .backgroundColor(Colors.White)
}

val LabeledBoxTitleStyle = ComponentStyle.base("labeled-box-title") {
    Modifier
        .backgroundColor(Colors.White)
        .padding(leftRight = 5.px)
        .position(Position.Absolute).left(5.px).top((-7).px)
        .fontSize(G.Font.Sizes.Small)
}

@Composable
fun LabeledBox(title: String, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(LabeledBoxStyle.toModifier().position(Position.Relative).then(modifier)) {
        SpanText(title, LabeledBoxTitleStyle.toModifier())
        content()
    }
}