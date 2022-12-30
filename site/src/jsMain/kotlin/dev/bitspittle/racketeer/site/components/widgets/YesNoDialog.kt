package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import org.jetbrains.compose.web.dom.*

enum class YesNo {
    YES,
    NO
}


@Composable
fun YesNoDialog(
    title: String,
    question: String,
    noText: String = "No",
    yesText: String = "Yes",
    response: (YesNo) -> Unit,
) {
    Modal(
        overlayModifier = Modifier.onClick { response(YesNo.NO) },
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
        }
    ) {
        Text(question)
    }
}

