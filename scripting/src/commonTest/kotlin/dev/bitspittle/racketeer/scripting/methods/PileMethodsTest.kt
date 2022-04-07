package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.methods.collection.FirstMethod
import dev.bitspittle.limp.methods.collection.TakeMethod
import dev.bitspittle.limp.types.Placeholder
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.addVariablesInto
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.pile.CopyToMethod
import dev.bitspittle.racketeer.scripting.methods.pile.MoveToMethod
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

class PileMethodsTest {
    @Test
    fun testCopyToMethod() = runTest {
        val env = Environment()
        val service = TestGameService(Random(2)) // Seed chosen so deck and hand cards are interesting
        val gameState = service.gameState
        env.addMethod(CopyToMethod { gameState })
        env.addMethod(TakeMethod(service.random))
        env.addMethod(FirstMethod())
        env.addConverter(PileToCardsConverter())
        env.storeValue("_", Placeholder)

        // Make sure we can clone card templates and not just cards
        env.storeValue("\$card-template", service.gameData.cards[3].also { cardTemplate ->
            assertThat(cardTemplate.name).isEqualTo("Squealer")
        })

        // Make sure we can clone card templates and not just cards
        env.storeValue("\$card-templates", listOf(service.gameData.cards[4], service.gameData.cards[5]).also { cardTemplates ->
            assertThat(cardTemplates.map { it.name }).containsExactly("Fool's Gold", "Croupier").inOrder()
        })

        gameState.draw(4)
        gameState.addVariablesInto(env)

        assertThat(gameState.discard.cards).isEmpty()
        assertThat(gameState.deck.cards).isNotEmpty()
        val deckSize = gameState.deck.cards.size

        assertThat(gameState.deck.cards.take(2).map { it.template.name })
            .containsExactly("Rumormonger", "Pickpocket")
            .inOrder()

        val evaluator = Evaluator()

        // Copy-to from the deck, testing copying a list of cards
        run {
            evaluator.evaluate(env, "copy-to! \$discard take \$deck 2")

            assertThat(gameState.discard.cards).hasSize(2)
            assertThat(gameState.deck.cards).hasSize(deckSize) // Deck pile not affected

            // The cards are cloned, but they are new copies with different IDs
            assertThat(gameState.discard.cards.map { it.template.name })
                .containsExactly("Rumormonger", "Pickpocket")
                .inOrder()
            assertThat(gameState.deck.cards[0].id).isNotEqualTo(gameState.discard.cards[0].id)
        }

        // Copy-to from the hand, testing copying a single card (and a different insert strategy)
        run {
            assertThat(gameState.hand.cards).isNotEmpty()
            val handSize = gameState.hand.cards.size
            assertThat(gameState.hand.cards[0].template.name).isEqualTo("Pickpocket")

            evaluator.evaluate(env, "copy-to! --pos 'front \$discard first \$hand _")

            assertThat(gameState.discard.cards).hasSize(3)
            assertThat(gameState.hand.cards).hasSize(handSize) // Hand not affected

            assertThat(gameState.discard.cards[0].template.name).isEqualTo("Pickpocket")
        }

        // Copy-to from card templates
        run {
            evaluator.evaluate(env, "copy-to! \$discard \$card-template")
            assertThat(gameState.discard.cards).hasSize(4)

            evaluator.evaluate(env, "copy-to! --pos 'random \$discard \$card-templates")
            assertThat(gameState.discard.cards).hasSize(6)
        }

        assertThat(gameState.discard.cards.map { it.template.name })
            .containsExactly("Fool's Gold", "Pickpocket", "Rumormonger", "Pickpocket", "Squealer", "Croupier")
            .inOrder()
    }

    @Test
    fun testMoveToMethod() = runTest {
        val env = Environment()
        val service = TestGameService(Random(2)) // Seed chosen so deck and hand cards are interesting
        val gameState = service.gameState
        env.addMethod(MoveToMethod { gameState })
        env.addMethod(TakeMethod(service.random))
        env.addMethod(FirstMethod())
        env.addConverter(PileToCardsConverter())
        env.storeValue("_", Placeholder)

        gameState.draw(4)
        gameState.addVariablesInto(env)

        assertThat(gameState.discard.cards).isEmpty()
        assertThat(gameState.deck.cards).isNotEmpty()
        val deckSize = gameState.deck.cards.size
        val handSize = gameState.hand.cards.size

        assertThat(gameState.deck.cards.take(2).map { it.template.name })
            .containsExactly("Rumormonger", "Pickpocket")
            .inOrder()

        val evaluator = Evaluator()

        // move a list of cards to a pile
        evaluator.evaluate(env, "move-to! \$discard take \$deck 2")

        assertThat(gameState.discard.cards).hasSize(2)
        assertThat(gameState.deck.cards).hasSize(deckSize - 2) // Deck size affected

        assertThat(gameState.discard.cards.map { it.template.name })
            .containsExactly("Rumormonger", "Pickpocket")
            .inOrder()

        // move a pile entirely to another
        evaluator.evaluate(env, "move-to! \$hand \$discard")
        assertThat(gameState.discard.cards).hasSize(0)
        assertThat(gameState.hand.cards).hasSize(handSize + 2) // Deck size affected

        // move a single card
        evaluator.evaluate(env, "move-to! \$discard first \$hand _")
        assertThat(gameState.discard.cards).hasSize(1)
    }
}