package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.G
import org.jetbrains.compose.web.css.*

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
        .padding(leftRight = 15.px, topBottom = 3.px)
}

val CardDescriptionFlavorVariant = CardDescriptionStyle.addVariantBase("flavor") {
    Modifier.fontStyle(FontStyle.Italic)
}

val CardDescriptionUpgradesVariant = CardDescriptionStyle.addVariantBase("upgrades") {
    Modifier.fontStyle(FontStyle.Italic)
}

val CardDescriptionEffectsVariant = CardDescriptionStyle.addVariantBase("effects") {
    Modifier
        .overflowWrap(OverflowWrap.BreakWord) // Don't break between emojis
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
fun Card(describer: Describer, card: Card, onClick: () -> Unit = {}, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Card(describer, card.toCardSpec(enabled), onClick, modifier)
}

@Composable
fun Card(describer: Describer, card: CardSpec, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
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
        card.upgrades.forEach {  upgrade ->
            SpanText(
                describer.describeUpgradeBody(upgrade),
                CardDescriptionStyle.toModifier(CardDescriptionUpgradesVariant)
            )
        }
        card.traits.forEach { trait ->
            SpanText(
                describer.describeTraitBody(trait),
                CardDescriptionStyle.toModifier(CardDescriptionUpgradesVariant)
            )
        }
        SpanText(
            describer.convertIcons(card.ability),
            CardDescriptionStyle.toModifier(CardDescriptionEffectsVariant)
        )
    }
}

@Composable
fun CardPlaceholder(modifier: Modifier = Modifier) {
    Box(CardStyle.toModifier(CardBackVariant).tabIndex(0).then(modifier))
}