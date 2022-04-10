package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.types.Placeholder
import dev.bitspittle.racketeer.scripting.TestGameService
import dev.bitspittle.racketeer.scripting.methods.card.CardGetMethod
import dev.bitspittle.racketeer.scripting.methods.card.CardHasTypeMethod
import dev.bitspittle.racketeer.scripting.methods.shop.ShopRerollMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ShopMethodsTest {
    @Test
    fun testRerollMethod() = runTest {
        val env = Environment()
        val service = TestGameService()
        val shop = service.gameState.shop
        env.addMethod(ShopRerollMethod { shop })
        env.addMethod(CardHasTypeMethod(service.gameData.cardTypes))
        env.addMethod(EqualsMethod())
        env.addMethod(CardGetMethod())
        env.storeValue("_", Placeholder)

        assertThat(shop.stock.map { it!!.template.name }).containsExactly("Squealer", "Con Artist", "Fool's Gold")
            .inOrder()

        assertThat(shop.stock.map { it!!.template.tier }.all { it <= shop.tier }).isTrue()

        val evaluator = Evaluator()
        evaluator.evaluate(env, "shop-reroll! _")

        assertThat(shop.stock.map { it!!.template.name }).containsExactly("Fool's Gold", "Squealer", "Con Artist")
            .inOrder()

        // Upgrade the shop for access to more cards and card slots
        shop.upgrade()

        assertThat(shop.stock.map { it!!.template.name }).containsExactly(
            "Fool's Gold",
            "Squealer",
            "Con Artist",
            "Embezzler"
        )
            .inOrder()

        evaluator.evaluate(env, "shop-reroll! '(card-has-type? \$card 'spy)")

        assertThat(shop.stock.map { it!!.template.name }).containsExactly(
            "Lady Thistledown",
            "Con Artist",
            "Embezzler",
            "Squealer"
        )
            .inOrder()
        assertThat(shop.stock.map { it!!.template.types }.all { types -> types.contains("spy") }).isTrue()

        evaluator.evaluate(env, "shop-reroll! '(= (card-get \$card 'tier) 1)")

        assertThat(shop.stock.map { it!!.template.name }).containsExactly(
            "Embezzler",
            "Ditch the Goods",
            "Lady Thistledown",
            "Cheese It!"
        ).inOrder()
        assertThat(shop.stock.map { it!!.template.tier }.all { it == 1 }).isTrue()
    }
}