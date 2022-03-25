import dev.bitspittle.racketeer.console.GameSession
import dev.bitspittle.racketeer.model.game.GameData
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.Path
import kotlin.io.path.readText

fun main() {
//    run {
//        val gameData = GameData(
//            GameConfig(4),
//            cards = listOf(
//                CardTemplate(
//                    "Example",
//                    2,
//                    4
//                )
//            )
//        )
//    }

    val gameData =
        Yaml.decodeFromString(GameData.serializer(), Path("gamedata.yaml").readText(Charsets.UTF_8))

    GameSession(gameData).start()
}