package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.model.GameContext
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

val CardPileStyle = ComponentStyle.base("card-pile") {
    Modifier
        .minWidth(G.Sizes.CardGroup.w)
        .minHeight(G.Sizes.CardGroup.h)
        .padding(20.px)
}

val CardPileCountStyle = ComponentStyle.base("card-pile-count") {
    Modifier
        .padding(10.px)
        .borderRadius(10.percent)
        .fontSize(G.Font.Sizes.Large)
        .fontWeight(FontWeight.Bold)
        .border(1.px, LineStyle.Solid, Colors.Black)
        .backgroundColor(Colors.White)
}

@Composable
fun CardPile(ctx: GameContext, pile: Pile) {
    LabeledBox(ctx.describer.describePileTitle(ctx.state, pile)) {
        Box(CardPileStyle.toModifier()) {
            Box(CardStyleMinimal.toModifier(CardBackVariant), contentAlignment = Alignment.Center) {
                SpanText(pile.cards.size.toString(), CardPileCountStyle.toModifier())
            }
        }
    }
}