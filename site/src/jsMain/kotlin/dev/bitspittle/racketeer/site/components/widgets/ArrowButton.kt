package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val ArrowButtonModifier = Modifier.padding(topBottom = 1.px, leftRight = 4.px)

enum class ArrowDirection {
    LEFT,
    RIGHT
}

@Composable
fun ArrowButton(dir: ArrowDirection, onClick: () -> Unit) {
    Button(onClick = onClick, ArrowButtonModifier) {
        Text(
            when (dir) {
                ArrowDirection.LEFT -> "<"
                ArrowDirection.RIGHT -> ">"
            }
        )
    }
}
