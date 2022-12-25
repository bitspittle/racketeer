package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.model.GameContext
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

val ReadOnlyChoiceStyle = ComponentStyle.base("read-only-choice") {
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .padding(10.px)
        .fillMaxWidth()
        .border(1.px, LineStyle.Solid, Colors.Black)
        .borderRadius(4.px)
        .cursor(Cursor.Help)
        .userSelect(UserSelect.None)
}

@Composable
private fun installPopup(ctx: GameContext, card: Card) =
    dev.bitspittle.racketeer.site.components.util.installPopup(ctx.describer, ctx.tooltipParser, card)

@Composable
fun BrowsePile(ctx: GameContext, pile: Pile, onDismiss: () -> Unit) {
    Modal(
        overlayModifier = Modifier.onClick { onDismiss() },
        dialogModifier = Modifier.onClick { evt -> evt.stopPropagation() }, // So click doesn't get to overlay
        title = "Browsing ${ctx.describer.describePile(ctx.state, pile)}...",
        content = {
            pile.cards.sortedBy { it.template.name }.forEach { card ->
                Div(ReadOnlyChoiceStyle.toModifier().toAttrs()) {
                    Text(ctx.describer.describeCardTitle(card))
                }
                installPopup(ctx, card)
            }
        },
        bottomRow = {
            Button(onClick = { onDismiss() }) {
                Text("Close")
            }
        }
    )
}