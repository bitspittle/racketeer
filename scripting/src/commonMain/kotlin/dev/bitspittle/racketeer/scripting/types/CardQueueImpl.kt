package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.scripting.utils.addVariableTo

class CardQueueImpl(private val env: Environment) : CardQueue {
    private val cardsToRun = mutableMapOf<Card, MutableList<Expr>>()
    private var isRunning = false

    private val compiledActions: MutableMap<String, Expr> = mutableMapOf()

    private fun enqueueScopedExprs(card: Card, exprs: List<Expr>) {
        cardsToRun[card] = exprs.toMutableList()
    }

    private fun enqueueScopedActions(card: Card, actions: List<String>) {
        enqueueScopedExprs(card, actions.map { action -> compiledActions.getOrPut(action) { Expr.parse(action) } })
    }

    override fun enqueueInitActions(card: Card) {
        TODO("Not yet implemented")
    }

    override fun enqueuePlayActions(card: Card) {
        enqueueScopedActions(card, card.template.playActions)
    }

    override fun enqueuePassiveActions(card: Card) {
        TODO("Not yet implemented")
    }

    override fun clear() = cardsToRun.clear()

    private suspend fun run(card: Card, actionsToRun: MutableList<Expr>) {
        val evaluator = Evaluator()
        env.scoped {
            card.addVariableTo(this)
            while (actionsToRun.isNotEmpty()) {
                val actionToRun = actionsToRun.removeFirst()
                evaluator.evaluate(env, actionToRun)
            }
        }
    }

    override suspend fun start() {
        if (isRunning) throw IllegalStateException("Attempting to start an action queue that's already running")
        isRunning = true
        try {
            while (cardsToRun.isNotEmpty()) {
                val card = cardsToRun.keys.first()
                run(card, cardsToRun.remove(card)!!)
            }
        }
        finally {
            isRunning = false
        }
    }
}