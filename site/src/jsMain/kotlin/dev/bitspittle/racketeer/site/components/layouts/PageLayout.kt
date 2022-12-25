package dev.bitspittle.racketeer.site.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.AppGlobals
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.sections.Footer
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun PageLayout(title: String, content: @Composable () -> Unit) {
    LaunchedEffect(title) {
        document.title = title
    }

    Box(
        Modifier
            .fillMaxWidth()
            .minHeight(100.percent)
            // Create a box with two rows: the main content (fills as much space as it can) and the footer (which reserves
            // space at the bottom). "auto" means the use the height of the row. "1fr" means give the rest of the space to
            // that row. Since this box is set to *at least* 100%, the footer will always appear at least on the bottom but
            // can be pushed further down if the first row grows beyond the page.
            .gridTemplateRows("1fr auto")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(Modifier.align(Alignment.Center).gridRowStart(2).gridRowEnd(3))
    }

    Div(Modifier.position(Position.Fixed).bottom(10.px).right(10.px).fontSize(G.Font.Sizes.ExtraSmall).fontWeight(
        FontWeight.Bold).toAttrs()) {
        Text("v" + AppGlobals["version"] as String)
    }
}