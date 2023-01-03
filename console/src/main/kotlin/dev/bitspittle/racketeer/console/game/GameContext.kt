package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.console.user.BuildingStats
import dev.bitspittle.racketeer.console.user.CardStats
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.UserStats
import dev.bitspittle.racketeer.console.utils.createNewGame
import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.text.Describer

fun MutableMap<String, CardStats>.notifyOwnership(card: CardTemplate) {
    compute(card.name) { name, prevStats ->
        val stats = prevStats ?: CardStats(name)
        stats.ownedCount++
        stats
    }
}
fun MutableMap<String, CardStats>.notifyOwnership(card: Card) = notifyOwnership(card.template)

fun MutableMap<String, BuildingStats>.notifyBuilt(blueprint: Blueprint) {
    compute(blueprint.name) { name, prevStats ->
        val stats = prevStats ?: BuildingStats(name)
        stats.builtCount++
        stats
    }
}
fun MutableMap<String, BuildingStats>.notifyBuilt(building: Building) = notifyBuilt(building.blueprint)

class GameContext(
    val data: GameData,
    val settings: Settings,
    val userStats: UserStats,
    val describer: Describer,
    val env: Environment,
    val enqueuers: Enqueuers,
    val viewStack: ViewStack,
    val app: App,
) {
    var state: MutableGameState = MutableGameState(data, emptySet(), enqueuers)
}