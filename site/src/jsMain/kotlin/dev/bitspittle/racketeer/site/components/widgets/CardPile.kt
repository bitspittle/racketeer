package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.sections.BrowsePile
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
    var showBrowseModal by remember { mutableStateOf(false) }
    LabeledBox("${ctx.describer.describePileTitle(ctx.state, pile)} (${pile.cards.size})") {
        Box(CardPileStyle.toModifier()) {
            val enabled = pile.cards.isNotEmpty()
            CardPlaceholder(
                Modifier
                    // Grab looks better than pointer finger for handling a pile of cards
                    .cursor(Cursor.Grab)
                    .thenIf(enabled) {
                        Modifier.onClick {
                            showBrowseModal = true
                        }
                    },
                enabled = enabled
            )
        }
    }

    if (showBrowseModal) {
        BrowsePile(ctx, pile, onDismiss = { showBrowseModal = false })
    }
}