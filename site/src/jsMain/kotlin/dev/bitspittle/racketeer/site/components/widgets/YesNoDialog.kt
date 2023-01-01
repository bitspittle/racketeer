package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
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
            ) {
                Text(noText)
            }

            Button(
                onClick = { response(YesNo.YES) },
            ) {
                Text(yesText)
            }
        },
        ref = inputRef { code ->
            if (code == "Escape") {
                response(YesNo.NO)
                true
            } else false
        },
        content = question?.let { question ->
            {
                Column {
                    // Need to add a space or else separating lines (e.g. from "Line1\n\nLine2") is squashed.
                    question.split("\n").forEach { line -> SpanText(line.takeIf { it.isNotEmpty() } ?: " ") }
                }
            }
        }
    )
}

