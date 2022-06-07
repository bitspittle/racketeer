package dev.bitspittle.racketeer.scripting.methods.shop

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.shop.Shop
import dev.bitspittle.racketeer.model.shop.priceFor

/**
 * shop-price-for (Card) -> (price: Int)
 */
class ShopPriceForMethod(private val getShop: () -> Shop) : Method("shop-price-for", 1) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val card = env.expectConvert<Card>(params[0])
        return getShop().priceFor(card)
    }
}