package dev.bitspittle.racketeer.console.view

import Constants
import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import kotlin.math.min

class CommandsSection(commands: List<Command>, currIndex: Int = 0) {
    private val commands = commands.filter { it.type != Command.Type.Hidden }
    init {
        require(commands.isNotEmpty())
        check(Constants.PAGE_SIZE > 4) // Need room for up and down arrows plus remaining command counts
    }

    private var pageStart = 0
    private val pageEnd get() = pageStart + Constants.PAGE_SIZE
    var currIndex = 0
        set(value) {
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
            val numPreviousItems = pageStart
            val numFollowingItems = commands.size - pageEnd
            val maxTitleLen = commands.maxOf { it.title.length }
            commands.forEachIndexed { i, command ->
                if (i in pageStart until pageEnd) {
                    if (numPreviousItems + numFollowingItems > 0) {
                        when {
                            i == pageStart && numPreviousItems > 0 -> text("▲  ")
                            i == pageStart + 1 && numPreviousItems > 0 -> text(numPreviousItems.toString().padEnd(3))
                            i == pageEnd - 2 && numFollowingItems > 0 -> text(numFollowingItems.toString().padEnd(3))
                            i == pageEnd - 1 && numFollowingItems > 0 -> text("▼  ")
                            else -> text("   ")
                        }
                    }
                    when (i) {
                        currIndex -> text("> ")
                        else -> text("  ")
                    }
                    command.renderTitleInto(this, padding = maxTitleLen)
                }
            }

            textLine()
        }
    }
}
