package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.methods.collection.FirstMethod
import dev.bitspittle.limp.methods.collection.TakeMethod
import dev.bitspittle.limp.types.Placeholder
import dev.bitspittle.racketeer.model.game.GameStateDelta
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.pile.PileCopyToMethod
import dev.bitspittle.racketeer.scripting.methods.pile.PileMoveToMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PileMethodsTest {
    @Test
    fun testCopyToMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        val gameState = service.gameState
        env.addMethod(PileCopyToMethod { gameState })
        env.addMethod(TakeMethod(service::random))
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

        gameState.apply(GameStateDelta.Draw(4))
        env.storeValue("\$discard", gameState.discard)
        env.storeValue("\$deck", gameState.deck)
        env.storeValue("\$hand", gameState.hand)

        assertThat(gameState.discard.cards).isEmpty()
        assertThat(gameState.deck.cards).isNotEmpty()
        val deckSize = gameState.deck.cards.size

        val cardsToCopy = gameState.deck.cards.take(2)

        val evaluator = Evaluator()

        // Copy-to from the deck, testing copying a list of cards
        run {
            evaluator.evaluate(env, "pile-copy-to! \$discard take \$deck 2")

            assertThat(gameState.discard.cards).hasSize(2)
            assertThat(gameState.deck.cards).hasSize(deckSize) // Deck pile not affected

            // The cards are cloned, but they are new copies with different IDs
            assertThat(gameState.discard.cards.map { it.template.name }).containsExactly(cardsToCopy.map { it.template.name }).inOrder()
            assertThat(gameState.deck.cards[0].id).isNotEqualTo(gameState.discard.cards[0].id)
        }

        // Copy-to from the hand, testing copying a single card (and a different insert strategy)
        run {
            assertThat(gameState.hand.cards).isNotEmpty()
            val handSize = gameState.hand.cards.size
            assertThat(gameState.hand.cards[0].template.name).isEqualTo("Pickpocket")

            evaluator.evaluate(env, "pile-copy-to! --pos 'front \$discard first \$hand _")

            assertThat(gameState.discard.cards).hasSize(3)
            assertThat(gameState.hand.cards).hasSize(handSize) // Hand not affected

            assertThat(gameState.discard.cards[0].template.name).isEqualTo("Pickpocket")
        }

        // Copy-to from card templates
        run {
            evaluator.evaluate(env, "pile-copy-to! \$discard \$card-template")
            assertThat(gameState.discard.cards).hasSize(4)

            evaluator.evaluate(env, "pile-copy-to! --pos 'random \$discard \$card-templates")
            assertThat(gameState.discard.cards).hasSize(6)
        }
    }

    @Test
    fun testMoveToMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        val gameState = service.gameState
        env.addMethod(PileMoveToMethod { gameState })
        env.addMethod(TakeMethod(service::random))
        env.addMethod(FirstMethod())
        env.addConverter(PileToCardsConverter())
        env.storeValue("_", Placeholder)

        gameState.apply(GameStateDelta.Draw(4))
        env.storeValue("\$discard", gameState.discard)
        env.storeValue("\$deck", gameState.deck)
        env.storeValue("\$hand", gameState.hand)

        assertThat(gameState.discard.cards).isEmpty()
        assertThat(gameState.deck.cards).isNotEmpty()
        val deckSize = gameState.deck.cards.size
        val handSize = gameState.hand.cards.size

        val cardsToMove = gameState.deck.cards.take(2)

        val evaluator = Evaluator()

        // move a list of cards to a pile
        evaluator.evaluate(env, "pile-move-to! \$discard take \$deck 2")

        assertThat(gameState.discard.cards).hasSize(2)
        assertThat(gameState.deck.cards).hasSize(deckSize - 2) // Deck size affected

        assertThat(gameState.discard.cards.map { it.template.name }).containsExactly(cardsToMove.map { it.template.name })
            .inOrder()

        // move a pile entirely to another
        evaluator.evaluate(env, "pile-move-to! \$hand \$discard")
        assertThat(gameState.discard.cards).hasSize(0)
        assertThat(gameState.hand.cards).hasSize(handSize + 2) // Deck size affected

        // move a single card
        evaluator.evaluate(env, "pile-move-to! \$discard first \$hand _")
        assertThat(gameState.discard.cards).hasSize(1)
    }
}