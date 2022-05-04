package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserData
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
class CardStats(
    val name: String,
    var ownedCount: Long = 0,
) {
    companion object {
        fun load(userData: UserData): Iterable<CardStats>? {
            return try {
                userData.pathForCardStats()
                    .takeIf { it.exists() }
                    ?.let { path -> Yaml.decodeFromString<List<CardStats>>(path.readText()) }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

fun Iterable<CardStats>.save(userData: UserData) {
    val self = this
    userData.pathForCardStats().apply {
        parent.createDirectories()
        writeText(Yaml.encodeToString(self.toList()))
    }
}
