package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.dom.ElementRefScope
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.toSilkPalette
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.util.renderTextWithTooltips
import dev.bitspittle.racketeer.site.model.TooltipParser
import dev.bitspittle.racketeer.site.model.user.UserStats
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

val ModalStyle = ComponentStyle.base("modal") {
    Modifier
        .minWidth(300.px)
        .maxWidth(500.px)
        .backgroundColor(colorMode.toSilkPalette().background)
        .margin(top = 6.vh)
        .padding(20.px)
        .gap(10.px)
        .borderRadius(2.percent)
}

val ModalContentColumnStyle = ComponentStyle("modal-content-col") {
    base {
        Modifier
            .fillMaxWidth()
            .gap(10.px)
            .padding(5.px) // Avoid outlines clipping against the side / add space between buttons and scrollbar
            .maxHeight(60.vh)
            .overflowY(Overflow.Auto)
    }
}

val ModalTitleStyle = ComponentStyle.base("modal-title") {
    Modifier
        .fillMaxWidth()
        .fontSize(G.Font.Sizes.Normal)
        .fontWeight(FontWeight.Bold)
}

val ModalTopRowStyle = ComponentStyle.base("modal-title-row") {
    Modifier.margin(bottom = 30.px)
}

val ModalButtonRowStyle = ComponentStyle("modal-button-row") {
    base {
        Modifier.fillMaxWidth().margin(top = 20.px).gap(10.px)
    }

    cssRule(" *") {
        Modifier.flexGrow(1)
    }
}

@Composable
fun Modal(
    overlayModifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    ref: ElementRefScope<HTMLElement>? = null,
    title: String? = null,
    bottomRow: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Modal(
        overlayModifier,
        dialogModifier,
        ref,
        titleRow = @Suppress("NAME_SHADOWING") title?.let { title -> {
            Spacer()
            SpanText(title)
            Spacer()
        }},
        topRow = null,
        bottomRow = bottomRow,
        content = content
    )
}



@Composable
fun Modal(
    data: GameData,
    userStats: UserStats,
    describer: Describer,
    tooltipParser: TooltipParser,
    overlayModifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    ref: ElementRefScope<HTMLElement>? = null,
    title: String? = null,
    bottomRow: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Modal(
        overlayModifier,
        dialogModifier,
        ref,
        titleRow = @Suppress("NAME_SHADOWING") title?.let { title -> {
            Spacer()
            renderTextWithTooltips(data, userStats, describer, tooltipParser, title)
            Spacer()
        }},
        topRow = null,
        bottomRow = bottomRow,
        content = content
    )
}

@Composable
fun Modal(
    overlayModifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    ref: ElementRefScope<HTMLElement>? = null,
    titleRow: (@Composable RowScope.() -> Unit)? = null,
    topRow: (@Composable RowScope.() -> Unit)? = null,
    bottomRow: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Overlay(overlayModifier) {
        Column(ModalStyle.toModifier().then(dialogModifier), ref = ref) {
            if (titleRow != null) {
                Row(ModalTitleStyle.toModifier()) { titleRow() }
            }

            if (topRow != null) {
                Row(Modifier.fillMaxWidth()) { topRow() }
            }
            content?.let { content ->
                Column(listOf(ModalContentColumnStyle, FullWidthChildrenStyle).toModifier()) {
                    content()
                }
            }
            if (bottomRow != null) {
                Row(ModalButtonRowStyle.toModifier()) {
                    bottomRow()
                }
            }
        }
    }
}
