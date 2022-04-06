package dev.bitspittle.limp

import dev.bitspittle.limp.types.LangService
import kotlin.random.Random

class TestLangService(override val random: Random = Random.Default) : LangService {
    private val _logs = mutableListOf<String>()
    val logs: List<String> = _logs
    override fun log(message: String) {
        _logs.add(message)
    }
}