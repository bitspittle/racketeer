package dev.bitspittle.racketeer.site.components.widgets.silk

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import org.w3c.dom.NodeFilter

/** An interface for finding some target element, relative to some given initial element. */
interface ElementTarget {
    operator fun invoke(sourceElement: HTMLElement): HTMLElement?

    object PreviousSibling : ElementTarget {
        override fun invoke(sourceElement: HTMLElement) = sourceElement.previousElementSibling as? HTMLElement
    }

    object Parent : ElementTarget {
        override fun invoke(sourceElement: HTMLElement) = sourceElement.parentElement as? HTMLElement
    }

    class Ancestor(private val matching: (HTMLElement) -> Boolean) : ElementTarget {
        override fun invoke(sourceElement: HTMLElement): HTMLElement? {
            var currElement: HTMLElement? = sourceElement
            do {
                currElement = currElement?.parentElement as? HTMLElement
            } while (currElement != null && !matching(currElement))
            return currElement
        }
    }

    class Find(private val matching: (HTMLElement) -> Boolean) : ElementTarget {
        override fun invoke(sourceElement: HTMLElement): HTMLElement? {
            return document.body?.let { body ->
                val walker = document.createTreeWalker(body, NodeFilter.SHOW_ELEMENT) { element ->
                    if (element is HTMLElement && matching(element)) NodeFilter.FILTER_ACCEPT else NodeFilter.FILTER_SKIP
                }
                walker.nextNode() as? HTMLElement
            }
        }
    }
}

enum class Placement {
    Top,
    Left,
    Right,
    Bottom,
}

val PopupStyle = ComponentStyle.base("silk-popup") {
    Modifier
        .position(Position.Absolute)
        .zIndex(2) // TODO: Enumerate Z-indexes
        .transitionProperty("opacity")
        .transitionDuration(100.ms)
}

@Suppress("NAME_SHADOWING")
@Composable
fun Popup(
    target: ElementTarget,
    modifier: Modifier = Modifier,
    placement: Placement = Placement.Bottom,
    variant: ComponentVariant? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    var targetElement by remember { mutableStateOf<HTMLElement?>(null) }
    Box(Modifier.display(DisplayStyle.None),
        ref = ref { element ->
            target(sourceElement = element)?.apply {
                onmouseenter = { targetElement = this; Unit }
                onmouseleave = { targetElement = null; Unit }
            }
        }
    )

    targetElement?.let { element ->
        val targetBounds = element.getBoundingClientRect()
        var popupBounds by remember { mutableStateOf<DOMRect?>(null) }
        val absPosModifier = popupBounds?.let { popupBounds ->
            when (placement) {
                Placement.Top -> {
                    println("${targetBounds.top} -> ${targetBounds.bottom}")
                    Modifier
                        .left((targetBounds.left - (popupBounds.width - targetBounds.width) / 2).px)
                        .top((targetBounds.top - 10 - popupBounds.height).px)
                }
                Placement.Bottom -> {
                    Modifier
                        .left((targetBounds.left - (popupBounds.width - targetBounds.width) / 2).px)
                        .top((targetBounds.bottom + 10).px)
                }
                Placement.Left -> {
                    Modifier
                        .top((targetBounds.top - (popupBounds.height - targetBounds.height) / 2).px)
                        .left((targetBounds.left - 10 - popupBounds.width).px)
                }
                Placement.Right -> {
                    Modifier
                        .top((targetBounds.top - (popupBounds.height - targetBounds.height) / 2).px)
                        .left((targetBounds.right + 10).px)
                }
            }
        }
            // Hack - move the popup out of the way while we calculate its width, or else it can block the cursor
            // causing focus to be gained and lost
            ?: Modifier.top((-100).percent).left((-100).percent).opacity(0) // Hide for a frame or so until we've calculated the final popup location
        Box(
            PopupStyle.toModifier(variant).then(absPosModifier).then(modifier),
            ref = ref { element ->
                popupBounds = element.getBoundingClientRect()
            },
            content = content
        )
    }
}
