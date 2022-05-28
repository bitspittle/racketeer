package dev.bitspittle.racketeer.model.text

fun String.escapeQuotes() = this.replace("\"", "\\\"")
fun String.unescapeQuotes() = this.replace("\\\"", "\"")

fun String.quote() = "\"${this.escapeQuotes()}\""
fun String.unquote(): String = run {
    val unquoted = this.removeSurrounding("\"")
    if (unquoted == this) return this
    return unquoted.unescapeQuotes()
}