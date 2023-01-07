package dev.bitspittle.racketeer.site.components.sections.menu.menus.user

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenUnless
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.common.DisabledStyle
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.ArrowButton
import dev.bitspittle.racketeer.site.components.widgets.ArrowDirection
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

class BuildingListMenu(private val params: PopupParams) : Menu {
    override val title = "Building List"

    override val topRow: @Composable RowScope.() -> Unit = {
        SpanText(
            "You have built ${params.userStats.buildings.size} out of ${params.data.blueprints.size} buildings.",
            Modifier.margin(topBottom = 10.px).fillMaxWidth().textAlign(TextAlign.Center)
        )
    }


    @Composable
    override fun renderContent(actions: MenuActions) = with(params) {
        data.blueprints.sortedBy { it.name }.forEach { blueprint ->
            val knownBlueprint = (userStats.buildings.contains(blueprint.name))

            Div(ReadOnlyStyle.toModifier().thenUnless(knownBlueprint, DisabledStyle.toModifier()).toAttrs()) {
                var text = describer.describeBlueprintTitle(blueprint)
                if (!knownBlueprint) {
                    text = "?".repeat(text.length)
                }

                Text(text)
            }
            if (knownBlueprint) {
                installPopup(params, blueprint)
            }
        }
    }
}
