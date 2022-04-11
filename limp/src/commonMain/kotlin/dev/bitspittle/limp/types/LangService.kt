package dev.bitspittle.limp.types

import kotlin.random.Random

/** Misc. services that can be provided to Limp to customize behavior. */
interface LangService {
    val random: Random
    val logger: Logger
}

class DefaultLangService : LangService {
    override val random: Random = Random.Default
    override val logger = ConsoleLogger()
}