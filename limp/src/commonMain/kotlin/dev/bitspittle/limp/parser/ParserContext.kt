package dev.bitspittle.limp.parser

data class ParserContext(val text: String, val startIndex: Int = 0) {
    val isFinished = startIndex > text.lastIndex
    val remaining get() = if (isFinished) "" else text.substring(startIndex)

    fun incStart(delta: Int = 1) = copy(startIndex = startIndex + delta)
    fun getChar(index: Int = 0): Char? = text.getOrNull(startIndex + index)

    fun startsWith(text: String): Boolean = this.text.startsWith(text, startIndex)
    fun takeWhile(predicate: (Char) -> Boolean) = buildString {
        var i = 0
        while (true) {
            val c = getChar(i++) ?: break
            if (!predicate(c)) break
            append(c)
        }
    }
}