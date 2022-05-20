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
import dev.bitspittle.racketeer.model.card.isInternal
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

class CardUpgradeMethod(private val getGameState: () -> GameState) : Method("card-upgrade!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {

        val cards = env.scoped {
            env.addConverter(ItemToSingletonListConverter(Card::class))
            env.expectConvert<List<Card>>(params[0], listTypeOf())
        }

        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val upgradeType = identifier.toEnum(UpgradeType.values())
        upgradeType.assertNotInternal()

        val gameState = getGameState()
        cards.forEach { card -> gameState.apply(GameStateChange.UpgradeCard(card, upgradeType)) }

        return Unit
    }
}