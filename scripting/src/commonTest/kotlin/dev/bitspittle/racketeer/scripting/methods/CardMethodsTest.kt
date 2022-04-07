package dev.bitspittle.racketeer.scripting.methods

import com.benasher44.uuid.Uuid
import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListGetMethod
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.collection.SizeMethod
import dev.bitspittle.limp.methods.collection.TakeMethod
import dev.bitspittle.limp.methods.math.MulMethod
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.addVariablesInto
import dev.bitspittle.racketeer.scripting.methods.card.CardGetMethod
import dev.bitspittle.racketeer.scripting.methods.card.CardSetMethod
import dev.bitspittle.racketeer.scripting.methods.card.RemoveMethod
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

class CardMethodsTest {
    @Test
    fun testCardSetMethod() = runTest {
        val env = Environment()
        env.addMethod(CardSetMethod())
        env.addMethod(MulMethod())
        env.addMethod(ListMethod())

        val card = CardTemplate("test-card", "", listOf()).instantiate()
        val card2 = CardTemplate("test-card2", "", listOf()).instantiate()
        env.storeValue("card", card)
        env.storeValue("card2", card2)

        val evaluator = Evaluator()
        assertThat(card.vp).isEqualTo(0)
        evaluator.evaluate(env, "card-set! card 'vp 3")
        assertThat(card.vp).isEqualTo(3)

        evaluator.evaluate(env, "card-set! card 'vp '(* \$it \$it)")
        assertThat(card.vp).isEqualTo(9)

        // Negative numbers are clamped
        evaluator.evaluate(env, "card-set! card 'vp -5")
        assertThat(card.vp).isEqualTo(0)

        // Can set multiple cards at the same time
        evaluator.evaluate(env, "card-set! (list card card2) 'vp 9")
        assertThat(card.vp).isEqualTo(9)
        assertThat(card2.vp).isEqualTo(9)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "card-set! 'invalid-property 1")
        }

        // Can't set readonly properties
        run {
            assertThrows<EvaluationException> {
                evaluator.evaluate(env, "card-set! 'name \"name-is-read-only\"")
            }
            assertThrows<EvaluationException> {
                evaluator.evaluate(env, "card-set! 'cost 0")
            }
        }

        // Amount must be numerical
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "card-set! 'vp \"amount-must-be-a-number\"")
        }
    }

    @Test
    fun testCardGetMethod() = runTest {
        val env = Environment()
        env.addMethod(CardGetMethod())

        val evaluator = Evaluator()

        val card = CardTemplate("test-card", "", listOf(), cost = 2, vp = 5).instantiate()
        env.storeValue("card", card)

        assertThat(evaluator.evaluate(env, "card-get card 'cost")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "card-get card 'vp")).isEqualTo(5)
        assertThat(evaluator.evaluate(env, "card-get card 'name")).isEqualTo("test-card")
        assertThat(evaluator.evaluate(env, "card-get card 'id")).isInstanceOf<Uuid>()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "card-get card 'invalid-label")
        }
    }

    @Test
    fun testRemoveMethod() = runTest {
        val env = Environment()
        val service = TestGameService(Random(123))

        val gameState = service.gameState
        env.addMethod(RemoveMethod { gameState })
        env.addMethod(SizeMethod())
        env.addMethod(TakeMethod(service.random))
        env.addMethod(ListGetMethod())

        gameState.draw(4)
        val evaluator = Evaluator()

        val ownedCount = env.scoped {
            gameState.addVariablesInto(this)
            evaluator.evaluate(env, "size \$owned") as Int
        }

        env.scoped {
            gameState.addVariablesInto(this)
            evaluator.evaluate(env, "remove! take \$owned 2")
        }

        val ownedCountRemoveMultipleCards = env.scoped {
            gameState.addVariablesInto(this)
            evaluator.evaluate(env, "size \$owned") as Int
        }

        assertThat(ownedCount - 2).isEqualTo(ownedCountRemoveMultipleCards)

        env.scoped {
            gameState.addVariablesInto(this)
            evaluator.evaluate(env, "remove! list-get \$owned 0")
        }

        val ownedCountRemoveSingleCard = env.scoped {
            gameState.addVariablesInto(this)
            evaluator.evaluate(env, "size \$owned") as Int
        }

        assertThat(ownedCountRemoveMultipleCards - 1).isEqualTo(ownedCountRemoveSingleCard)
    }
}