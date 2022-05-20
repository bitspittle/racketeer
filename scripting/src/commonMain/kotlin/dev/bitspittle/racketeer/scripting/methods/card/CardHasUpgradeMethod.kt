package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.card.isInternal

class CardHasUpgradeMethod : Method("card-has-upgrade?", 2) {
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
        upgradeType.assertNotInternal()

        return card.upgrades.contains(upgradeType)
    }
}