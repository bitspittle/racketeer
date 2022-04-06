package dev.bitspittle.racketeer.scripting

import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameIcons
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.tier.Tier
import dev.bitspittle.racketeer.scripting.types.GameService
import kotlin.random.Random

private val FAKE_GAME_DATA_TEXT = """
    title: Do Crimes Test
    icons:
      cash: "${'$'}"
      influence: "&"
      luck: "%"
      vp: "*"
    numTurns: 3
    initialHandSize: 4
    initialLuck: 5
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
      patience: Patient

    tiers:
      - name: Common
        frequency: 5
      - name: Uncommon
        frequency: 4
      - name: Rare
        frequency: 3
      - name: Scarce
        frequency: 2
      - name: Legendary
        frequency: 1
    shopPrices:
      - 5
      - 10
      - 15
      - 20
    ratingScores:
      # Anything lower than the first score is D
      - 10 # C
      - 20 # B
      - 30 # A
      - 40 # S

    cards:
      # TIER 1

      - name: Pickpocket
        types: [thief]
        flavor: ""
        actions: []

      - name: Rumormonger
        types: [spy]
        flavor: ""
        actions: []

      - name: Con Artist
        types: [thief, spy]
        cost: 2
        flavor: ""
        actions: []

      - name: Squealer
        types: [spy]
        cost: 2
        flavor: ""
        actions: []

      - name: Fool's Gold
        types: [treasure]
        cost: 2
        flavor: ""
        actions: []

      - name: Croupier
        types: [thief]
        cost: 3
        flavor: ""
        actions: []

      # TIER 2 (i.e. 1 when 0-indexed)

      - name: Ditch the Goods
        tier: 1
        types: [action]
        cost: 2
        flavor: ""
        actions: []

      - name: Cheese It!
        tier: 1
        types: [action]
        cost: 2
        flavor: ""
        actions: []

      - name: Lady Thistledown
        tier: 1
        types: [spy, legend]
        cost: 3
        flavor: ""
        actions: []

      - name: Roving Gambler
        tier: 1
        types: [thief, spy]
        cost: 3
        flavor: ""
        actions: []
""".trimIndent()

private fun createFakeGameData() = GameData.decodeFromString(FAKE_GAME_DATA_TEXT)

class TestGameService(val random: Random = Random.Default) : GameService {
    val gameData = createFakeGameData()
    override val gameState = GameState(gameData, random)
    private val _logs = mutableListOf<String>()
    val logs: List<String> = _logs
    override fun log(message: String) {
        _logs.add(message)
    }
}