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

enum class GameCancelReason {
    NONE,
    RESTARTED,
    ABORTED,
}

@Serializable
class GameStats(
    val vp: Int,
    // val features: Set<GameFeature>,
    val cancelReason: GameCancelReason = GameCancelReason.NONE,
) {
    companion object {
        fun loadFrom(userDataDir: UserDataDir): Iterable<GameStats>? {
            return try {
                userDataDir.pathForGameStats()
                    .takeIf { it.exists() }
                    ?.let { path -> Yaml.decodeFromString<List<GameStats>>(path.readText()) }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

val GameStats.wasFinished get() = (cancelReason == GameCancelReason.NONE)

val Iterable<GameStats>.totalVp get() = this.asSequence()
    .filter { it.wasFinished }
    .sumOf { it.vp }

fun Iterable<GameStats>.saveInto(userDataDir: UserDataDir) {
    val self = this
    userDataDir.pathForGameStats().apply {
        parent.createDirectories()
        writeText(self.toList().encodeToYaml())
    }
}
