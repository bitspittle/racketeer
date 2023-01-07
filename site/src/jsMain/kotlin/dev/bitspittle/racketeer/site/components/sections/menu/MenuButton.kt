package dev.bitspittle.racketeer.site.components.sections.menu

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.components.forms.Button
import org.jetbrains.compose.web.dom.*

@Composable
fun MenuButton(actions: MenuActions, target: Menu, enabled: Boolean = true) {
    Button(onClick = { actions.visit(target) }, enabled = enabled) { Text(target.title) }
}
