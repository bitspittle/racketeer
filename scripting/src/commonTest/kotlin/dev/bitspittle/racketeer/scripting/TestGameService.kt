package dev.bitspittle.racketeer.scripting

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.types.DelegatingLogger
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.action.ExprEnqueuer
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingEnqueuer
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.types.CardEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.GameService
import dev.bitspittle.racketeer.scripting.types.BuildingEnqueuerImpl
import dev.bitspittle.racketeer.scripting.types.ExprEnqueuerImpl
import kotlin.random.Random

private val FAKE_GAME_DATA_TEXT = """
    title: Do Crimes Test
    icons:
      cash: "${'$'}"
      expendable: "x"
      flavor: "Flavor - "
      influence: "&"
      luck: "%"
      swift: ""
      suspicious: ""
      veteran: "!"
      vp: "*"
    features: []
    unlocks: []
    numTurns: 3
    initialHandSize: 4
    initialCash: 0
    initialInfluence: 0
    initialLuck: 5

    cardTypes:
      # Ordered by how they should show up on a card, NOT alphabetically necessarily
      - Action
      - Treasure
      - Thief
      - Spy
      - Legend

    traitNames:
      expendable: Expendable
      suspicious: Suspicious
      swift: Swift

    upgradeNames:
      cash: Dextrous
      influence: Artful
      luck: Lucky
      veteran: Veteran

    rarities:
      - name: Common
        blueprintFrequency: 3
        cardFrequency: 5
        shopCount: 10
      - name: Uncommon
        blueprintFrequency: 2
        cardFrequency: 3
        shopCount: 8
      - name: Rare
        blueprintFrequency: 1
        cardFrequency: 1
        shopCount: 6

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
        description:
          ability: ""
        playActions: []

      - name: Newsie
        tier: 0
        types: [spy]
        description:
          ability: ""
        playActions: []

      - name: Con Artist
        tier: 0
        types: [thief, spy]
        cost: 2
        description:
          ability: ""
        playActions: []

      - name: Squealer
        tier: 0
        types: [spy]
        cost: 2
        description:
          ability: ""
        playActions: []

      - name: Fool's Gold
        tier: 0
        types: [treasure]
        cost: 2
        description:
          ability: ""
        playActions: []

      - name: Croupier
        tier: 0
        types: [thief]
        cost: 3
        description:
          ability: ""
        playActions: []

      # TIER 2 (i.e. 1 when 0-indexed)

      - name: Ditch the Goods
        tier: 1
        traits: [swift]
        types: [action]
        cost: 2
        description:
          ability: ""
        playActions: []

      - name: Cheese It!
        tier: 1
        types: [action]
        cost: 2
        description:
          ability: ""
        playActions: []

      - name: Lady Thistledown
        tier: 1
        types: [spy, legend]
        cost: 3
        description:
          ability: ""
        playActions: []

      - name: Embezzler
        tier: 1
        types: [thief, spy]
        cost: 4
        description:
          ability: ""
        playActions: - fx-add! '(game-set! 'cash '(+ ${'$'}it 1))

    blueprints:
      - name: Wine Cellar
        description:
          ability: This gains * at the start of every turn.
        rarity: 0
        buildCost:
          cash: 2
          influence: 2
        canActivate: false # Passive building
        initActions:
          - fx-add! --lifetime 'game --event 'turn-start --data (building-get ${'$'}this 'id) '(building-add! building-with-id ${'$'}data 'vp 1)

      - name: City Hall
        description:
          ability: Gain 4&.
        rarity: 0
        buildCost:
          cash: 3
          influence: 3
        activationCost:
          cash: 2
        activateActions:
          - game-add! 'influence 4

      - name: Stock Exchange
        description:
          ability: Gain 4${'$'}.
        rarity: 2
        buildCost:
          cash: 3
          influence: 3
        activationCost:
          influence: 2
        activateActions:
          - game-add! 'cash 4

      - name: Newsstand
        description:
          ability: Draw a card.
        rarity: 0
        vp: 1
        buildCost:
          cash: 2
          influence: 2
        activationCost:
          cash: 1
        activateActions:
          - game-draw! 1
""".trimIndent()

fun createFakeGameData() = GameData.decodeFromString(FAKE_GAME_DATA_TEXT)

class StubExprEnqueuer : ExprEnqueuer {
    override fun enqueue(gameState: GameState, codeLines: List<String>) { NotImplementedError() }
}
class StubCardEnqueuer : CardEnqueuer {
    override fun enqueueInitActions(gameState: GameState, card: Card) { NotImplementedError() }
    override fun enqueuePlayActions(gameState: GameState, card: Card) { NotImplementedError() }
    override fun enqueuePassiveActions(gameState: GameState, card: Card) { NotImplementedError() }
}
class StubBuildingEnqueuer : BuildingEnqueuer {
    override suspend fun canActivate(gameState: GameState, building: Building): Boolean = true
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
        ExprEnqueuerImpl(env, exprCache, actionQueue),
        CardEnqueuerImpl(env, exprCache, actionQueue),
        BuildingEnqueuerImpl(env, exprCache, actionQueue)
    )
}

fun TestCard(
    name: String,
    types: List<String> = listOf(""),
    tier: Int = 0,
    vp: Int = 0,
    cost: Int = 0,
    desc: CardTemplate.Description = CardTemplate.Description("dummy"),
    playActions: List<String> = emptyList(),
) = CardTemplate(
    name,
    types,
    tier,
    desc,
    vp = vp,
    cost = cost,
    playActions = playActions,
).instantiate()


// Create a random with a fixed seed so tests run consistently

class TestGameService private constructor(
    private val copyableRandom: CopyableRandom,
    override val gameData: GameData,
    override val enqueuers: Enqueuers,
    override val chooseHandler: ChooseHandler,
) : GameService {
    companion object {
        suspend fun create(
            copyableRandom: CopyableRandom = CopyableRandom(0),
            gameData: GameData = createFakeGameData(),
            enqueuers: Enqueuers = Enqueuers(
                ActionQueue(),
                StubExprEnqueuer(),
                StubCardEnqueuer(),
                StubBuildingEnqueuer(),
            ),
            chooseHandler: ChooseHandler = object : ChooseHandler {
                override suspend fun query(
                    prompt: String?,
                    list: List<Any>,
                    range: IntRange,
                    requiredChoice: Boolean
                ): List<Any> {
                    return emptyList()
                }
            }
        ) = TestGameService(
            copyableRandom,
            gameData,
            enqueuers,
            chooseHandler,
        ).apply {
            // In production, this is done via game initActions, but as we don't have a real enqueuer for tests
            // (and instead we just create a stub), we just manually set things up. A few tests care about an initial
            // deck.
            val pickpocket = gameData.cards.single { it.name == "Pickpocket" }
            val newsie = gameData.cards.single { it.name == "Newsie" }

            gameState.move(
                List(5) { pickpocket.instantiate() } + List(3) { newsie.instantiate() },
                gameState.deck,
                ListStrategy.RANDOM
            )
        }
    }

    val random: Random get() = copyableRandom()

    override val describer: Describer = Describer(gameData, showDebugInfo = { true })
    override val gameState = MutableGameState(gameData, setOf(), enqueuers, copyableRandom)
    private val _logs = mutableListOf<String>()
    val logs: List<String> = _logs

    override val logger = object : DelegatingLogger() {
        override fun log(message: String) {
            _logs.add(message)
        }
    }
}