package dev.bitspittle.racketeer.site.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.menus.user.UserDataMenu
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.util.loadSnapshotFromDisk
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.model.*
import dev.bitspittle.racketeer.site.model.user.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
fun TitleLayout(
    data: GameData,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.fillMaxSize().padding(2.percent), contentAlignment = Alignment.TopCenter) {
        Column(FullWidthChildrenStyle.toModifier().gap(15.px).maxWidth(G.Sizes.MenuWidth)) {
            H1(Modifier.margin(bottom = 10.px).textAlign(TextAlign.Center).toAttrs()) { Text(data.title) }
            content()
        }
    }
}
