package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.overlay.Popup
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.model.ChoiceContext
import dev.bitspittle.racketeer.site.model.cancel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private const val REQUIRED_CHOICE_EXPLANATION = "This choice is not optional, so you cannot back out of it."

private val CommonChoiceStyle = Modifier
    .fontSize(G.Font.Sizes.Normal)
    .padding(10.px)


val ChoiceTitleStyle = ComponentStyle.base("choice-title") {
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .fontWeight(FontWeight.Bold)
        .margin(bottom = 30.px)
}

val ChoiceStyle = ComponentStyle.base("choice") {
    CommonChoiceStyle
        .fillMaxWidth()
        .margin(bottom = 10.px)
}

val SelectedChoiceVariant = ChoiceStyle.addVariantBase("selected") {
    Modifier
        .outline(width = 1.px, style = LineStyle.Solid, color = Colors.Black)

}

val ChoiceTextDivVariant = ChoiceStyle.addVariantBase("text") {
    Modifier
        .cursor(Cursor.Help)
        .userSelect(UserSelect.None)
}

val ChoiceSystemButtonStyle = ComponentStyle.base("choice-system-button") {
    CommonChoiceStyle.flexGrow(1)
        .margin(top = 20.px)
}

@Composable
fun Choice(ctx: ChoiceContext) = ctx.apply {
    if (range.first == range.last && range.first == items.size) {
        // You have to pick exactly the whole list. Not much of a choice actually!
        ReviewChoices(ctx)
    } else if (range.first == range.last && range.first == 1) {
        // You have to pick exactly one item from a list, no need for a confirm button.
        PickChoice(ctx)
    } else {
        // Normal multi-select choose screen.
        PickChoices(ctx)
    }
}

@Composable
private fun installPopup(ctx: ChoiceContext, item: Any) {
    @Composable
    fun RightPopup(content: @Composable BoxScope.() -> Unit) {
        Popup(ElementTarget.PreviousSibling, placement = PopupPlacement.Right, content = content)
    }

    when (item) {
        is Blueprint -> RightPopup { Card(ctx.describer, ctx.tooltipParser, item.toCardSpec()) }
        is Building -> RightPopup { Card(ctx.describer, ctx.tooltipParser, item.toCardSpec()) }
        is Card -> RightPopup { Card(ctx.describer, ctx.tooltipParser, item.toCardSpec()) }
        is CardTemplate -> RightPopup { Card(ctx.describer, ctx.tooltipParser, item.toCardSpec()) }
        is FormattedItem -> installPopup(ctx, item.wrapped)
    }
}

@Composable
private fun ReviewChoices(ctx: ChoiceContext) {
    Modal {
        Column {
            SpanText(
                ctx.prompt ?: if (ctx.items.size > 1) {
                    "Review all:"
                } else {
                    "Review:"
                },
                ChoiceTitleStyle.toModifier().align(Alignment.CenterHorizontally)
            )

            ctx.items.forEach { item ->
                Div(ChoiceStyle.toModifier(SelectedChoiceVariant, ChoiceTextDivVariant).toAttrs()) {
                    Text(ctx.describe(item))
                }
                installPopup(ctx, item)
            }
            Row(Modifier.fillMaxWidth().gap(10.px)) {
                Button(
                    onClick = { ctx.cancel() },
                    ChoiceSystemButtonStyle.toModifier(),
                    enabled = !ctx.requiredChoice
                ) {
                    Text("Cancel")
                }
                if (ctx.requiredChoice) {
                    Tooltip(ElementTarget.PreviousSibling, REQUIRED_CHOICE_EXPLANATION)
                }

                Button(
                    onClick = { ctx.choose(ctx.items) },
                    ChoiceSystemButtonStyle.toModifier()
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
private fun PickChoice(ctx: ChoiceContext) {
    Modal {
        Column {
            SpanText(
                buildString {
                    ctx.prompt?.let { append(it); append(' ') }
                    append("Choose 1:")
                },
                ChoiceTitleStyle.toModifier()
            )
            ctx.items.forEach { item ->
                Button(
                    onClick = { ctx.choose(listOf(item)) },
                    ChoiceStyle.toModifier()
                ) {
                    Text(ctx.describe(item))
                }
                installPopup(ctx, item)
            }
            Button(
                onClick = { ctx.cancel() },
                ChoiceSystemButtonStyle.toModifier().fillMaxWidth(),
                enabled = !ctx.requiredChoice
            ) {
                Text("Cancel")
            }
            if (ctx.requiredChoice) {
                Tooltip(ElementTarget.PreviousSibling, REQUIRED_CHOICE_EXPLANATION)
            }
        }
    }
}

@Composable
private fun PickChoices(ctx: ChoiceContext) {
    val selected = remember { mutableStateMapOf<Any, Unit>() }

    Modal {
        Column {
            SpanText(
                buildString {
                    ctx.prompt?.let { append(it); append(' ') }
                    append("Choose ${ctx.describer.describeRange(ctx.range)}:")
                },
                ChoiceTitleStyle.toModifier()
            )
            ctx.items.forEach { item ->
                Button(onClick = {
                    if (selected.remove(item) == null) {
                        selected[item] = Unit
                    }
                }, ChoiceStyle.toModifier(SelectedChoiceVariant.takeIf { selected.contains(item) })) {
                    Text(ctx.describe(item))
                }
                installPopup(ctx, item)
            }
            Row(Modifier.fillMaxWidth().gap(10.px)) {
                Button(
                    onClick = { ctx.cancel() },
                    ChoiceSystemButtonStyle.toModifier(),
                    enabled = !ctx.requiredChoice,
                ) {
                    Text("Cancel")
                }
                if (ctx.requiredChoice) {
                    Tooltip(ElementTarget.PreviousSibling, REQUIRED_CHOICE_EXPLANATION)
                }

                Button(
                    onClick = { ctx.choose(ctx.items.filter { it in selected }) },
                    ChoiceSystemButtonStyle.toModifier(),
                    enabled = selected.count() in ctx.range
                ) {
                    Text("Confirm")
                }
                if (selected.count() !in ctx.range) {
                    Tooltip(ElementTarget.PreviousSibling, "You must choose ${ctx.describer.describeRange(ctx.range)} item(s) before you can confirm.")
                }
            }
        }
    }
}