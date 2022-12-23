package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Popup
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.model.TooltipData
import dev.bitspittle.racketeer.site.model.TooltipParser
import dev.bitspittle.racketeer.site.model.TooltipRange
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val CardStyleCommon =
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .padding(topBottom = 5.px)
        .borderRadius(5.percent)
        .backgroundColor(G.Colors.Card.Front)
        .color(Colors.Black)
        .outlineStyle(LineStyle.None)

private val CardStyleCommonHover =
    Modifier
        .boxShadow(blurRadius = 10.px, spreadRadius = 2.px, color = Colors.Yellow)
        .cursor(Cursor.Pointer)

private val CardStyleCommonFocus =
    Modifier
        .boxShadow(blurRadius = 10.px, spreadRadius = 2.px, color = Colors.Red)


val CardStyle = ComponentStyle("card") {
    base {
        CardStyleCommon
            .width(G.Sizes.Card.w).flexShrink(0) // Needed to prevent Row from resizing the elements
            .height(G.Sizes.Card.h)
    }

    hover {
        CardStyleCommonHover
    }

    focus {
        CardStyleCommonFocus
    }
}

val CardBackVariant = CardStyle.addVariantBase("back") {
    Modifier
        .backgroundColor(G.Colors.Card.Back)
}

val DisabledCardVariant = CardStyle.addVariantBase("disabled") {
    Modifier.opacity(G.Colors.DisabledOpacity)
}

val CardTitleStyle = ComponentStyle.base("card-title") {
    Modifier
        .textAlign(TextAlign.Center)
        .fontSize(G.Font.Sizes.Normal)
}

val CardDescriptionStyle = ComponentStyle.base("card-desc") {
    Modifier
        .fontSize(G.Font.Sizes.Small)
}

val CardDescriptionFlavorVariant = CardDescriptionStyle.addVariantBase("flavor") {
    Modifier.fontStyle(FontStyle.Italic)
}

private val UnderlineModifier = Modifier.borderBottom(1.px, LineStyle.Dotted, Colors.Black)

val CardDescriptionUpgradesVariant = CardDescriptionStyle.addVariantBase("upgrades") {
    UnderlineModifier
        .fontStyle(FontStyle.Italic)

}

val CardDescriptionEffectsVariant = CardDescriptionStyle.addVariantBase("effects") {
    Modifier
        .overflowWrap(OverflowWrap.BreakWord) // Don't break between emojis
        .margin(top = 5.px)
        .styleModifier {
            property("text-shadow", "0px 0px 4px #000000")
        }
}

interface CardSpec {
    val title: String
    val types: List<String>
    val tier: Int?
    val rarity: Int
    val vpBase: Int
    val vpTotal: Int?
    val counter: Int
    val flavor: String?
    val upgrades: Set<UpgradeType>
    val traits: Set<TraitType>
    val ability: String
    val enabled: Boolean
}

fun Card.toCardSpec(enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val title = self.template.name
        override val types = self.template.types
        override val tier = self.template.tier
        override val rarity = self.template.rarity
        override val vpBase = self.template.vp
        override val vpTotal = self.vpTotal
        override val counter = self.counter
        override val flavor = self.template.description.flavor
        override val upgrades = self.upgrades
        override val traits = self.traits
        override val ability = self.template.description.ability
        override val enabled = enabled
    }
}

fun CardTemplate.toCardSpec(enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val title = self.name
        override val types = self.types
        override val tier = self.tier
        override val rarity = self.rarity
        override val vpBase = self.vp
        override val vpTotal = 0
        override val counter = 0
        override val flavor = self.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val traits = emptySet<TraitType>()
        override val ability = self.description.ability
        override val enabled = enabled
    }
}

@Composable
fun Card(describer: Describer, tooltipParser: TooltipParser, card: Card, onClick: () -> Unit = {}, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Card(describer, tooltipParser, card.toCardSpec(enabled), onClick, modifier)
}

