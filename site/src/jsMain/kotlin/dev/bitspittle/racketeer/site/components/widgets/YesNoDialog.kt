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

enum class YesNo {
    YES,
    NO
}


@Composable
fun YesNoDialog(
    title: String,
    question: String? = null,
    noText: String = "No",
    yesText: String = "Yes",
    response: (YesNo) -> Unit,
) {
    Modal(
        overlayModifier = Modifier.onClick { response(YesNo.NO) },
        dialogModifier = Modifier.margin(top = 8.vh).onClick { evt -> evt.stopPropagation() },
        title = title,
        bottomRow = {
            Button(
                onClick = { response(YesNo.NO) },
                ref = ref { it.focus() }
            ) {
                Text(noText)
            }

            Button(
                onClick = { response(YesNo.YES) },
            ) {
                Text(yesText)
            }
        },
        ref = inputRef {
            if (code == "Escape") {
                response(YesNo.NO)
                true
            } else false
        },
        content = @Suppress("NAME_SHADOWING") question?.let { question ->
            {
                Column {
                    question.split("\n").forEach { line -> if (line.isNotEmpty()) SpanText(line) else Br() }
                }
            }
        }
    )
}

