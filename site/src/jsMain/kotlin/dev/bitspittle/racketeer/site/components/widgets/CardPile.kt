package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.model.GameContext
import org.jetbrains.compose.web.css.*

val CardPileStyle = ComponentStyle.base("card-pile") {
    Modifier
        .minWidth(G.Sizes.CardGroup.w)
        .minHeight(G.Sizes.CardGroup.h)
        .padding(20.px)
}

@Composable
fun CardPile(ctx: GameContext, pile: Pile) {
    LabeledBox("${ctx.describer.describePileTitle(ctx.state, pile)} (${pile.cards.size})") {
        Box(CardPileStyle.toModifier()) {
            CardPlaceholder(enabled = pile.cards.isNotEmpty())
        }
    }
}