package dev.bitspittle.racketeer.console.view.sections

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ConfirmChoicesCommand

class SelectCommandsSection(commands: List<Command>) {
    private val lastChoiceIndex = commands.size
    private val commands = commands + listOf(ConfirmChoicesCommand)

    private var currIndex = 0
        set(value) {
            field = value.coerceIn(commands.indices)
        }

    private var selectedIndices = mutableSetOf<Int>()

    fun handleKey(key: Key): Boolean {
        var handled = true
        when (key) {
            Keys.UP -> currIndex--
            Keys.DOWN -> currIndex++
            Keys.PAGE_UP -> currIndex -= Constants.PAGE_SIZE
            Keys.PAGE_DOWN -> currIndex += Constants.PAGE_SIZE
            Keys.SPACE -> {
                if (currIndex < commands.lastIndex) { // don't allow selecting the "confirm" command
                    // Toggle selection
                    if (!selectedIndices.add(currIndex)) {
                        selectedIndices.remove(currIndex)
                    }
                }
            }
            Keys.A -> {
                // Toggle all
                if (selectedIndices.size == commands.size - 1) {
                    selectedIndices.clear()
                }
                else {
                    selectedIndices.addAll(0 .. lastChoiceIndex)
                }
            }

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
                if (i < commands.lastIndex) {
                    val checkmark = if (selectedIndices.contains(i)) 'x' else ' '
                    text("[$checkmark] ")
                }
                textLine(command.title)
            }
        }
    }
}