package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.model.Settings
import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import kotlin.js.Date

fun <T: Any> T.encodeToYaml() = Yaml { this.encodeDefaultValues = false }.encodeToString(this)

object Data {
    class Key<T: Any>(val name: String)

    class Result<T: Any>(
        val timestamp: Date,
        val value: T,
    )

    fun <T: Any> save(key: Key<T>, value: T) {
        val timestamp = Date().toTimeString()
        localStorage.setItem(key.name, value.encodeToYaml())
        localStorage.setItem(timestampKeyFor(key), timestamp)
    }

    fun exists(key: Key<*>): Boolean {
        return localStorage.getItem(key.name) != null
    }

    fun delete(key: Key<*>): Boolean {
        return if (exists(key)) {
            localStorage.removeItem(key.name)
            localStorage.removeItem(timestampKeyFor(key))
            true
        } else false
    }

    inline fun <reified T: Any> load(key: Key<T>): Result<T>? {
        var result: Result<T>? = null
        localStorage.getItem(key.name)?.let { value ->
            val timestamp = Date(localStorage.getItem(timestampKeyFor(key))!!)
            result = Result(
                timestamp,
                Yaml.decodeFromString(value)
            )
        }
        return result
    }

    fun timestampKeyFor(key: Key<*>) = "${key.name}-timestamp"

    object Keys {
//        val CardStats = "cardstats"
//        val BuildingState = "bldgstats"
//        val GameStats = "gamestats"
        val Settings = Key<Settings>("settings")
        val Quicksave = Key<GameSnapshot>("quicksave")
    }
}