package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.toSilkPalette
import org.jetbrains.compose.web.css.*
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement

interface TooltipTarget {
    operator fun invoke(tooltipElement: HTMLElement): HTMLElement?

    object PreviousSibling : TooltipTarget {
        override fun invoke(tooltipElement: HTMLElement) = tooltipElement.previousElementSibling as? HTMLElement
    }
}

enum class TooltipPosition {
    Top,
    Left,
    Right,
    Bottom,
}

val TooltipStyle = ComponentStyle.base("silk-tooltip") {
    val palette = colorMode.toSilkPalette()

    Modifier
        .position(Position.Absolute)
        .backgroundColor(palette.color)
        .color(palette.background)
        .borderRadius(6.px)
        .zIndex(2) // TODO: Enumerate Z-indexes
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

@Suppress("NAME_SHADOWING")
@Composable
fun Tooltip(
    target: TooltipTarget,
    modifier: Modifier = Modifier,
    position: TooltipPosition = TooltipPosition.Bottom,
    hasArrow: Boolean = true,
    variant: ComponentVariant? = null,
    content: @Composable () -> Unit,
) {
    var targetBounds by remember { mutableStateOf<DOMRect?>(null) }
    Box(Modifier.display(DisplayStyle.None),
        ref = ref { element ->
            target(tooltipElement = element)?.apply {
                onmouseenter = { targetBounds = this.getBoundingClientRect(); Unit }
                onmouseleave = { targetBounds = null; Unit }
            }
        }
    )

    targetBounds?.let { targetBounds ->
        var tooltipBounds by remember { mutableStateOf<DOMRect?>(null) }
        val absPosModifier = tooltipBounds?.let { tooltipBounds ->
            when (position) {
                TooltipPosition.Top -> {
                    println("${targetBounds.top} -> ${targetBounds.bottom}")
                    Modifier
                        .left((targetBounds.left - (tooltipBounds.width - targetBounds.width) / 2).px)
                        .top((targetBounds.top - 10 - tooltipBounds.height).px)
                }
                TooltipPosition.Bottom -> {
                    Modifier
                        .left((targetBounds.left - (tooltipBounds.width - targetBounds.width) / 2).px)
                        .top((targetBounds.bottom + 10).px)
                }
                TooltipPosition.Left -> {
                    Modifier
                        .top((targetBounds.top - (tooltipBounds.height - targetBounds.height) / 2).px)
                        .left((targetBounds.left - 10 - tooltipBounds.width).px)
                }
                TooltipPosition.Right -> {
                    Modifier
                        .top((targetBounds.top - (tooltipBounds.height - targetBounds.height) / 2).px)
                        .left((targetBounds.right + 10).px)
                }
            }

        } ?: Modifier.opacity(0)
        Box(
            TooltipStyle.toModifier(variant).then(absPosModifier).then(modifier),
            ref = ref { element ->
                tooltipBounds = element.getBoundingClientRect()
            }
        ) {
            content()
            if (hasArrow) {
                Box(
                    // e.g. if tooltip is below the target, arrow points up
                    TooltipArrowStyle.toModifier(
                        when (position) {
                            TooltipPosition.Top -> BottomTooltipArrowVariant
                            TooltipPosition.Left -> RightTooltipArrowVariant
                            TooltipPosition.Right -> LeftTooltipArrowVariant
                            TooltipPosition.Bottom -> TopTooltipArrowVariant
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun Tooltip(
    target: TooltipTarget,
    text: String,
    modifier: Modifier = Modifier,
    position: TooltipPosition = TooltipPosition.Bottom,
    hasArrow: Boolean = true,
    variant: ComponentVariant? = null,
) {
    Tooltip(target, modifier, position, hasArrow, variant) {
        SpanText(text, Modifier.padding(5.px))
    }

}
