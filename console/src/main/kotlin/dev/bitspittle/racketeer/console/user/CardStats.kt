package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir
import dev.bitspittle.racketeer.console.utils.encodeToYaml
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
        fun loadFrom(userDataDir: UserDataDir): Iterable<CardStats>? {
            return try {
                userDataDir.pathForCardStats()
                    .takeIf { it.exists() }
                    ?.let { path -> Yaml.decodeFromString<List<CardStats>>(path.readText()) }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

fun Iterable<CardStats>.saveInto(userDataDir: UserDataDir) {
    val self = this
    userDataDir.pathForCardStats().apply {
        parent.createDirectories()
        writeText(self.toList().encodeToYaml())
    }
}
