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
import dev.bitspittle.racketeer.site.G
import org.jetbrains.compose.web.css.*

val CardGroupStyle = ComponentStyle.base("card-group") {
    Modifier
        .flexWrap(FlexWrap.Nowrap)
        .minHeight(G.Sizes.CardGroup.h)
        .padding(20.px)
        .gap(10.px)
}

@Composable
fun CardGroup(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LabeledBox(title, modifier) {
        Box(Modifier.fillMaxSize().overflowX(Overflow.Auto)) {
            Row(CardGroupStyle.toModifier(), verticalAlignment = Alignment.Bottom) {
                content()
            }
        }
    }
}