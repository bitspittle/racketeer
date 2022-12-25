package dev.bitspittle.racketeer.site.components.util

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.overlay.Popup
import com.varabyte.kobweb.silk.components.overlay.PopupPlacement
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.toCardSpec
import dev.bitspittle.racketeer.site.model.TooltipParser
import org.jetbrains.compose.web.css.*

@Composable
fun installPopup(describer: Describer, tooltipParser: TooltipParser, item: Any) {
    @Composable
    fun RightPopup(content: @Composable BoxScope.() -> Unit) {
        Popup(
            ElementTarget.PreviousSibling, placement = PopupPlacement.Right, content = content,
            modifier = Modifier
                .backgroundColor(Colors.WhiteSmoke)
                .padding(5.px)
                .borderRadius(5.px)
                .outline(1.px, LineStyle.Solid, Colors.Black)
        )
    }

    when (item) {
        is Blueprint -> RightPopup { Card(describer, tooltipParser, item.toCardSpec(describer)) }
        is Building -> RightPopup { Card(describer, tooltipParser, item.toCardSpec(describer)) }
        is Card -> RightPopup { Card(describer, tooltipParser, item.toCardSpec()) }
        is CardTemplate -> RightPopup { Card(describer, tooltipParser, item.toCardSpec()) }
        is FormattedItem -> installPopup(describer, tooltipParser, item.wrapped)
    }
}
