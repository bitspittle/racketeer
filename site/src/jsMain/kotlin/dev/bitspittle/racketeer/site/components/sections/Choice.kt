package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.components.widgets.silk.*
import dev.bitspittle.racketeer.site.model.ChoiceContext
import dev.bitspittle.racketeer.site.model.cancel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private const val REQUIRED_CHOICE_EXPLANATION = "This choice is not optional, so you cannot back out of it."

private val CommonButtonStyle = Modifier
    .fontSize(G.Font.Sizes.Normal)
    .padding(10.px)


val ChoiceTitleStyle = ComponentStyle.base("choice-title") {
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .fontWeight(FontWeight.Bold)
        .margin(bottom = 30.px)
}

val ChoiceButtonStyle = ComponentStyle.base("choice-button") {
    CommonButtonStyle
        .fillMaxWidth()
        .margin(bottom = 10.px)
}

val SelectedChoiceButtonVariant = ChoiceButtonStyle.addVariantBase("selected") {
    Modifier
        .outline(width = 1.px, style = LineStyle.Solid, color = Colors.Black)

}

val ChoiceSystemButtonStyle = ComponentStyle.base("choice-system-button") {
    CommonButtonStyle.flexGrow(1)
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
private fun ReviewChoices(ctx: ChoiceContext) {
    Modal(Modifier.width(360.px)) {
        Column {
            SpanText(
                ctx.prompt ?: if (ctx.items.size > 1) {
                    "Review all:"
                } else {
                    "Review:"
                },
                ChoiceTitleStyle.toModifier()
            )

            ctx.items.forEach { item ->
                Button(
                    onClick = {}, ChoiceButtonStyle.toModifier()
                ) {
                    Text(ctx.describe(item))
                }
            }
            Row(Modifier.fillMaxWidth().gap(10.px)) {
                Button(
                    onClick = { ctx.cancel() },
                    ChoiceSystemButtonStyle.toModifier(),
                    enabled = !ctx.requiredChoice
                ) {
                    Text("Cancel")
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
                    ChoiceButtonStyle.toModifier()
                ) {
                    Text(ctx.describe(item))
                }
            }
            Button(
                onClick = { ctx.cancel() },
                ChoiceSystemButtonStyle.toModifier().fillMaxWidth(),
                enabled = !ctx.requiredChoice
            ) {
                Text("Cancel")
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
                }, ChoiceButtonStyle.toModifier(SelectedChoiceButtonVariant.takeIf { selected.contains(item) })) {
                    Text(ctx.describe(item))
                }

                (item as? Blueprint)?.let { blueprint ->
                    Popup(ElementTarget.PreviousSibling, placement = Placement.Right) {
                        Card(ctx.describer, blueprint.toCardSpec())
                    }
                }
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