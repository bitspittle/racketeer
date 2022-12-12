package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.G
import org.jetbrains.compose.web.css.*

val CardGroupStyle = ComponentStyle.base("card-group") {
    Modifier
        .border(width = 1.px, style = LineStyle.Solid, color = Colors.Black)
        .flexWrap(FlexWrap.Nowrap)
        .width(G.Sizes.CardGroup.w)
        .minHeight(G.Sizes.CardGroup.h)
        .padding(20.px)
        .borderRadius(2.percent)
        .backgroundColor(Colors.White)
        .overflowX(Overflow.Auto)
        .gap(10.px)
}

val CardGroupTitleStyle = ComponentStyle.base("card-group-title") {
    Modifier
        .backgroundColor(Colors.White)
        .padding(leftRight = 5.px)
        .position(Position.Absolute).left(5.px).top((-10).px)
        .fontSize(G.Font.Sizes.Small)
}

@Composable
fun CardGroup(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(Modifier.position(Position.Relative)) {
        SpanText(title, CardGroupTitleStyle.toModifier())
        Row(CardGroupStyle.toModifier().then(modifier), verticalAlignment = Alignment.Bottom) {
            content()
        }
    }
}