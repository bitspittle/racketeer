package dev.bitspittle.racketeer.site.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.PointerEvents
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.sections.Footer
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.model.*
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

val VersionStyle = ComponentStyle("version") {
    base {
        Modifier
            .position(Position.Absolute)
            .top(10.px)
            .right(10.px)
            .fontSize(G.Font.Sizes.ExtraSmall)
            .fontWeight(FontWeight.Bold)
            .opacity(0)
            .transitionProperty("opacity")
            .transitionDuration(100.ms)
    }

    hover {
        Modifier.opacity(1)
    }
}

class PageLayoutScope(val scope: CoroutineScope, val events: Events, val settings: Settings)

@Composable
fun PageLayout(title: String, content: @Composable PageLayoutScope.() -> Unit) {
    remember(title) {
        document.title = title
    }

    val scope = rememberCoroutineScope()
    val events = remember { MutableSharedFlow<Event>(replay = 0) }
    val settings = remember { Data.load(Data.Keys.Settings)?.value ?: Settings() }

    var showAdminDecoration by remember { mutableStateOf(settings.admin.enabled) }

    LaunchedEffect(Unit) {
        events.collect { evt ->
            when (evt) {
                is Event.SettingsChanged -> {
                    showAdminDecoration = evt.settings.admin.enabled
                    if (settings.isDefault) {
                        Data.delete(Data.Keys.Settings)
                    } else Data.save(Data.Keys.Settings, settings)
                }
                else -> {}
            }
        }
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
            PageLayoutScope(scope, events, settings).content()
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(Modifier.align(Alignment.Center).gridRowStart(2).gridRowEnd(3))
    }

    if (showAdminDecoration) {
        Box(
            Modifier.position(Position.Fixed).top(0.px).left(0.px).bottom(0.px).right(0.px)
                .pointerEvents(PointerEvents.None).thenIf(settings.admin.enabled) {
                Modifier.boxShadow(spreadRadius = 15.px, color = Colors.Pink, inset = true)
            })
    }

    Div(
        VersionStyle
            .toModifier()
            .onClick { evt ->
                if (evt.ctrlKey && evt.altKey && evt.shiftKey) {
                    settings.admin.enabled = !settings.admin.enabled
                    events.emitAsync(scope, Event.SettingsChanged(settings))
                }
            }.toAttrs()
    ){
        Text("v" + AppGlobals["version"] as String)
    }
}