package dev.bitspittle.racketeer.site

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.compose.ui.graphics.Colors as KobwebColors
import org.jetbrains.compose.web.css.*

data class Size<W: CSSUnit, H: CSSUnit>(val w: CSSSizeValue<W>, val h: CSSSizeValue<H>)

object G {
    object Sizes {
        val Card = Size(180.px, 155.px)
        val CardGroup = Size(Card.w + 40.px, Card.h + 40.px)
        val MenuWidth = 600.px
    }
    object Font {
        object Sizes {
            val ExtraSmall = 11.px
            val Small = 13.px
            val Normal = 16.px
            val Large = 20.px
        }
    }

    object Colors {
        val DisabledOpacity = 0.5
        object Card {
            val Front = KobwebColors.LightGray
            val Back = KobwebColors.DarkGray
        }
    }

    lateinit var version: String
}
