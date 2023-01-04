package dev.bitspittle.racketeer.site.components.util

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Popup
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.toCardSpec
import dev.bitspittle.racketeer.site.model.TooltipData
import dev.bitspittle.racketeer.site.model.TooltipParser
import org.jetbrains.compose.web.css.*

val UnderlineModifier = Modifier.borderBottom(1.px, LineStyle.Dotted, Colors.Black)


@Composable
fun renderTextWithTooltips(data: GameData, describer: Describer, tooltipParser: TooltipParser, text: String) {
    val rangeActions = remember(text) {
        val tooltipRanges = tooltipParser.parse(text)
        mutableListOf<Pair<IntRange, @Composable () -> Unit>>().apply {

            // Create ranges that handle tooltips - they should render relevant text with some decoration
            // inviting users to mouse over them.
            tooltipRanges.forEach { tooltipRange ->
                add(tooltipRange.range to {
                    SpanText(text.substring(tooltipRange.range), UnderlineModifier)
                    if (tooltipRange.tooltip is TooltipData.OfText) {
                        Tooltip(ElementTarget.PreviousSibling, tooltipRange.tooltip.text)
                    } else {
                        Popup(ElementTarget.PreviousSibling, placement = PopupPlacement.Right) {
                            val outlineModifier = Modifier.outline(2.px, LineStyle.Solid, Colors.Black)
                            when (val tooltip = tooltipRange.tooltip) {
                                is TooltipData.OfBlueprint -> Card(
                                    data,
                                    describer,
                                    tooltipParser,
                                    tooltip.blueprint.toCardSpec(describer),
                                    modifier = outlineModifier
                                )

                                is TooltipData.OfCard -> Card(
                                    data,
                                    describer,
                                    tooltipParser,
                                    tooltip.card.toCardSpec(data),
                                    modifier = outlineModifier
                                )

                                is TooltipData.OfText -> error("Should have been handled above")
                            }
                        }
                    }
                })
            }

            // Create emoji ranges, so we can prevent them being broken up in the middle
            // e.g. if a description says 🎲🎲🎲🎲, we don't want html layouts to break that
            // into 🎲🎲 and 🎲🎲.
            run {
                fun getEmojiRanges(text: String): List<IntRange> {
                    val regex = "([\uD83C-\uDBFF\uDC00-\uDFFF]+)".toRegex()
                    return regex.findAll(text).map { it.range }.toList()
                }

                (getEmojiRanges(text)).forEach { range ->
                    add(range to {
                        SpanText(text.substring(range), Modifier.whiteSpace(WhiteSpace.NoWrap))
                    })
                }
            }

            this.sortBy { it.first.first }

            // Finally, fill in the remaining ranges but have them just render text directly
            fun findMissingRanges(ranges: List<IntRange>, length: Int): List<IntRange> {
                val result = mutableListOf<IntRange>()
                ranges.fold(0) { current, range ->
                    if (current < range.first) {
                        result.add(IntRange(current, range.first - 1))
                    }
                    range.last + 1
                }
                val finalStart = ranges.lastOrNull()?.last?.plus(1) ?: 0
                if (finalStart < length) {
                    result.add(IntRange(finalStart, length - 1))
                }
                return result
            }

            val missingRanges = findMissingRanges(this.map { it.first }, text.length)

            for (missingRange in missingRanges) {
                add(missingRange to { SpanText(text.substring(missingRange)) })
            }

            this.sortBy { it.first.first }
        }
    }

    rangeActions.forEach { it.second.invoke() }
}