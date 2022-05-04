package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.card.allPassiveActions
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.scripting.utils.addVariableTo
import dev.bitspittle.racketeer.scripting.utils.addVariablesInto

class CardQueueImpl(private val env: Environment) : CardQueue {
    private val actionsToRun = mutableListOf<Expr>()
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

    override fun clear() {
        actionsToRun.clear()
        cardsToRun.clear()
    }

    private suspend fun run(gameState: MutableGameState, card: Card) {
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

    override suspend fun runEnqueuedActions(gameState: MutableGameState) {
        if (isRunning) return
        isRunning = true
        try {
            while (cardsToRun.isNotEmpty()) {
                val card = cardsToRun.keys.first()
                actionsToRun.addAll(cardsToRun.remove(card)!!)
                run(gameState, card)
            }
        }
        finally {
            cardsToRun.clear()
            actionsToRun.clear()
            isRunning = false
        }
    }
}