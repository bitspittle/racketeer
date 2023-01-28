package dev.bitspittle.racketeer.site.viewmodel

import androidx.compose.runtime.mutableStateListOf
import dev.bitspittle.limp.types.Logger

/** Log messages into memory that is observable by Compose. */
class LoggerViewModel : Logger {
    private val _messages = mutableStateListOf<String>()
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
        println("⚠️ $message")
    }

    override fun error(message: String) {
        println("⛔ $message")
    }

    override fun debug(message: String) {
        println("⚙️ $message")
    }
}