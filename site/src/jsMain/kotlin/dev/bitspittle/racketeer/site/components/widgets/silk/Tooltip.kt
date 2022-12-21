package dev.bitspittle.racketeer.site.components.widgets.silk

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.toSilkPalette
import org.jetbrains.compose.web.css.*

val TooltipStyle = ComponentStyle.base("silk-tooltip") {
    val palette = colorMode.toSilkPalette()

    Modifier
        .backgroundColor(palette.color)
        .color(palette.background)
        .borderRadius(6.px)
}

val TooltipArrowStyle = ComponentStyle.base("silk-tooltip-arrow") {
    Modifier
        .position(Position.Absolute)
        .borderWidth(5.px)
        .borderStyle(LineStyle.Solid)
}

val BottomTooltipArrowVariant = TooltipArrowStyle.addVariantBase("bottom") {
    val palette = colorMode.toSilkPalette()

    Modifier
        .margin(left = (-5).px, bottom = (-10).px)
        .left(50.percent)
        .bottom(0.px)
        .styleModifier {
            property("border-color", "${palette.color} transparent transparent transparent")
        }
}

val LeftTooltipArrowVariant = TooltipArrowStyle.addVariantBase("left") {
    val palette = colorMode.toSilkPalette()

    Modifier
        .margin(top = (-5).px, left = (-10).px)
        .left(0.px)
        .top(50.percent)
        .styleModifier {
            property("border-color", "transparent ${palette.color} transparent transparent")
        }
}

val TopTooltipArrowVariant = TooltipArrowStyle.addVariantBase("up") {
    val palette = colorMode.toSilkPalette()

    Modifier
        .margin(left = (-5).px, top = (-10).px)
        .left(50.percent)
        .top(0.px)
        .styleModifier {
            property("border-color", "transparent transparent ${palette.color} transparent")
        }
}

val RightTooltipArrowVariant = TooltipArrowStyle.addVariantBase("right") {
    val palette = colorMode.toSilkPalette()

    Modifier
        .margin(top = (-5).px, right = (-10).px)
        .right(0.px)
        .top(50.percent)
        .styleModifier {
            property("border-color", "transparent transparent transparent ${palette.color}")
        }
}

@Composable
fun Tooltip(
    target: ElementTarget,
    modifier: Modifier = Modifier,
    placement: Placement = Placement.Bottom,
    hasArrow: Boolean = true,
    variant: ComponentVariant? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Popup(target, Modifier, placement, null) {
        Box(
            TooltipStyle.toModifier(variant).then(modifier),
        ) {
            content()
            if (hasArrow) {
                Box(
                    // e.g. if tooltip is below the target, arrow points up
                    TooltipArrowStyle.toModifier(
                        when (placement) {
                            Placement.Top -> BottomTooltipArrowVariant
                            Placement.Left -> RightTooltipArrowVariant
                            Placement.Right -> LeftTooltipArrowVariant
                            Placement.Bottom -> TopTooltipArrowVariant
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun Tooltip(
    target: ElementTarget,
    text: String,
    modifier: Modifier = Modifier,
    placement: Placement = Placement.Bottom,
    hasArrow: Boolean = true,
    variant: ComponentVariant? = null,
) {
    Tooltip(target, modifier, placement, hasArrow, variant) {
        SpanText(text, Modifier.padding(5.px))
    }
}
