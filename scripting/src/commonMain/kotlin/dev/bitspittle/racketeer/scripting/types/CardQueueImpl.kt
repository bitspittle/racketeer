package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.card.allPassiveActions
import dev.bitspittle.racketeer.model.game.GameState

class CardQueueImpl(private val env: Environment) : CardQueue {
    private val cardsToRun = mutableMapOf<Card, MutableList<Expr>>()
    private var isRunning = false

    private val compiledActions: MutableMap<String, Expr> = mutableMapOf()

    private fun enqueueScopedExprs(card: Card, exprs: List<Expr>) {
        if (exprs.isNotEmpty()) {
            cardsToRun[card] = exprs.toMutableList()
        }
    }

    private fun enqueueScopedActions(card: Card, actions: List<String>) {
        if (actions.isNotEmpty()) {
            enqueueScopedExprs(card, actions.map { action -> compiledActions.getOrPut(action) { Expr.parse(action) } })
        }
    }

    override fun enqueueInitActions(card: Card) {
        enqueueScopedActions(card, card.template.initActions)
    }

    override fun enqueuePlayActions(card: Card) {
        enqueueScopedActions(card, card.template.playActions)
    }

    override fun enqueuePassiveActions(card: Card) {
        enqueueScopedActions(card, card.template.allPassiveActions)
    }

    override fun clear() = cardsToRun.clear()

    private suspend fun run(gameState: GameState, card: Card, actionsToRun: MutableList<Expr>) {
        val evaluator = Evaluator()
        env.scoped {
            gameState.addVariablesInto(env)
            card.addVariableTo(this)
            while (actionsToRun.isNotEmpty()) {
                val actionToRun = actionsToRun.removeFirst()
                evaluator.evaluate(env, actionToRun)
            }
        }
    }

    override suspend fun runEnqueuedActions(gameState: GameState) {
        if (isRunning) return
        isRunning = true
        try {
            while (cardsToRun.isNotEmpty()) {
                val card = cardsToRun.keys.first()
                run(gameState, card, cardsToRun.remove(card)!!)
            }
        }
        finally {
            cardsToRun.clear()
            isRunning = false
        }
    }

    /**
     * Add all variables related to the current game state into the environment.
     *
     * You probably want to do this within an [Environment.scoped] block, to avoid ever accidentally referring to stale game
     * state from previous turns.
     */
    fun GameState.addVariablesInto(env: Environment) {
        env.storeValue("\$all-cards", allCards)
        env.storeValue("\$shop-tier", shop.tier)

        env.storeValue("\$deck", deck)
        env.storeValue("\$hand", hand)
        env.storeValue("\$street", street)
        env.storeValue("\$discard", discard)
        env.storeValue("\$jail", jail)

        env.storeValue("\$shop", shop.stock.filterNotNull())

        env.addMethod(object : Method("\$owned", 0) {
            override suspend fun invoke(
                env: Environment,
                eval: Evaluator,
                params: List<Any>,
                options: Map<String, Any>,
                rest: List<Any>
            ): Any {
                return getOwnedCards()
            }
        })
    }

    /**
     * Store the current card in the environment.
     *
     * You probably want to do this within an [Environment.scoped] block, tied to the lifetime of the current card being
     * played.
     */
    fun Card.addVariableTo(env: Environment) {
        env.storeValue("\$this", this)
    }
}