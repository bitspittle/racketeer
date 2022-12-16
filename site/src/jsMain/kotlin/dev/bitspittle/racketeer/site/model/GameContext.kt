package dev.bitspittle.racketeer.site.model

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.utils.installDefaults
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.allPassiveActions
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.allInitActions
import dev.bitspittle.racketeer.model.card.allPassiveActions
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem
import dev.bitspittle.racketeer.scripting.types.*
import dev.bitspittle.racketeer.scripting.utils.installGameLogic
import kotlin.coroutines.suspendCoroutine

class GameContext(
    val data: GameData, val env: Environment, val logger: MemoryLogger, val describer: Describer, var state: GameState
)

suspend fun createNewGame(gameData: GameData, handleChoice: (ChoiceContext) -> Unit): GameContext {
    val logger = MemoryLogger()

    val copyableRandom = CopyableRandom()
    val env = Environment()
    env.installDefaults(object : LangService {
        override val random = copyableRandom.invoke()
        override val logger = logger
    })

    // Evaluate global actions found in the gamedata
    Evaluator().let { evaluator ->
        // Safe to call runBlocking at this point because limp defaults all promise not to suspend
        gameData.globalActions.forEach { action ->
            evaluator.evaluate(env, action)
        }
    }

    val exprCache = ExprCache()
    // Compile early to suss out any syntax errors at startup time instead of runtime
    gameData.cards.flatMap { it.allInitActions }.forEach { exprCache.parse(it) }
    gameData.cards.flatMap { it.allPassiveActions }.forEach { exprCache.parse(it) }
    gameData.cards.flatMap { it.playActions }.forEach { exprCache.parse(it) }
    gameData.blueprints.flatMap { it.initActions }.forEach { exprCache.parse(it) }
    gameData.blueprints.flatMap { it.allPassiveActions }.forEach { exprCache.parse(it) }
    gameData.blueprints.flatMap { it.activateActions }.forEach { exprCache.parse(it) }
    gameData.blueprints.map { it.canActivate }.filter { it.isNotBlank() }.forEach { exprCache.parse(it) }
    gameData.initActions.forEach { exprCache.parse(it) }

    val actionQueue = ActionQueue()
    val enqueuers = Enqueuers(
        actionQueue,
        ExprEnqueuerImpl(env, exprCache, actionQueue),
        CardEnqueuerImpl(env, exprCache, actionQueue),
        BuildingEnqueuerImpl(env, exprCache, actionQueue),
    )

    val gameState = MutableGameState(gameData, setOf(Feature.Type.BUILDINGS), enqueuers, copyableRandom)
    val describer = Describer(gameData, showDebugInfo = { true })
    var provideGameState: () -> GameState = { gameState }
    env.installGameLogic(object : GameService {
        override val gameData = gameData
        override val describer = describer
        override val gameState get() = provideGameState()
        override val enqueuers = enqueuers
        override val chooseHandler = object : ChooseHandler {
            override suspend fun query(
                prompt: String?,
                list: List<Any>,
                range: IntRange,
                requiredChoice: Boolean
            ): List<Any>? {
                return suspendCoroutine { continuation ->
                    handleChoice(ChoiceContext(describer, prompt, list, range, requiredChoice, continuation))
                }
            }
        }
        override val logger = logger
    })

    gameState.recordChanges {
        gameState.apply(GameStateChange.GameStart())
        enqueuers.expr.enqueue(gameState, gameData.initActions)
        enqueuers.actionQueue.runEnqueuedActions()
        gameState.apply(GameStateChange.Draw())
    }

    return GameContext(gameData, env, logger, describer, gameState)
        .also { provideGameState = { it.state } }
}

suspend fun GameContext.runStateChangingAction(block: suspend GameContext.() -> Unit): Boolean {
    val prevState = state
    val nextState = prevState.copy()

    env.scoped {
        try {
            state = nextState
            if (state.recordChanges {
                    block()
                    state.onBoardChanged()
                }) {
                state.history.last().toSummaryText(describer, state, prevState.history.lastOrNull())
                    ?.let { summaryText ->
                        logger.info(summaryText)
                    }
            }

            // TODO: Add user stats for web, backed by local storage or database?
            // Update user stats based on new history
//            state.history.last().items.forEach { change ->
//                when (change) {
//                    is GameStateChange.MoveCard -> {
//                        if (prevState.pileFor(change.card) == null) {
//                            userStats.cards.notifyOwnership(change.card)
//                        }
//                    }
//                    is GameStateChange.MoveCards -> {
//                        change.cards.values.flatten().forEach { card ->
//                            if (prevState.pileFor(card) == null) {
//                                userStats.cards.notifyOwnership(card)
//                            }
//                        }
//                    }
//                    is GameStateChange.Build -> {
//                        userStats.buildings.notifyBuilt(change.blueprint)
//                    }
//                    else -> Unit // Doesn't affect user stats
//                }
//            }

        } catch (ex: Exception) {
            state = prevState
            if (ex !is EvaluationException || ex.cause !is CancelPlayException) {
                throw ex
            }
        }
    }

    return (state === nextState)
}
