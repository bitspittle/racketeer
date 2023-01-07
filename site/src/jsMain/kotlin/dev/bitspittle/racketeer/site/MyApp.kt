package dev.bitspittle.racketeer.site

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.dom.disposableRef
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.InitSilk
import com.varabyte.kobweb.silk.InitSilkContext
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import com.varabyte.kobweb.silk.theme.registerBaseStyle
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.w3c.dom.HTMLElement

private const val COLOR_MODE_KEY = "site:colorMode"

@InitSilk
fun updateTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.LIGHT

    ctx.config.registerBaseStyle("@font-face") {
        Modifier
            .fontFamily("GameText")
            .styleModifier {
                property("src", "url(fonts/CaslonAntiqueBold.ttf")
            }
    }

    ctx.config.registerBaseStyle("body") {
        Modifier.fontFamily(
            "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Oxygen", "Ubuntu",
            "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", "sans-serif"
        )
    }

    ctx.config.registerBaseStyle("#root") {
        // UserSelect.None everywhere by default, because the game feels cheap if you allow users to drag highlight
        // text on stuff
        Modifier.userSelect(UserSelect.None)
    }
}

class KeyScope(val code: String, val key: String, val isShift: Boolean, val isAlt: Boolean, val isCtrl: Boolean)

// I was having trouble getting event bubbling to work, e.g. a dialog should have been catching input before the
// document body did. So for now I am just going to create a global input fallback handler on document.body and manage
// the input events myself.
//
// Handlers take a key code (e.g. "KeyA", "Escape") and should return true if they handled it or false otherwise.
// Handling a key prevents other handlers from getting a chance.
private val InputHandlers: MutableMap<HTMLElement, KeyScope.() -> Boolean> = mutableMapOf()

// Pass the return value of this into the `ref` parameter of any Silk widgest you want to add global input handling for.
fun inputRef(handler: KeyScope.() -> Boolean) = disposableRef<HTMLElement> { element ->
    InputHandlers[element] = handler
    onDispose {
        InputHandlers.remove(element)
    }
}

@App
@Composable
fun MyApp(content: @Composable () -> Unit) {
    remember {
        window.document.body!!.onkeydown = { evt ->
            // Last in first out -- let the elements we registered last have a first crack at the input. This usually
            // means children items because they were composed after their parents.
            InputHandlers.values.toList().lastOrNull { it.invoke(KeyScope(evt.code, evt.key, evt.shiftKey, evt.altKey, evt.ctrlKey)) }
        }
    }

    SilkApp {
        val colorMode = getColorMode()
        LaunchedEffect(colorMode) {
            localStorage.setItem(COLOR_MODE_KEY, colorMode.name)
        }

        Surface(Modifier.minHeight(100.vh)) {
            content()
        }
    }
}
