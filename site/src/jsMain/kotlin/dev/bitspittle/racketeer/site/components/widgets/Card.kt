package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.common.ariaDisabled
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.util.UnderlineModifier
import dev.bitspittle.racketeer.site.components.util.renderTextWithTooltips
import dev.bitspittle.racketeer.site.model.TooltipParser
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val CardStyleCommon =
    Modifier
        .fontSize(G.Font.Sizes.Normal)
        .padding(topBottom = 5.px)
        .borderRadius(5.percent)
        .backgroundColor(G.Colors.Card.Front)
        .color(Colors.Black)
        .transitionProperty("translate")
        .transitionDuration(100.ms)

val CardStyle = ComponentStyle("card") {
    base {
        CardStyleCommon
            .width(G.Sizes.Card.w).flexShrink(0) // Needed to prevent Row from resizing the elements
            .height(G.Sizes.Card.h)
    }

    (hover + not(ariaDisabled)) {
        Modifier.translateY((-10).px)
    }

    hover {
        Modifier.cursor(Cursor.Pointer)
    }
}

val CardBackVariant = CardStyle.addVariantBase("back") {
    Modifier
        .backgroundColor(G.Colors.Card.Back)
}

val CardTitleStyle = ComponentStyle.base("card-title") {
    Modifier
        .textAlign(TextAlign.Center)
        .fontSize(G.Font.Sizes.Normal)
        .fontWeight(FontWeight.Bold)
}

val CardDescriptionStyle = ComponentStyle.base("card-desc") {
    Modifier
        .fontSize(G.Font.Sizes.Small)
}

val CardDescriptionTypesVariant = CardDescriptionStyle.addVariantBase("types") {
    Modifier.fontSize(G.Font.Sizes.ExtraSmall)
}

val CardDescriptionFlavorVariant = CardDescriptionStyle.addVariantBase("flavor") {
    Modifier.fontStyle(FontStyle.Italic)
}

val CardDescriptionUpgradesVariant = CardDescriptionStyle.addVariantBase("upgrades") {
    UnderlineModifier
        .fontStyle(FontStyle.Italic)

}

val CardDescriptionEffectsVariant = CardDescriptionStyle.addVariantBase("effects") {
    Modifier.margin(top = 5.px)
}

interface CardSpec {
    val enabled: Boolean
    val colorOverride: Color?
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
    val activationCost: String?
    val label: String?
}

fun Card.toCardSpec(label: String? = null, enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val enabled = enabled
        override val colorOverride: Color? = null
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
        override val activationCost = null
        override val label = label
    }
}

fun CardTemplate.toCardSpec(enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val enabled = enabled
        override val colorOverride: Color? = null
        override val title = self.name
        override val types = self.types
        override val tier = self.tier
        override val rarity = self.rarity
        override val vpBase = self.vp
        override val vpTotal = vpBase
        override val counter = 0
        override val flavor = self.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val traits = emptySet<TraitType>()
        override val ability = self.description.ability
        override val activationCost = null
        override val label = null
    }
}

fun Iterable<Card>.toCardSpec(): CardSpec {
    val card = this.first().template
    return object : CardSpec {
        override val enabled = true
        override val colorOverride = null
        override val title = card.name
        override val types = card.types
        override val tier = card.tier
        override val rarity = card.rarity
        override val vpBase = card.vp
        override val vpTotal = vpBase
        override val counter = 0
        override val flavor = card.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val traits = card.traitTypes
        override val ability = card.description.ability
        override val activationCost = null
        override val label = null
    }
}

@Composable
fun Card(describer: Describer, tooltipParser: TooltipParser, card: Card, onClick: () -> Unit = {}, modifier: Modifier = Modifier, label: String? = null, enabled: Boolean = true) {
    Card(describer, tooltipParser, card.toCardSpec(label, enabled), onClick, modifier)
}

@Composable
fun Card(describer: Describer, tooltipParser: TooltipParser, cards: List<Card>, modifier: Modifier = Modifier) {
    Card(describer, tooltipParser, cards.toCardSpec(), modifier = modifier)
}

@Composable
private fun LabeledContent(label: String? = null, enabled: Boolean = true, content: @Composable () -> Unit) {
    Column(Modifier.thenUnless(enabled) { Modifier.opacity(G.Colors.DisabledOpacity) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
        content()
        if (label != null) {
            SpanText(label, Modifier.margin(top = 10.px))
        }
    }
}


@Composable
fun Card(describer: Describer, tooltipParser: TooltipParser, card: CardSpec, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    LabeledContent(card.label, enabled = card.enabled) {
        Column(CardStyle
            .toModifier()
            .thenIf(card.enabled) {
                Modifier.tabIndex(0).onClick { onClick() }
            }
            .thenIf(!card.enabled) {
                Modifier.ariaDisabled()
            }
            .thenIf(card.colorOverride != null) {
                Modifier.backgroundColor(card.colorOverride!!)
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
                    SpanText(
                        describer.describeTypes(card.types),
                        CardDescriptionStyle.toModifier(CardDescriptionTypesVariant)
                    )
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
                Row(Modifier.rowGap(2.px).columnGap(5.px).flexWrap(FlexWrap.Wrap)) {
                    card.upgrades.forEach { upgrade ->
                        SpanText(
                            describer.describeUpgradeTitle(upgrade, icons = false),
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
                card.activationCost?.let { activationCost ->
                    SpanText("Activation cost: $activationCost", CardDescriptionStyle.toModifier())
                }
                Span(CardDescriptionStyle.toModifier(CardDescriptionEffectsVariant).toAttrs()) {
                    renderTextWithTooltips(describer, tooltipParser, describer.convertIcons(card.ability))
                }
            }
        }
   }
}

@Composable
fun CardPlaceholder(modifier: Modifier = Modifier, enabled: Boolean = true, label: String? = null) {
    LabeledContent(label, enabled) {
        Box(CardStyle.toModifier(CardBackVariant)
            .thenIf(enabled) { Modifier.tabIndex(0) }
            .thenIf(!enabled) { Modifier.ariaDisabled() }
                .then(modifier)
        )
    }
}