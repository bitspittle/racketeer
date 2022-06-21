package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        Text("TODO: Content")
    }
}