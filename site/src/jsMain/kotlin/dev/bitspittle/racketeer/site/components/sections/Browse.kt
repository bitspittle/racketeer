package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.GameContext
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

val ReadOnlyStyle = ComponentStyle.base("read-only") {
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .padding(10.px)
        .border(1.px, LineStyle.Solid, Colors.Black)
        .borderRadius(4.px)
        .cursor(Cursor.Help)
}

@Composable
fun BrowsePile(ctx: GameContext, pile: Pile, onDismiss: () -> Unit) {
    Modal(
        ctx.data,
        ctx.describer,
        ctx.tooltipParser,
        overlayModifier = Modifier.onClick { onDismiss() },
        dialogModifier = Modifier.onClick { evt -> evt.stopPropagation() }, // So click doesn't get to overlay
        ref = inputRef {
            if (code == "Escape") {
                onDismiss()
                true
            } else false
        },
        title = "Browsing ${ctx.describer.describePile(ctx.state, pile)}...",
        content = {
            pile.cards.sortedBy { it.template.name }.groupBy { it.template.name }.forEach { (_, cards) ->
                val totalVp = cards.sumOf { it.vpTotal }.takeIf { it > 0 }
                Box(ReadOnlyStyle.toModifier()) {
                    SpanText(ctx.describer.describeCardGroupTitle(cards), Modifier.margin(right = 5.px))
                    if (totalVp != null) {
                        SpanText("(${ctx.describer.describeVictoryPoints(totalVp)})", Modifier.align(Alignment.End).textAlign(TextAlign.End))
                    }
                }
                installPopup(ctx, cards)
            }
        },
        bottomRow = {
            Button(onClick = { onDismiss() }) {
                Text("Close")
            }
        }
    )
}