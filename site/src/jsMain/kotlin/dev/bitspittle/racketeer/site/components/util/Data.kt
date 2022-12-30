package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import kotlin.js.Date

// package dev.bitspittle.racketeer.console.command.commands.system
//
//import net.harawata.appdirs.AppDirsFactory
//import java.nio.file.Paths
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import kotlin.io.path.absolutePathString
//import kotlin.io.path.exists
//import kotlin.io.path.getLastModifiedTime
//
//class UserDataDir(appName: String) {
//    companion object {
//        const val QUICKSAVE_SLOT = -1
//    }
//
//    val path = Paths.get(AppDirsFactory.getInstance().getUserDataDir(appName, null, null))
//
//    fun firstFreeSlot(): Int {
//        var slot = 0
//        while (pathForSlot(slot).exists()) {
//            ++slot
//        }
//        return slot
//    }
//
//    fun pathForSlot(slot: Int) = path.resolve(if (slot >= 0) "savegame.$slot.yaml" else "quicksave.yaml")
//
//    fun pathForCardStats() = path.resolve("cardstats.yaml")
//    fun pathForBuildingStats() = path.resolve("bldgstats.yaml")
//    fun pathForGameStats() = path.resolve("gamestats.yaml")
//    fun pathForSettings() = path.resolve("settings.yaml")
//
//    fun modifiedTime(slot: Int): String {
//        val path = pathForSlot(slot).takeIf { it.exists() }
//        return if (path != null) {
//            path.getLastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
//                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
//        }
//        else {
//            "(EMPTY)"
//        }
//    }
//}

object Data {
    class Key<T: Any>(val name: String)

    class Result<T: Any>(
        val timestamp: Date,
        val value: T,
    )

    private fun <T: Any> T.encodeToYaml() = Yaml { this.encodeDefaultValues = false }.encodeToString(this)
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
//        val Settings = "settings"
        val Quicksave = Key<GameSnapshot>("quicksave")
    }
}