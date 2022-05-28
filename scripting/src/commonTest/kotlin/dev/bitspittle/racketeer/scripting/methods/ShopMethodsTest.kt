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
        val service = TestGameService.create()
        val shop = service.gameState.shop
        env.addMethod(ShopRerollMethod { service.gameState })
        env.addMethod(CardHasTypeMethod(service.gameData.cardTypes))
        env.addMethod(EqualsMethod())
        env.addMethod(CardGetMethod())
        env.storeValue("_", Placeholder)

        val initialStock = shop.stock.filterNotNull()
        assertThat(initialStock).hasSize(3)

        assertThat(shop.stock.map { it!!.template.tier }.all { it <= shop.tier }).isTrue()

        val evaluator = Evaluator()
        evaluator.evaluate(env, "shop-reroll! _")

        val rerolledStock = shop.stock.filterNotNull()
        assertThat(rerolledStock).hasSize(3)
        assertThat(rerolledStock.none { newStock -> initialStock.contains(newStock) }).isTrue()

        // Upgrade the shop for access to more cards and card slots
        shop.upgrade()

        val stockPostUpgrade = shop.stock.filterNotNull()
        assertThat(stockPostUpgrade).hasSize(4)
        assertThat(stockPostUpgrade.take(3)).containsExactly(rerolledStock)

        evaluator.evaluate(env, "shop-reroll! '(card-has-type? \$card 'spy)")

        val spiesOnly = shop.stock.filterNotNull()
        assertThat(stockPostUpgrade).hasSize(4)
        assertThat(spiesOnly.all { types -> types.template.types.contains("spy") }).isTrue()

        evaluator.evaluate(env, "shop-reroll! '(= (card-get \$card 'tier) 1)")

        val tier1Only = shop.stock.filterNotNull()
        assertThat(stockPostUpgrade).hasSize(4)
        assertThat(tier1Only.all { types -> types.template.tier == 1}).isTrue()
    }
}