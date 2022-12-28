package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.model.ChoiceContext
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.createNewGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun TitleScreen(scope: CoroutineScope, gameData: GameData, handleChoice: (ChoiceContext) -> Unit, gameRequested: (GameContext) -> Unit) {
    Box(Modifier.fillMaxSize().padding(5.percent), contentAlignment = Alignment.TopCenter) {
        Column(FullWidthChildrenStyle.toModifier().gap(15.px)) {
            H1(Modifier.margin(bottom = 10.px).toAttrs()) { Text("Do Crimes") }
            Button(onClick = {
                scope.launch {
                    gameRequested(
                        createNewGame(gameData, handleChoice)
                    )
                }
            }) { Text("New Game") }
            // TODO: Button(onClick = {}) { Text("Restore Game") }
        }
    }
}
