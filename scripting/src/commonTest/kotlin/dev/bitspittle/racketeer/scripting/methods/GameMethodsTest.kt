package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.math.PowMethod
import dev.bitspittle.racketeer.scripting.TestGameService
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

        assertThat(service.gameState.vp).isEqualTo(0)
        evaluator.evaluate(env, "game-set! 'vp 5")
        assertThat(service.gameState.vp).isEqualTo(5)

        // Negative numbers are clamped
        assertThat(service.gameState.cash).isEqualTo(3)
        evaluator.evaluate(env, "game-set! 'cash -5")
        assertThat(service.gameState.cash).isEqualTo(0)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "game-set! 'invalid-label 2")
        }
    }

    @Test
    fun testGameGetMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        env.addMethod(GameGetMethod(service::gameState))

        val evaluator = Evaluator()

        service.gameState.cash = 1
        service.gameState.influence = 2
        service.gameState.vp = 3

        assertThat(evaluator.evaluate(env, "game-get 'cash")).isEqualTo(1)
        assertThat(evaluator.evaluate(env, "game-get 'influence")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "game-get 'vp")).isEqualTo(3)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "game-get 'invalid-label 2")
        }
    }
}