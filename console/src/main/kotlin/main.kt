import dev.bitspittle.racketeer.console.game.GameSession
import dev.bitspittle.racketeer.model.game.GameData
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

private fun clearConsole() {
    print("\u001b[H\u001b[2J")
    System.out.flush()
}

fun main() {
    val gameData = run {
        // For users, we will just use the bundled gamedata.yaml. However, if you know what you're doing, you can
        // create a local gamedata.yaml and iterate with that one instead.
        val gameDataStr = Path("gamedata.yaml").takeIf { it.exists() }?.readText()
            ?: GameSession::class.java.getResourceAsStream("/gamedata.yaml")!!.readAllBytes().decodeToString()
        GameData.decodeFromString(gameDataStr)
    }

    clearConsole()
    GameSession(gameData).start()
}