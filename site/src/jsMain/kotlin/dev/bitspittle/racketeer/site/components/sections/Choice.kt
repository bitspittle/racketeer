package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.model.ChoiceContext
import dev.bitspittle.racketeer.site.model.cancel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private const val REQUIRED_CHOICE_EXPLANATION = "This choice is not optional, so you cannot back out of it."

val ChoiceStyle = ComponentStyle.base("choice") {
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .padding(10.px)
        .fillMaxWidth()
}

val SelectedChoiceVariant = ChoiceStyle.addVariantBase("selected") {
    Modifier
        .outline(width = 1.px, style = LineStyle.Solid, color = Colors.Black)

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
    Modal(
        ctx.describer,
        ctx.tooltipParser,
        title = ctx.prompt ?: if (ctx.items.size > 1) "Review all:" else { "Review:" },
        content = {
            ctx.items.forEach { item ->
                Div(ReadOnlyChoiceStyle.toAttrs()) {
                    Text(ctx.describe(item))
                }
                installPopup(ctx, item)
            }
        },
        bottomRow = {
            Button(
                onClick = { ctx.cancel() },
                enabled = !ctx.requiredChoice
            ) {
                Text("Cancel")
            }
            if (ctx.requiredChoice) {
                Tooltip(ElementTarget.PreviousSibling, REQUIRED_CHOICE_EXPLANATION)
            }

            Button(
                onClick = { ctx.choose(ctx.items) },
            ) {
                Text("Confirm")
            }
        }
    )
}

@Composable
private fun PickChoice(ctx: ChoiceContext) {
    Modal(
        ctx.describer,
        ctx.tooltipParser,
        title = ctx.prompt ?: "Choose 1:",
        content = {
            ctx.items.forEach { item ->
                Button(
                    onClick = { ctx.choose(listOf(item)) },
                    ChoiceStyle.toModifier()
                ) {
                    Text(ctx.describe(item))
                }
                installPopup(ctx, item)
            }
        },
        bottomRow = {
            Button(
                onClick = { ctx.cancel() },
                enabled = !ctx.requiredChoice
            ) {
                Text("Cancel")
            }
            if (ctx.requiredChoice) {
                Tooltip(ElementTarget.PreviousSibling, REQUIRED_CHOICE_EXPLANATION)
            }
        }
    )
}

@Composable
private fun PickChoices(ctx: ChoiceContext) {
    val selected = remember { mutableStateMapOf<Any, Unit>() }
    Modal(
        ctx.describer,
        ctx.tooltipParser,
        title = buildString {
            ctx.prompt?.let { append(it); append(' ') }
            append("Choose ${ctx.describer.describeRange(ctx.range)}:")
        },
        content = {
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
        },
        bottomRow = {
            Button(
                onClick = { ctx.cancel() },
                enabled = !ctx.requiredChoice,
            ) {
                Text("Cancel")
            }
            if (ctx.requiredChoice) {
                Tooltip(ElementTarget.PreviousSibling, REQUIRED_CHOICE_EXPLANATION)
            }

            Button(
                onClick = { ctx.choose(ctx.items.filter { it in selected }) },
                enabled = selected.count() in ctx.range
            ) {
                Text("Confirm")
            }
            if (selected.count() !in ctx.range) {
                Tooltip(
                    ElementTarget.PreviousSibling,
                    "You must choose ${ctx.describer.describeRange(ctx.range)} item(s) before you can confirm."
                )
            }
        }
    )
}