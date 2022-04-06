import dev.bitspittle.racketeer.console.game.GameSession
import dev.bitspittle.racketeer.model.game.GameData
import kotlin.io.path.Path
import kotlin.io.path.readText

fun main() {
    val gameData = with(Path("gamedata.yaml").readText(Charsets.UTF_8)) {
        GameData.decodeFromString(this)
    }

    GameSession(gameData).start()
}