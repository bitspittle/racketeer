package dev.bitspittle.racketeer.scripting

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.types.DelegatingLogger
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingEnqueuer
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.types.CardEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.GameService
import dev.bitspittle.racketeer.scripting.types.BuildingEnqueuerImpl
import kotlin.random.Random

private val FAKE_GAME_DATA_TEXT = """
    title: Do Crimes Test
    icons:
      cash: "${'$'}"
      influence: "&"
      luck: "%"
      card: "▯"
      vp: "*"
    numTurns: 3
    initialHandSize: 4
    initialCash: 0
    initialInfluence: 0
    initialLuck: 5
    initialBlueprintCount: 0
    initialDeck:
      - Pickpocket 5
      - Rumormonger 3
    cardTypes:
      # Ordered by how they should show up on a card, NOT alphabetically necessarily
      - Action
      - Treasure
      - Thief
      - Spy
      - Legend

    upgradeNames:
      cash: Dextrous
      influence: Artful
      luck: Lucky
      veteran: Veteran

    rarities:
      - name: Common
        frequency: 5
      - name: Uncommon
        frequency: 3
      - name: Rare
        frequency: 1

    tierFrequencies:
      - 1
      - 1
      - 1
      - 1
      - 1
    shopSizes:
      - 3
      - 4
      - 5
      - 6
      - 7
    shopPrices:
      - 5
      - 10
      - 15
      - 20

    rankings:
      - name: D
        score: 0
      - name: C
        score: 10
      - name: B
        score: 20
      - name: A
        score: 30
      - name: S
        score: 40

    cards:
      # TIER 1

      - name: Pickpocket
        tier: 0
        types: [thief]
        flavor: ""
        playActions: []

      - name: Rumormonger
        tier: 0
        types: [spy]
        flavor: ""
        playActions: []

      - name: Con Artist
        tier: 0
        types: [thief, spy]
        cost: 2
        flavor: ""
        playActions: []

      - name: Squealer
        tier: 0
        types: [spy]
        cost: 2
        flavor: ""
        playActions: []

      - name: Fool's Gold
        tier: 0
        types: [treasure]
        cost: 2
        flavor: ""
        playActions: []

      - name: Croupier
        tier: 0
        types: [thief]
        cost: 3
        flavor: ""
        playActions: []

      # TIER 2 (i.e. 1 when 0-indexed)

      - name: Ditch the Goods
        tier: 1
        types: [action]
        cost: 2
        flavor: ""
        playActions: []

      - name: Cheese It!
        tier: 1
        types: [action]
        cost: 2
        flavor: ""
        playActions: []

      - name: Lady Thistledown
        tier: 1
        types: [spy, legend]
        cost: 3
        flavor: ""
        playActions: []

      - name: Embezzler
        tier: 1
        types: [thief, spy]
        cost: 4
        flavor: ""
        playActions: - fx-add! '(game-set! 'cash '(+ ${'$'}it 1))
        
    # Disabled for now. Should add tests later...
    blueprints: []
""".trimIndent()

fun createFakeGameData() = GameData.decodeFromString(FAKE_GAME_DATA_TEXT)

class StubCardEnqueuer : CardEnqueuer {
    override fun enqueueInitActions(gameState: GameState, card: Card) { NotImplementedError() }
    override fun enqueuePlayActions(gameState: GameState, card: Card) { NotImplementedError() }
    override fun enqueuePassiveActions(gameState: GameState, card: Card) { NotImplementedError() }
}
class StubBuildingEnqueuer : BuildingEnqueuer {
    override fun enqueueInitActions(gameState: GameState, building: Building) { NotImplementedError() }
    override fun enqueueActivateActions(gameState: GameState, building: Building) { NotImplementedError() }
    override fun enqueuePassiveActions(gameState: GameState, building: Building) { NotImplementedError() }
}

@Suppress("TestFunctionName") // Imitating a factory method
fun TestEnqueuers(env: Environment): Enqueuers {
    val exprCache = ExprCache()
    val actionQueue = ActionQueue()

    return Enqueuers(
        actionQueue,
        CardEnqueuerImpl(env, exprCache, actionQueue),
        BuildingEnqueuerImpl(env, exprCache, actionQueue)
    )
}

// Create a random with a fixed seed so tests run consistently
class TestGameService(
    private val copyableRandom: CopyableRandom = CopyableRandom(0),
    override val gameData: GameData = createFakeGameData(),

    override val enqueuers: Enqueuers = Enqueuers(
        ActionQueue(),
        StubCardEnqueuer(),
        StubBuildingEnqueuer(),
    ),
    override val chooseHandler: ChooseHandler = object : ChooseHandler {
        override suspend fun query(
            prompt: String?,
            list: List<Any>,
            range: IntRange,
            requiredChoice: Boolean
        ): List<Any> {
            return emptyList()
        }
    },
) : GameService {
    val random: Random get() = copyableRandom()

    override val describer: Describer = Describer(gameData, showDebugInfo = { true })
    override val gameState = MutableGameState(gameData, enqueuers, copyableRandom)
    private val _logs = mutableListOf<String>()
    val logs: List<String> = _logs

    override val logger = object : DelegatingLogger() {
        override fun log(message: String) {
            _logs.add(message)
        }
    }
}