package dev.bitspittle.racketeer.site.components.sections.menu

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.components.forms.Button
import org.jetbrains.compose.web.dom.*

@Composable
fun MenuButton(actions: MenuActions, target: Menu) {
    Button(onClick = { actions.visit(target) }) { Text(target.title) }
}
