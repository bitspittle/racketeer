package dev.bitspittle.racketeer.site.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.site.components.layouts.PageLayout
import dev.bitspittle.racketeer.site.components.sections.GameBoard
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.createNewGame
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun HomePage() {
    PageLayout("Do Crimes") {
        val scope = rememberCoroutineScope()
        var ctx by remember { mutableStateOf<GameContext?>(null) }
        var forceRecomposition by remember { mutableStateOf(0) }

        @Suppress("NAME_SHADOWING")
        ctx?.let { ctx ->
            key(forceRecomposition) {
                GameBoard(scope, ctx) { ++forceRecomposition }
            }
        } ?: run {
            Text("Please wait, loading...")
            window.fetch("gamedata.yaml").then { response ->
                response.text().then { responseText ->
                    val gameData = GameData.decodeFromString(responseText)
                    scope.launch {
                        ctx = createNewGame(gameData)
                    }
                }
            }
        }
    }
}
