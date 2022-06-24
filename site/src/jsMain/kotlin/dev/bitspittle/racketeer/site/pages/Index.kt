package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.widgets.Card
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun HomePage() {
    PageLayout("Do Crimes") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card()
        }
    }
}