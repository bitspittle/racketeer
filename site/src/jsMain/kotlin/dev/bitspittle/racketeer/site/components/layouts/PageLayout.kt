package dev.bitspittle.racketeer.site.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.PointerEvents
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.FaCopy
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.firebase.analytics.Analytics
import dev.bitspittle.firebase.app.FirebaseApp
import dev.bitspittle.firebase.app.FirebaseOptions
import dev.bitspittle.firebase.auth.Auth
import dev.bitspittle.firebase.database.Database
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.sections.Footer
import dev.bitspittle.racketeer.site.model.Event
import dev.bitspittle.racketeer.site.model.Events
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.web.css.*

val VersionStyle = ComponentStyle("version") {
    base {
        Modifier
            .position(Position.Absolute)
            .top(10.px)
            .right(10.px)
            .gap(5.px)
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

class PageLayoutScope(val firebase: FirebaseData, val scope: CoroutineScope, val events: Events)

class FirebaseData(val analytics: Analytics, val auth: Auth, val db: Database)

@Composable
fun PageLayout(content: @Composable PageLayoutScope.() -> Unit) {
    val firebase = remember {
        val app = FirebaseApp.initialize(
            FirebaseOptions(
                apiKey = "AIzaSyDr4Rq67D5bB4ZDCzHbqXxHWtDUjeOyOD4",
                authDomain = "cardgame-racketeer.firebaseapp.com",
                databaseURL = "https://cardgame-racketeer-default-rtdb.firebaseio.com",
                projectId = "cardgame-racketeer",
                storageBucket = "cardgame-racketeer.appspot.com",
                messagingSenderId = "719861613042",
                appId = "1:719861613042:web:5d27346021b9a4b1f5eb6b",
                measurementId = "G-MY7ZNH5N22"
            )
        )

        FirebaseData(
            analytics = app.getAnalytics(),
            auth = app.getAuth().apply { useDeviceLanguage() },
            db = app.getDatabase()
        )
    }

    val scope = rememberCoroutineScope()
    val events = remember { MutableSharedFlow<Event>(replay = 0) }

    var isAdmin: Boolean? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        events.collect { evt ->
            when (evt) {
                is Event.AccountChanged -> {
                    val account = evt.account
                    isAdmin = account?.isAdmin ?: false
                }
                else -> {}
            }
        }
    }

    val pageContext = rememberPageContext()
    LaunchedEffect(isAdmin, pageContext) {
        if (isAdmin == false) {
            firebase.analytics.log(Analytics.Event.PageView())
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
            PageLayoutScope(firebase, scope, events).content()
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(Modifier.align(Alignment.Center).gridRowStart(2).gridRowEnd(3))
    }

    if (isAdmin == true) {
        Box(
            Modifier.position(Position.Fixed).top(0.px).left(0.px).bottom(0.px).right(0.px)
                .pointerEvents(PointerEvents.None)
                .boxShadow(spreadRadius = 15.px, color = Colors.Pink, inset = true)
        )
    }

    Row(
        VersionStyle.toModifier(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val versionStr = "v${G.version}"
        SpanText(versionStr)
        FaCopy(
            Modifier
                .cursor(Cursor.Pointer)
                .onClick {
                    window.navigator.clipboard.writeText(versionStr)
                },
            size = IconSize.SM
        )
    }
}