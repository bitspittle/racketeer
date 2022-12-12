package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.CardGroup
import org.jetbrains.compose.web.css.*

@Page
@Composable
fun HomePage() {
    PageLayout("Do Crimes") {
        // Temporary hack for testing card movement. Will be improved in a followup commit.
        var onStreet by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(Modifier.gap(20.px)) {
                CardGroup("The Street") {
                    if (onStreet) {
                        Card(onClick = { onStreet = false })
                    }
                }
                CardGroup("Your Hand") {
                    if (!onStreet) {
                        Card(onClick = { onStreet = true })
                    }
                }
            }
        }
    }
}