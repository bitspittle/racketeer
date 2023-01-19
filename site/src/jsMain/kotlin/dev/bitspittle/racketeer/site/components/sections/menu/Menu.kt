package dev.bitspittle.racketeer.site.components.sections.menu

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import dev.bitspittle.racketeer.site.KeyScope
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.inputRef
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

interface MenuActions {
    fun visit(other: Menu)
    fun close()
}

interface Menu {
    val title: String

    fun KeyScope.handleKey(): Boolean = false

    val topRow: (@Composable RowScope.() -> Unit)? get() = null

    @Composable
    fun renderContent(actions: MenuActions)

    @Composable
    open fun RowScope.renderExtraButtons(actions: MenuActions) {}
}

fun Menu.handleKey(keyScope: KeyScope): Boolean = keyScope.handleKey()

@Composable
fun Menu.renderExtraButtons(rowScope: RowScope, actions: MenuActions) = rowScope.renderExtraButtons(actions)

@Composable
fun Menu(
    closeRequested: () -> Unit,
    initialMenu: Menu,
) {
    val menuStack = remember { mutableStateListOf(initialMenu) }

    fun goBack() {
        if (menuStack.size >= 2) {
            menuStack.removeLast()
        } else closeRequested()
    }

    val menuActions = object : MenuActions {
        override fun visit(other: Menu) {
            menuStack.add(other)
        }

        override fun close() {
            closeRequested()
        }
    }

    Modal(
        // A reasonable min width that can grow if necessary but prevents menu sizes jumping around otherwise
        overlayModifier = Modifier.onClick { closeRequested() },
        dialogModifier = Modifier.minWidth(400.px).onClick { evt -> evt.stopPropagation() },
        ref = inputRef {
            if (!menuStack.last().handleKey(this)) {
                if (code == "Escape") goBack()
            }
            true // Prevent keys from inadvertently affected game behind the modal
        },
        titleRow = {
            Spacer(); Text(menuStack.joinToString(" > ") { it.title }); Spacer()
        },
        topRow = menuStack.last().topRow,
        bottomRow = {
            Button(onClick = { goBack() }) {
                Text(if (menuStack.size >= 2) "Go Back" else "Close")
            }

            menuStack.last().renderExtraButtons(this, menuActions)
        },
    ) {
        menuStack.last().renderContent(menuActions)
    }

}
