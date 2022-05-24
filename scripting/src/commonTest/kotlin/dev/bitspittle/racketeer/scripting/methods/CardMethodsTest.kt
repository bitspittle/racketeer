package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.math.MulMethod
import dev.bitspittle.limp.methods.system.DbgMethod
import dev.bitspittle.limp.methods.system.RunMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.getOwnedCards
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.scripting.TestCard
import dev.bitspittle.racketeer.scripting.TestEnqueuers
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.card.*
import dev.bitspittle.racketeer.scripting.methods.pile.PileCopyToMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class CardMethodsTest {
    @Test
    fun testCardSetMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        env.addMethod(CardSetMethod(service::gameState))
        env.addMethod(MulMethod())
        env.addMethod(ListMethod())

        val card = TestCard("test-card")
        val card2 = TestCard("test-card2")
        env.storeValue("card", card)
        env.storeValue("card2", card2)

        val evaluator = Evaluator()
        assertThat(card.vpBase).isEqualTo(0)
        evaluator.evaluate(env, "card-set! card 'vp 3")
        assertThat(card.vpBase).isEqualTo(3)

        evaluator.evaluate(env, "card-set! card 'vp '(* \$it \$it)")
        assertThat(card.vpBase).isEqualTo(9)

        // Negative numbers are clamped
        evaluator.evaluate(env, "card-set! card 'vp -5")
        assertThat(card.vpBase).isEqualTo(0)

        // Can set multiple cards at the same time
        evaluator.evaluate(env, "card-set! (list card card2) 'vp 9")
        assertThat(card.vpBase).isEqualTo(9)
        assertThat(card2.vpBase).isEqualTo(9)

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

        val card = TestCard(
            "test-card",
            listOf("type-a", "type-b"),
            cost = 2,
            vp = 5
        )
        env.storeValue("card", card)

        assertThat(evaluator.evaluate(env, "card-get card 'cost")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "card-get card 'vp")).isEqualTo(5)
        assertThat(evaluator.evaluate(env, "card-get card 'name")).isEqualTo("test-card")
        assertThat(evaluator.evaluate(env, "card-get card 'id")).isInstanceOf<String>()
        assertThat(evaluator.evaluate(env, "card-get card 'types") as List<String>).containsExactly("type-a", "type-b").inOrder()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "card-get card 'invalid-label")
        }
    }

    @Test
    fun testRemoveMethod() = runTest {
        val env = Environment()
        val service = TestGameService(CopyableRandom(123))

        val gameState = service.gameState
        env.addMethod(CardRemoveMethod { gameState })
        env.addMethod(SizeMethod())
        env.addMethod(TakeMethod(service::random))
        env.addMethod(ListGetMethod())

        gameState.apply(GameStateChange.Draw(4))
        val evaluator = Evaluator()

        val ownedCount = env.scoped {
            env.storeValue("\$owned-cards", gameState.getOwnedCards().toList())
            evaluator.evaluate(env, "size \$owned-cards") as Int
        }

        env.scoped {
            env.storeValue("\$owned-cards", gameState.getOwnedCards().toList())
            evaluator.evaluate(env, "card-remove! take \$owned-cards 2")
        }

        val ownedCountRemoveMultipleCards = env.scoped {
            env.storeValue("\$owned-cards", gameState.getOwnedCards().toList())
            evaluator.evaluate(env, "size \$owned-cards") as Int
        }

        assertThat(ownedCount - 2).isEqualTo(ownedCountRemoveMultipleCards)

        env.scoped {
            env.storeValue("\$owned-cards", gameState.getOwnedCards().toList())
            evaluator.evaluate(env, "card-remove! list-get \$owned-cards 0")
        }

        val ownedCountRemoveSingleCard = env.scoped {
            env.storeValue("\$owned-cards", gameState.getOwnedCards().toList())
            evaluator.evaluate(env, "size \$owned-cards") as Int
        }

        assertThat(ownedCountRemoveMultipleCards - 1).isEqualTo(ownedCountRemoveSingleCard)
    }

    @Test
    fun testUpgradeMethods() = runTest {
        val env = Environment()
        val service = TestGameService()

        val gameState = service.gameState
        env.addMethod(CardUpgradeMethod { gameState })
        env.addMethod(CardUpgradesMethod())
        env.addMethod(CardHasUpgradeMethod())
        env.addMethod(SetMethod(service.logger))
        env.addMethod(ListGetMethod())
        env.addMethod(DropMethod(service::random))
        env.addMethod(TakeMethod(service::random))
        env.addMethod(CountMethod())
        env.addConverter(PileToCardsConverter())

        val evaluator = Evaluator()
        env.storeValue("\$deck", gameState.deck)

        evaluator.evaluate(env, "set '\$card list-get \$deck 0")
        evaluator.evaluate(env, "set '\$cards take (drop \$deck 1) 2")
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'cash") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'influence") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'luck") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'veteran") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-upgrades \$card") as List<UpgradeType>).isEmpty()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "card-has-upgrade? \$card 'invalid-property")
        }

        evaluator.evaluate(env, "card-upgrade! \$card 'influence")
        evaluator.evaluate(env, "card-upgrade! \$card 'veteran")
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'cash") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'influence") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'luck") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-upgrade? \$card 'veteran") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "card-upgrades \$card") as List<UpgradeType>)
            .containsExactly(UpgradeType.INFLUENCE, UpgradeType.VETERAN)

        assertThat(evaluator.evaluate(env, "count \$cards '(card-has-upgrade? \$it 'cash)")).isEqualTo(0)
        assertThat(evaluator.evaluate(env, "count \$cards '(card-has-upgrade? \$it 'influence)")).isEqualTo(0)
        evaluator.evaluate(env, "card-upgrade! \$cards 'cash")
        assertThat(evaluator.evaluate(env, "count \$cards '(card-has-upgrade? \$it 'cash)")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "count \$cards '(card-has-upgrade? \$it 'influence)")).isEqualTo(0)
    }

    @Test
    fun testHasTypeMethod() = runTest {
        val env = Environment()
        val service = TestGameService()

        val gameState = service.gameState
        env.addMethod(CardHasTypeMethod(service.gameData.cardTypes))
        env.addMethod(SingleMethod())
        env.addMethod(EqualsMethod())
        env.addMethod(CardGetMethod())
        env.addMethod(ListGetMethod())
        env.addMethod(PileCopyToMethod { gameState })
        env.addMethod(SetMethod(service.logger))
        env.addConverter(PileToCardsConverter())

        val evaluator = Evaluator()
        env.storeValue("\$all-cards", service.gameData.cards)
        env.storeValue("\$hand", gameState.hand)

        // Can check types from cards even if they're not instantiated yet
        assertThat(evaluator.evaluate(env, "card-has-type? list-get \$all-cards 0 'thief") as Boolean).isTrue()

        evaluator.evaluate(env, "pile-copy-to! \$hand single --matching '(= card-get \$it 'name \"Embezzler\") \$all-cards")
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
        assertThat(evaluator.evaluate(env, "card-has-type? \$card 'thief") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "card-has-type? \$card 'spy") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "card-has-type? \$card 'action") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-type? \$card 'treasure") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "card-has-type? \$card 'legend") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "card-get \$card 'types") as List<String>)
            .containsExactly("thief", "spy")
            .inOrder()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "card-has-type? \$card 'invalid-type")
        }
    }

    @Test
    fun testTriggerMethod() = runTest {
        val env = Environment()


        val service = TestGameService(enqueuers = TestEnqueuers(env))

        env.addMethod(CardTriggerMethod(service.enqueuers.card, service::gameState))
        env.addMethod(DbgMethod(service.logger))
        env.addMethod(RunMethod())
        env.addMethod(CardGetMethod())

        // card-trigger! enqueues a card to be played later, so all actions in the current card finish first
        val card1 = TestCard(
            "Card #1",
            playActions = listOf("card-trigger! \$card2", "dbg card-get \$this 'name")
        )
        val card2 = TestCard(
            "Card #2",
            playActions = listOf("card-trigger! \$card3", "dbg card-get \$this 'name")
        )
        val card3 = TestCard(
            "Card #3",
            playActions = listOf("dbg card-get \$this 'name")
        )
        env.storeValue("\$card1", card1)
        env.storeValue("\$card2", card2)
        env.storeValue("\$card3", card3)

        assertThat(service.logs).isEmpty()
        service.enqueuers.apply {
            card.enqueuePlayActions(service.gameState, card1)
            actionQueue.runEnqueuedActions()
        }
        assertThat(service.logs).containsExactly(
            "[D] Card #1 # String",
            "[D] Card #2 # String",
            "[D] Card #3 # String"
        ).inOrder()
    }
}