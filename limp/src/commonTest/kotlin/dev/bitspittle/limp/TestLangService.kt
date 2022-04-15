package dev.bitspittle.limp

import dev.bitspittle.limp.types.DelegatingLogger
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.types.Logger
import kotlin.random.Random

class TestLangService(override val random: Random = Random.Default) : LangService {
    private val _logs = mutableListOf<String>()
    val logs: List<String> = _logs
    override val logger = object : DelegatingLogger() {
        override fun log(message: String) {
            _logs.add(message)
        }
    }

    fun clearLogs() { _logs.clear() }
}