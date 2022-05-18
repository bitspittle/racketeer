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
class BuildingStats(
    val name: String,
    var builtCount: Long = 0,
) {
    companion object {
        fun loadFrom(userDataDir: UserDataDir): Iterable<BuildingStats>? {
            return try {
                userDataDir.pathForBuildingStats()
                    .takeIf { it.exists() }
                    ?.let { path -> Yaml.decodeFromString<List<BuildingStats>>(path.readText()) }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

fun Iterable<BuildingStats>.saveInto(userDataDir: UserDataDir) {
    val self = this
    userDataDir.pathForBuildingStats().apply {
        parent.createDirectories()
        writeText(self.toList().encodeToYaml())
    }
}
