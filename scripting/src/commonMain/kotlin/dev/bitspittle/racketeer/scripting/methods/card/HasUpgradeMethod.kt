package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.UpgradeType

class HasUpgradeMethod() : Method("has-upgrade?", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {

        val card = env.expectConvert<Card>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val upgradeType = identifier.toEnum(UpgradeType.values())

        return card.upgrades.contains(upgradeType)
    }
}