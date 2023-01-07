package dev.bitspittle.racketeer.site.model

import dev.bitspittle.limp.types.Logger

/** Log messages into memory, leaving it up for a different system to read it out and render things on screen. */
class MemoryLogger : Logger {
    private val _messages = mutableListOf<String>()
    val messages: List<String> = _messages

    fun clear() {
        _messages.clear()
    }

    private fun append(message: String, map: (String) -> String = { it }) {
        message.split('\n').map(map).forEach { _messages.add(it) }
    }

    override fun info(message: String) {
        append(message) { line -> if (line.isNotBlank()) "• $line" else "" }
    }

    override fun warn(message: String) {
        append("⚠️ $message")
    }

    override fun error(message: String) {
        append("⛔ $message")
    }

    override fun debug(message: String) {
        append("⚙️ $message")
    }
}