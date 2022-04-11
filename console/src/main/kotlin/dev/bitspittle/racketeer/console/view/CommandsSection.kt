package dev.bitspittle.racketeer.console.view

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import kotlin.math.min

class CommandsSection(private val commands: List<Command>, currIndex: Int = 0) {
    init {
        require(commands.isNotEmpty())
    }

    private var pageStart = 0
    private val pageEnd get() = pageStart + Constants.PAGE_SIZE
    private var currIndex = 0
        private set(value) {
            field = value.coerceIn(commands.indices)
            if (field < pageStart) {
                pageStart = field
            } else if (field >= pageEnd) {
                pageStart += field - pageEnd + 1
            }
        }
    init {
        this.currIndex = currIndex
    }

    val currCommand: Command get() = commands[currIndex]

    fun handleKey(key: Key): Boolean {
        var handled = true
        when (key) {
            Keys.UP -> currIndex--
            Keys.DOWN -> currIndex++
            Keys.PAGE_UP -> currIndex -= Constants.PAGE_SIZE
            Keys.PAGE_DOWN -> currIndex += Constants.PAGE_SIZE
            Keys.HOME -> currIndex = 0
            Keys.END -> currIndex = commands.size - 1
            else -> handled = false
        }

        return handled
    }

    fun renderInto(scope: RenderScope) {
        scope.apply {
            if (pageStart > 0) {
                textLine("... $pageStart earlier item(s) ...")
            }
            commands.forEachIndexed { i, command ->
                if (i in pageStart until pageEnd) {
                    if (i == currIndex) {
                        text("> ")
                    } else {
                        text("  ")
                    }
                    command.renderTitleInto(this)
                }
            }

            if (pageEnd < commands.size) {
                textLine("... ${commands.size - pageEnd} more item(s) ...")
            }

            textLine()
        }
    }
}
