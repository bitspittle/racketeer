package dev.bitspittle.racketeer.site.components.animations

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.scale
import com.varabyte.kobweb.silk.components.animation.keyframes
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

// Inspired by https://www.florin-pop.com/blog/2019/03/css-pulse-effect/
val Pulse by keyframes {
    0.percent {
        Modifier.scale(0.95)
    }

    70.percent {
        Modifier
            .scale(1.05)
            .boxShadow(blurRadius = 4.px, spreadRadius = 4.px, color = Colors.Grey)
    }
    100.percent {
        Modifier
            .scale(0.95)
            .boxShadow(color = Colors.Transparent)
    }
}
