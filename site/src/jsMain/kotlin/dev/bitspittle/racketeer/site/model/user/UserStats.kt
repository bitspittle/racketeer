package dev.bitspittle.racketeer.site.model.user

import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameState
import kotlinx.serialization.Serializable

@Serializable
class CardStats(
    val name: String,
    var ownedCount: Long = 0,
)

@Serializable
class BuildingStats(
    val name: String,
    var builtCount: Long = 0,
)

enum class GameCancelReason {
    NONE,
    RESTARTED,
    ABORTED,
}

@Serializable
class GameStats(
    val vp: Int,
    val features: Set<Feature.Type>,
    val cancelReason: GameCancelReason = GameCancelReason.NONE,
) {
    companion object {
        fun from(state: GameState, cancelReason: GameCancelReason = GameCancelReason.NONE) =
            GameStats(state.vp, state.features, cancelReason)
    }
}

interface UserStats {
    val cards: Map<String, CardStats>
    val buildings: Map<String, BuildingStats>
    val games: List<GameStats>
}

@Serializable
class MutableUserStats(
    override val cards: MutableMap<String, CardStats>,
    override val buildings: MutableMap<String, BuildingStats>,
    override val games: MutableList<GameStats>,
) : UserStats {
    companion object {
        fun from(cardStats: List<CardStats>, buildingStats: List<BuildingStats>, gameStats: List<GameStats>) =
            MutableUserStats(
                cardStats.associateBy { it.name }.toMutableMap(),
                buildingStats.associateBy { it.name }.toMutableMap(),
                gameStats.toMutableList()
            )
    }

    constructor() : this(mutableMapOf(), mutableMapOf(), mutableListOf())
}

fun MutableUserStats.clear() {
    cards.clear()
    buildings.clear()
    games.clear()
}

fun MutableMap<String, CardStats>.notifyOwnership(card: CardTemplate) {
    getOrPut(card.name) { CardStats(card.name) }
        .ownedCount++
}
fun MutableMap<String, CardStats>.notifyOwnership(card: Card) = notifyOwnership(card.template)

fun MutableMap<String, BuildingStats>.notifyBuilt(blueprint: Blueprint) {
    getOrPut(blueprint.name) { BuildingStats(blueprint.name) }
        .builtCount++
}
