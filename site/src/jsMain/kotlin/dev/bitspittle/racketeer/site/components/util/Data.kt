package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.model.account.Account
import dev.bitspittle.racketeer.site.model.user.MutableUserStats
import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import kotlin.js.Date

object Data {
    class Key<T: Any>(val name: String)

    data class Result<T: Any>(
        val timestamp: Date,
        val value: T,
    )

    fun <T: Any> save(key: Key<T>, value: T) {
        saveRaw(key, value.encodeToYaml())
    }

    fun saveRaw(key: Key<*>, value: String) {
        val timestamp = Date().toTimeString()
        localStorage.setItem(key.name, value)
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
        return loadRaw(key)?.let {
            Result(it.timestamp, Yaml.decodeFromString(it.value))
        }
    }

    fun loadRaw(key: Key<*>): Result<String>? {
        var result: Result<String>? = null
        localStorage.getItem(key.name)?.let { value ->
            val timestamp = Date(localStorage.getItem(timestampKeyFor(key))!!)
            result = Result(
                timestamp,
                value,
            )
        }
        return result
    }

    fun timestampKeyFor(key: Key<*>) = "${key.name}-timestamp"

    object Keys {
        val Account = Key<Account>("account")
        val UserStats = Key<MutableUserStats>("userstats")
        val Quicksave = Key<GameSnapshot>("quicksave")
        val GameData = Key<String>("gamedata")
    }
}