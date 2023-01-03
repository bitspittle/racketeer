package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.TraitType
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * card-add-trait! (Card) ('Trait)
 */
class CardAddTraitMethod(private val addGameChange: suspend (GameStateChange) -> Unit) : Method("card-add-trait!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val card = env.expectConvert<Card>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val traitType = identifier.toEnum(TraitType.values())

        if (!card.traits.contains(traitType)) {
            addGameChange(GameStateChange.AddTrait(card, traitType))
        }

        return Unit
    }
}