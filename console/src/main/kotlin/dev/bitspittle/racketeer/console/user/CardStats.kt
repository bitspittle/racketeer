package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserDataSupport
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Serializable
class CardStats(
    val name: String,
    var numTimesOwned: Long = 0,
)

fun Iterable<CardStats>.save() {
    val self = this
    UserDataSupport.pathForCardStats().apply {
        parent.createDirectories()
        writeText(Yaml.encodeToString(self.toList()))
    }
}
