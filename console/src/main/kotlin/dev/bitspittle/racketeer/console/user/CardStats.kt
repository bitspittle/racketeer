package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserDataSupport
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
    var numTimesOwned: Long = 0,
) {
    companion object {
        fun load(): Iterable<CardStats>? {
            return try {
                UserDataSupport.pathForCardStats()
                    .takeIf { it.exists() }
                    ?.let { path -> Yaml.decodeFromString<List<CardStats>>(path.readText()) }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

fun Iterable<CardStats>.save() {
    val self = this
    UserDataSupport.pathForCardStats().apply {
        parent.createDirectories()
        writeText(Yaml.encodeToString(self.toList()))
    }
}