@Composable
fun Card(describer: Describer, tooltipParser: TooltipParser, card: CardSpec, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(CardStyle
        .toModifier(DisabledCardVariant.takeUnless { card.enabled })
        .thenIf(card.enabled) {
            Modifier.tabIndex(0).onClick { onClick() }
        }
        .then(modifier)
    ) {
        Column(Modifier.fillMaxWidth().height(40.px), horizontalAlignment = Alignment.CenterHorizontally) {
            SpanText(buildString {
                card.tier?.let { tier ->
                    append("Tier ${tier + 1}, ")
                }
                append(describer.describeRarity(card.rarity))
            }, CardDescriptionStyle.toModifier())
            SpanText(card.title, CardTitleStyle.toModifier())
            if (card.types.isNotEmpty()) {
                SpanText(describer.describeTypes(card.types), CardDescriptionStyle.toModifier())
            }

            val vpTotal = card.vpTotal ?: card.vpBase
            if (vpTotal > 0 || card.vpBase > 0) {
                Row(CardDescriptionStyle.toModifier().gap(5.px)) {
                    if (card.vpBase > 0) {
                        SpanText(describer.describeVictoryPoints(card.vpBase))
                    }
                    val deltaVp = vpTotal - card.vpBase
                    if (deltaVp > 0) {
                        SpanText("+${describer.describeVictoryPoints(deltaVp)}")
                    } else if (deltaVp < 0) {
                        SpanText("-${describer.describeVictoryPoints(-deltaVp)}")
                    }
                }
            }
            if (card.counter > 0) {
                SpanText("Counter: ${card.counter}", CardDescriptionStyle.toModifier())
            }
        }
        Spacer()
        Column(Modifier.margin(leftRight = 15.px, topBottom = 3.px)) {
            Row(Modifier.gap(5.px).flexWrap(FlexWrap.Wrap)) {
                card.upgrades.forEach { upgrade ->
                    SpanText(
                        describer.describeUpgradeBody(upgrade),
                        CardDescriptionStyle.toModifier(CardDescriptionUpgradesVariant)
                    )
                    Tooltip(
                        ElementTarget.PreviousSibling,
                        describer.describeUpgradeBody(upgrade),
                        placement = PopupPlacement.Bottom
                    )
                }
                card.traits.forEach { trait ->
                    SpanText(
                        describer.describeTraitTitle(trait),
                        CardDescriptionStyle.toModifier(CardDescriptionUpgradesVariant)
                    )
                    Tooltip(
                        ElementTarget.PreviousSibling,
                        describer.describeTraitBody(trait),
                        placement = PopupPlacement.Bottom
                    )
                }
            }
            Span(CardDescriptionStyle.toModifier(CardDescriptionEffectsVariant).toAttrs()) {
                val abilityText = describer.convertIcons(card.ability)
                val tooltipRanges = remember { tooltipParser.parse(abilityText) }
                if (tooltipRanges.isEmpty()) {
                    Text(abilityText)
                } else {
                    SpanText(abilityText.substring(0, tooltipRanges.first().range.first))
                    var prevTooltipRange: TooltipRange? = null
                    tooltipRanges.forEach { tooltipRange ->
                        prevTooltipRange?.let { prevTooltipRange ->
                            SpanText(abilityText.substring(prevTooltipRange.range.last + 1, tooltipRange.range.first))
                        }
                        SpanText(abilityText.substring(tooltipRange.range), UnderlineModifier)
                        if (tooltipRange.tooltip is TooltipData.OfText) {
                            Tooltip(ElementTarget.PreviousSibling, tooltipRange.tooltip.text)
                        } else {
                            Popup(ElementTarget.PreviousSibling, placement = PopupPlacement.Right) {
                                val outlineModifier = Modifier.outline(2.px, LineStyle.Solid, Colors.Black)
                                when (val tooltip = tooltipRange.tooltip) {
                                    is TooltipData.OfBlueprint -> Card(
                                        describer,
                                        tooltipParser,
                                        tooltip.blueprint.toCardSpec(),
                                        modifier = outlineModifier
                                    )

                                    is TooltipData.OfCard -> Card(
                                        describer,
                                        tooltipParser,
                                        tooltip.card.toCardSpec(),
                                        modifier = outlineModifier
                                    )
                                    is TooltipData.OfText -> error("Should have been handled above")
                                }
                            }
                        }
                        prevTooltipRange = tooltipRange
                    }
                    SpanText(abilityText.substring(tooltipRanges.last().range.last + 1))
                }
            }
        }
    }
}

@Composable
fun CardPlaceholder(modifier: Modifier = Modifier) {
    Box(CardStyle.toModifier(CardBackVariant).tabIndex(0).then(modifier))
}