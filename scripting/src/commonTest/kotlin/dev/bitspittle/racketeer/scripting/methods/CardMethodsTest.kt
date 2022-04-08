package dev.bitspittle.racketeer.scripting.methods

import com.benasher44.uuid.Uuid
import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.math.MulMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.addVariablesInto
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.card.*
import dev.bitspittle.racketeer.scripting.methods.pile.CopyToMethod
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
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

        val card = CardTemplate("test-card", "", listOf("type-a", "type-b"), cost = 2, vp = 5).instantiate()
        env.storeValue("card", card)

        assertThat(evaluator.evaluate(env, "card-get card 'cost")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "card-get card 'vp")).isEqualTo(5)
        assertThat(evaluator.evaluate(env, "card-get card 'name")).isEqualTo("test-card")
        assertThat(evaluator.evaluate(env, "card-get card 'id")).isInstanceOf<Uuid>()
        assertThat(evaluator.evaluate(env, "card-get card 'types") as List<String>).containsExactly("type-a", "type-b").inOrder()

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

    @Test
    fun testUpgradeMethods() = runTest {
        val env = Environment()
        val service = TestGameService()

        val gameState = service.gameState
        env.addMethod(UpgradeMethod())
        env.addMethod(HasUpgradeMethod())
        env.addMethod(SetMethod())
        env.addMethod(ListGetMethod())
        env.addMethod(DropMethod(service.random))
        env.addMethod(TakeMethod(service.random))
        env.addMethod(CountMethod())
        env.addConverter(PileToCardsConverter())

        val evaluator = Evaluator()
        gameState.addVariablesInto(env)
        evaluator.evaluate(env, "set '\$card list-get \$deck 0")
        evaluator.evaluate(env, "set '\$cards take (drop \$deck 1) 2")
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'cash") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'influence") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'luck") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'patience") as Boolean).isFalse()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "has-upgrade? \$card 'invalid-property")
        }

        evaluator.evaluate(env, "upgrade! \$card 'influence")
        evaluator.evaluate(env, "upgrade! \$card 'patience")
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'cash") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'influence") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'luck") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-upgrade? \$card 'patience") as Boolean).isTrue()

        assertThat(evaluator.evaluate(env, "count \$cards '(has-upgrade? \$it 'cash)")).isEqualTo(0)
        assertThat(evaluator.evaluate(env, "count \$cards '(has-upgrade? \$it 'influence)")).isEqualTo(0)
        evaluator.evaluate(env, "upgrade! \$cards 'cash")
        assertThat(evaluator.evaluate(env, "count \$cards '(has-upgrade? \$it 'cash)")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "count \$cards '(has-upgrade? \$it 'influence)")).isEqualTo(0)
    }

    @Test
    fun testHasTypeMethod() = runTest {
        val env = Environment()
        val service = TestGameService()

        val gameState = service.gameState
        env.addMethod(HasTypeMethod(service.gameData.cardTypes))
        env.addMethod(SingleMethod())
        env.addMethod(EqualsMethod())
        env.addMethod(CardGetMethod())
        env.addMethod(ListGetMethod())
        env.addMethod(CopyToMethod { gameState })
        env.addMethod(SetMethod())
        env.addConverter(PileToCardsConverter())

        val evaluator = Evaluator()
        gameState.addVariablesInto(env)
        evaluator.evaluate(env, "copy-to! \$hand single \$all-cards '(= card-get \$it 'name \"Roving Gambler\")")
        evaluator.evaluate(env, "set '\$card list-get \$hand 0")
        val card = evaluator.evaluate(env, "\$card") as Card
        assertThat(card.template.types).containsExactly("thief", "spy").inOrder()

        // From TestGameService:
        // cardTypes:
        //      - Action
        //      - Treasure
        //      - Thief
        //      - Spy
        //      - Legend
        assertThat(evaluator.evaluate(env, "has-type? \$card 'thief") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "has-type? \$card 'spy") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "has-type? \$card 'action") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-type? \$card 'treasure") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "has-type? \$card 'legend") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "card-get \$card 'types") as List<String>)
            .containsExactly("thief", "spy")
            .inOrder()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "has-type? \$card 'invalid-type")
        }
    }
}