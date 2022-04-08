package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.methods.collection.SingleMethod
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.math.AddMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.racketeer.model.action.ActionRunner
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.addVariablesInto
import dev.bitspittle.racketeer.scripting.methods.card.CardGetMethod
import dev.bitspittle.racketeer.scripting.methods.card.CardSetMethod
import dev.bitspittle.racketeer.scripting.methods.effect.FxAddMethod
import dev.bitspittle.racketeer.scripting.methods.game.GameSetMethod
import dev.bitspittle.racketeer.scripting.methods.pile.CopyToMethod
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

class EffectMethodsTest {
    @Test
    fun testFxAddMethod() = runTest {
        val env = Environment()
        val actionRunner = ActionRunner(env)
        val service = TestGameService(getActionQueue = { actionRunner.actionQueue })
        val gameState = service.gameState
        env.addMethod(FxAddMethod { gameState })
        env.addMethod(GameSetMethod { gameState })
        env.addMethod(CopyToMethod { gameState })
        env.addMethod(AddMethod())
        env.addMethod(EqualsMethod())
        env.addMethod(SetMethod())
        env.addMethod(CardSetMethod())
        env.addMethod(CardGetMethod())
        env.addMethod(SingleMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "fx-add! '(card-set! \$card 'vp '(+ \$it 2))"))
        assertThat(evaluator.evaluate(env, "fx-add! '(game-set! 'influence '(+ \$it 3))"))

        gameState.draw()
        var expectedHandSize = gameState.handSize
        assertThat(gameState.hand.cards.size).isEqualTo(expectedHandSize)

        env.scoped {
            gameState.addVariablesInto(this)
            // Create a card which has a "install effect which adds one cash to the game per card played
            evaluator.evaluate(env, "copy-to! --pos 'front \$hand single \$all-cards '(= card-get \$it 'name \"Embezzler\")")
            ++expectedHandSize
        }
        assertThat(gameState.hand.cards.size).isEqualTo(expectedHandSize)
        assertThat(gameState.hand.cards.take(3).map { it.template.name })
            .containsExactly(
                "Embezzler", // Installs an effect
                "Rumormonger",
                "Pickpocket"
            ).inOrder()
        val card1 = gameState.hand.cards[0]
        val card2 = gameState.hand.cards[1]
        val card3 = gameState.hand.cards[2]

        assertThat(gameState.cash).isEqualTo(0)
        assertThat(gameState.influence).isEqualTo(0)
        assertThat(card1.vp).isEqualTo(0)
        assertThat(card2.vp).isEqualTo(0)
        assertThat(card3.vp).isEqualTo(0)

        // First, play the card with an effect. It should install an effect that happens on the NEXT CARD but not
        // itself (adding cash)
        gameState.play(actionRunner, 0); --expectedHandSize
        assertThat(gameState.hand.cards.size).isEqualTo(expectedHandSize)
        assertThat(gameState.cash).isEqualTo(0) // Cash effect just installed but won't start until the next card
        assertThat(gameState.influence).isEqualTo(3) // Already installed effect affects game
        assertThat(card1.vp).isEqualTo(2) // Already installed effect affects this card
        assertThat(card2.vp).isEqualTo(0)
        assertThat(card3.vp).isEqualTo(0)

        gameState.play(actionRunner, 0); --expectedHandSize
        assertThat(gameState.hand.cards.size).isEqualTo(expectedHandSize)
        assertThat(gameState.cash).isEqualTo(1) // Cash effect starts taking effect
        assertThat(gameState.influence).isEqualTo(6)
        assertThat(card1.vp).isEqualTo(2) // Previous card not affected
        assertThat(card2.vp).isEqualTo(2) // Card just played affected
        assertThat(card3.vp).isEqualTo(0)

        gameState.play(actionRunner, 0); --expectedHandSize
        assertThat(gameState.hand.cards.size).isEqualTo(expectedHandSize)
        assertThat(gameState.cash).isEqualTo(2)
        assertThat(gameState.influence).isEqualTo(9)
        assertThat(card1.vp).isEqualTo(2)
        assertThat(card2.vp).isEqualTo(2)
        assertThat(card3.vp).isEqualTo(2)
    }
}