package dev.bitspittle.limp.types

import kotlin.random.Random

/** Misc. services that can be provided to Limp to customize behavior. */
interface LangService {
    val random: Random
    fun log(message: String)
}

class DefaultLangService : LangService {
    override val random: Random = Random.Default

    override fun log(message: String) {
        println(message)
    }
}