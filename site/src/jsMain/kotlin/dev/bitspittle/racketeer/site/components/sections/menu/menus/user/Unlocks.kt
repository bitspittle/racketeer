package dev.bitspittle.racketeer.site.components.sections.menu.menus.user

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.thenUnless
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.common.DisabledStyle
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.model.user.totalVp
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

class UnlocksMenu(private val params: PopupParams) : Menu {
    private val totalVp = params.userStats.games.totalVp

    override val title = "Unlocks"

    override val topRow: @Composable RowScope.() -> Unit = {
        SpanText("You have so far earned ${params.describer.describeVictoryPoints(totalVp)} across all your games.", Modifier.margin(topBottom = 10.px))
    }

    @Composable
    override fun renderContent(actions: MenuActions) = with(params) {
        data.unlocks.forEach { unlock ->
            Row(ReadOnlyStyle.toModifier()
                .gap(5.px)
                .fillMaxWidth()
                .thenIf(totalVp < unlock.vp, DisabledStyle.toModifier())
            ) {
                SpanText(unlock.resolvedName(data))
                SpanText(params.describer.describeVictoryPoints(unlock.vp), Modifier.textAlign(TextAlign.End).flexGrow(1))
            }
            Tooltip(ElementTarget.PreviousSibling, unlock.resolvedDescription(data))
        }
    }
}
