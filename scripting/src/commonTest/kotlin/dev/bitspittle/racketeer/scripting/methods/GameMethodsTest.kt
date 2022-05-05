package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.math.PowMethod
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.game.GameDrawMethod
import dev.bitspittle.racketeer.scripting.methods.game.GameGetMethod
import dev.bitspittle.racketeer.scripting.methods.game.GameSetMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GameMethodsTest {
    @Test
    fun testGameSetMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        env.addMethod(GameSetMethod(service::gameState))
        env.addMethod(PowMethod())

        val evaluator = Evaluator()
        assertThat(service.gameState.cash).isEqualTo(0)
        evaluator.evaluate(env, "game-set! 'cash 3")
        assertThat(service.gameState.cash).isEqualTo(3)

        assertThat(service.gameState.influence).isEqualTo(0)
        evaluator.evaluate(env, "game-set! 'influence 2")
        assertThat(service.gameState.influence).isEqualTo(2)
        evaluator.evaluate(env, "game-set! 'influence '(^ \$it 3)")
        assertThat(service.gameState.influence).isEqualTo(8)

        assertThat(service.gameState.handSize).isEqualTo(4)
        evaluator.evaluate(env, "game-set! 'hand-size 6")
        assertThat(service.gameState.handSize).isEqualTo(6)

        // Negative numbers are clamped
        assertThat(service.gameState.cash).isEqualTo(3)
        evaluator.evaluate(env, "game-set! 'cash -5")
        assertThat(service.gameState.cash).isEqualTo(0)

        assertThrows<EvaluationException> {
            // Game VP is read-only
            evaluator.evaluate(env, "game-set! 'vp 5")
        }

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "game-set! 'invalid-label 2")
        }
    }

    @Test
    fun testGameGetMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        val gameState = service.gameState
        env.addMethod(GameGetMethod(service::gameState))

        val evaluator = Evaluator()

        gameState.cash = 1
        gameState.influence = 2
        // As a side-effect, sets the gamestate's VP to 3
        gameState.move(CardTemplate("Free VP", "", listOf(), tier = 0, vp = 3).instantiate(), gameState.hand)

        assertThat(evaluator.evaluate(env, "game-get 'cash")).isEqualTo(1)
        assertThat(evaluator.evaluate(env, "game-get 'influence")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "game-get 'vp")).isEqualTo(3)
        assertThat(evaluator.evaluate(env, "game-get 'hand-size")).isEqualTo(4)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "game-get 'invalid-label 2")
        }
    }

    @Test
    fun testDrawMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        val gameState = service.gameState
        gameState.numTurns = Int.MAX_VALUE // Don't want to worry about running out; using endTurn to reset discard
        env.addMethod(GameDrawMethod { gameState })

        val evaluator = Evaluator()

        assertThat(gameState.deck.cards).hasSize(8)
        assertThat(gameState.hand.cards).hasSize(0)
        assertThat(gameState.discard.cards).hasSize(0)
        evaluator.evaluate(env, "game-draw! 3")

        assertThat(gameState.deck.cards).hasSize(5)
        assertThat(gameState.hand.cards).hasSize(3)
        assertThat(gameState.discard.cards).hasSize(0)

        gameState.apply(GameStateChange.EndTurn())
        assertThat(gameState.deck.cards).hasSize(5)
        assertThat(gameState.hand.cards).hasSize(0)
        assertThat(gameState.discard.cards).hasSize(3)

        // Emptying the deck shuffles doesn't trigger a discard refill
        evaluator.evaluate(env, "game-draw! 5")
        assertThat(gameState.deck.cards).hasSize(0)
        assertThat(gameState.hand.cards).hasSize(5)
        assertThat(gameState.discard.cards).hasSize(3)

        evaluator.evaluate(env, "game-draw! 1") // Refill triggered
        assertThat(gameState.deck.cards).hasSize(2)
        assertThat(gameState.hand.cards).hasSize(6)
        assertThat(gameState.discard.cards).hasSize(0)

        gameState.apply(GameStateChange.EndTurn())
        assertThat(gameState.deck.cards).hasSize(2)
        assertThat(gameState.hand.cards).hasSize(0)
        assertThat(gameState.discard.cards).hasSize(6)

        // Overdrawing will refill it from the discard pile automatically
        evaluator.evaluate(env, "game-draw! 6")
        assertThat(gameState.deck.cards).hasSize(2)
        assertThat(gameState.hand.cards).hasSize(6)
        assertThat(gameState.discard.cards).hasSize(0)

        gameState.apply(GameStateChange.EndTurn())
        assertThat(gameState.deck.cards).hasSize(2)
        assertThat(gameState.hand.cards).hasSize(0)
        assertThat(gameState.discard.cards).hasSize(6)

        // Draw count gets clamped to what you actually have
        evaluator.evaluate(env, "game-draw! 999")
        assertThat(gameState.deck.cards).hasSize(0)
        assertThat(gameState.hand.cards).hasSize(8)
        assertThat(gameState.discard.cards).hasSize(0)

        gameState.apply(GameStateChange.EndTurn())
        assertThat(gameState.deck.cards).hasSize(0)
        assertThat(gameState.hand.cards).hasSize(0)
        assertThat(gameState.discard.cards).hasSize(8)

        // Negative draw counts are not allowed
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "game-draw! -1")
        }
    }
}