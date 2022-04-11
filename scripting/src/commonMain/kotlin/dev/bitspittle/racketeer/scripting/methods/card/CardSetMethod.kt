package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.scripting.types.CardProperty

class CardSetMethod : Method("card-set!", 3) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val cards = env.scoped {
            env.addConverter(ItemToSingletonListConverter(Card::class))
            env.expectConvert<List<Card>>(params[0], listTypeOf())
        }
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(CardProperty.values())

        val setExpr = env.scoped {
            env.addConverter(ValueToExprConverter(Int::class))
            env.expectConvert<Expr>(params[2])
        }

        cards.forEach { card ->
            val currValue = when (property) {
                CardProperty.VP -> card.vp
                CardProperty.VP_PASSIVE -> card.vpPassive
                CardProperty.COST, CardProperty.TIER, CardProperty.TYPES, CardProperty.NAME, CardProperty.ID  -> throw EvaluationException(
                    identifier.ctx, "Cannot set this card's property as it is read-only."
                )
            }

            val newValue = env.scoped { // Don't let values defined during the lambda escape
                val evaluator = eval.extend(mapOf("\$it" to currValue))
                env.expectConvert<Int>(evaluator.evaluate(env, setExpr))
            }
            when (property) {
                CardProperty.VP -> card.vp = newValue
                CardProperty.VP_PASSIVE -> card.vpPassive = newValue
                else -> error("Unhandled card-set case: ${property.name}")
            }
        }

        return Unit
    }
}