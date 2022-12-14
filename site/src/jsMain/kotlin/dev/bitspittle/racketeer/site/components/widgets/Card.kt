package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.model.GameContext
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

val CardDescriptionStyle = ComponentStyle.base("card-desc") {
    Modifier
        .fontSize(G.Font.Sizes.Small)
        .padding(topBottom = 10.px, leftRight = 15.px)
}

val CardDescriptionFlavorVariant = CardDescriptionStyle.addVariantBase("flavor") {
    Modifier.fontStyle(FontStyle.Italic)
}

val CardDescriptionAbilityVariant = CardDescriptionStyle.addVariantBase("ability") {
    Modifier.styleModifier {
        property("text-shadow", "0px 0px 4px #000000")
    }
}

@Composable
fun Card(ctx: GameContext, card: Card, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Column(CardStyle
        .toModifier(DisabledCardVariant.takeUnless { enabled })
        .thenIf(enabled) {
            Modifier.tabIndex(0).onClick { onClick() }
        }
        .then(modifier)
    ) {
        Box(Modifier.fillMaxWidth().height(33.px), contentAlignment = Alignment.Center) {
            SpanText(card.template.name)
        }
        Spacer()
        card.template.description.flavor?.let { flavor ->
            SpanText(flavor, CardDescriptionStyle.toModifier(CardDescriptionFlavorVariant))
        }
        SpanText(
            ctx.describer.convertIcons(card.template.description.ability),
            CardDescriptionStyle.toModifier(CardDescriptionAbilityVariant)
        )
    }
}

@Composable
fun CardPlaceholder(modifier: Modifier = Modifier) {
    Box(CardStyle.toModifier(CardBackVariant).tabIndex(0).then(modifier))
}