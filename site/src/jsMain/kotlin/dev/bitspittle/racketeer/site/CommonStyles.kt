package dev.bitspittle.racketeer.site

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*

val FullWidthChildrenStyle = ComponentStyle("full-width-children") {
    cssRule(" > *") {
        Modifier.fillMaxWidth()
    }
}

val FullWidthChildrenRecursiveStyle = ComponentStyle("full-width-children-recursive") {
    cssRule(" *") {
        Modifier.fillMaxWidth()
    }
}
