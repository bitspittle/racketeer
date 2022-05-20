package dev.bitspittle.racketeer.console.utils

const val DEFAULT_WRAP_WIDTH = 60

fun String.wrap(width: Int = DEFAULT_WRAP_WIDTH): String {
    val lines = this.split('\n').toMutableList()
    return buildString {
        while (lines.isNotEmpty()) {
            val line = lines.removeFirst()
            if (line.length >= width) {
                // Don't break words, just go over the requested wrapping width if we can't fit.
                // This is useful because if we went over, it's probably because of a URL....
                val charIndexToBreakAt = line.take(width).lastIndexOf(' ').takeIf { it >= 0 } ?: line.indexOf(' ')
                if (charIndexToBreakAt >= 0) {
                    val firstPart = line.substring(0, charIndexToBreakAt)
                    val secondPart = line.substring(charIndexToBreakAt + 1)
                    lines.add(0, secondPart) // Return second part for further processing
                    appendLine(firstPart)
                } else {
                    // This line is just one long, giant word without spaces
                    appendLine(line)
                }
            } else {
                appendLine(line)
            }
        }
        check(this.last() == '\n')
        this.deleteAt(this.lastIndex)
    }
}

