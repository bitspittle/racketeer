package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.console.user.CardStats
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer

fun MutableMap<String, CardStats>.notifyOwnership(card: CardTemplate) {
    compute(card.name) { _, prevStats ->
        val stats = prevStats ?: CardStats(card.name)
        stats.ownedCount++
        stats
    }
}
fun MutableMap<String, CardStats>.notifyOwnership(card: Card) = notifyOwnership(card.template)

class GameContext(
    val data: GameData,
    val settings: Settings,
    val cardStats: MutableMap<String, CardStats>,
    val describer: Describer,
    var state: GameState,
    val env: Environment,
    val enqueuers: Enqueuers,
    val viewStack: ViewStack,
    val app: App
)