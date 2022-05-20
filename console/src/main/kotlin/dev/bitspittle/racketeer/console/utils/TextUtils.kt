package dev.bitspittle.racketeer.console.utils

const val DEFAULT_WRAP_WIDTH = 60

fun String.wrap(width: Int = DEFAULT_WRAP_WIDTH): String {
    val lines = this.split('\n').toMutableList()
    return buildString {
        while (lines.isNotEmpty()) {
            val line = lines.removeFirst()
            if (line.length >= width) {
                val charIndexToBreakAt = line.take(width).lastIndexOf(' ')
                val firstPart = line.substring(0, charIndexToBreakAt)
                val secondPart = line.substring(charIndexToBreakAt + 1)
                lines.add(0, secondPart) // Return second part for further processing
                appendLine(firstPart)
            } else {
                appendLine(line)
            }
        }
        check(this.last() == '\n')
        this.deleteAt(this.lastIndex)
    }
}

