package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.game.GameData
import org.jetbrains.compose.web.css.*

enum class NewIndicatorTarget {
    CARD,
    BLUEPRINT
}

@Composable
fun NewIndicator(data: GameData, target: NewIndicatorTarget, modifier: Modifier = Modifier, placement: PopupPlacement = PopupPlacement.Bottom) {
    val message = when (target) {
        NewIndicatorTarget.CARD -> "You have never owned this card before."
        NewIndicatorTarget.BLUEPRINT -> "You have never built this building before."
    }

    SpanText(data.icons.new, modifier)
    Tooltip(ElementTarget.PreviousSibling, message, Modifier.maxWidth(150.px), placement = placement)
}