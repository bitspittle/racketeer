package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.inputRef
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun OkDialog(
    title: String,
    info: String? = null,
    okText: String = "Ok",
    onClose: () -> Unit,
) {
    Modal(
        overlayModifier = Modifier.onClick { onClose() },
        dialogModifier = Modifier.margin(top = 8.vh).onClick { evt -> evt.stopPropagation() },
        title = title,
        bottomRow = {
            Button(
                onClick = { onClose() },
                ref = ref { it.focus() }
            ) {
                Text(okText)
            }
        },
        ref = inputRef {
            if (code == "Escape") {
                onClose()
                true
            } else false
        },
        content = @Suppress("NAME_SHADOWING") info?.let { info ->
            {
                Column {
                    info.split("\n").forEach { line -> if (line.isNotEmpty()) SpanText(line) else Br() }
                }
            }
        }
    )
}

