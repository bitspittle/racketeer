package dev.bitspittle.racketeer.console.view.sections

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command

class PickCommandSection(private val commands: List<Command>) {
    private var currIndex = 0
        set(value) {
            field = value.coerceIn(commands.indices)
        }

    fun handleKey(key: Key): Boolean {
        var handled = true
        when (key) {
            Keys.UP -> currIndex--
            Keys.DOWN -> currIndex++
            Keys.PAGE_UP -> currIndex -= Constants.PAGE_SIZE
            Keys.PAGE_DOWN -> currIndex += Constants.PAGE_SIZE
            else -> handled = false
        }

        return handled
    }

    fun renderInto(scope: RenderScope) {
        scope.apply {
            commands.forEachIndexed { i, command ->
                if (i == currIndex) {
                    text("> ")
                }
                else {
                    text("  ")
                }
                textLine(command.title)
            }
        }
    }
}
