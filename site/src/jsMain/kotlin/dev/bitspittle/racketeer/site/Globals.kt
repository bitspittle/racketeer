package dev.bitspittle.racketeer.site

import org.jetbrains.compose.web.css.*

data class Size<W: CSSUnit, H: CSSUnit>(val w: CSSSizeValue<W>, val h: CSSSizeValue<H>)

object G {
    object Sizes {
        val CardGroup = Size(50.vw, 200.px)
    }
    object Font {
        const val NAME = "GameText"

        object Sizes {
            val Small = 13.px
            val Normal = 16.px
        }
    }
}