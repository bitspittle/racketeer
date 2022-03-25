import dev.bitspittle.racketeer.model.GameConfig
import dev.bitspittle.racketeer.model.GameData
import kotlin.io.path.Path
import kotlin.io.path.readText

fun main() {
    run {
        val gameData = GameData(
            GameConfig()
        )
    }


    val gameData = Path("gamedata.yaml").readText(Charsets.UTF_8)
    println(gameData)
}