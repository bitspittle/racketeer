package dev.bitspittle.racketeer.scripting.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.allCards
import dev.bitspittle.racketeer.model.game.getOwnedCards
import dev.bitspittle.racketeer.scripting.converters.PileToCardsConverter
import dev.bitspittle.racketeer.scripting.converters.MutablePileToCardsConverter
import dev.bitspittle.racketeer.scripting.methods.card.*
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseMethod
import dev.bitspittle.racketeer.scripting.methods.effect.FxAddMethod
import dev.bitspittle.racketeer.scripting.methods.game.*
import dev.bitspittle.racketeer.scripting.methods.pile.PileCopyToMethod
import dev.bitspittle.racketeer.scripting.methods.pile.PileGetMethod
import dev.bitspittle.racketeer.scripting.methods.pile.PileMoveToMethod
import dev.bitspittle.racketeer.scripting.methods.shop.ShopExcludeMethod
import dev.bitspittle.racketeer.scripting.methods.shop.ShopRerollMethod
import dev.bitspittle.racketeer.scripting.methods.system.CancelMethod
import dev.bitspittle.racketeer.scripting.methods.system.RunLaterMethod
import dev.bitspittle.racketeer.scripting.methods.system.StopMethod
import dev.bitspittle.racketeer.scripting.methods.text.IconConvertMethod
import dev.bitspittle.racketeer.scripting.types.GameService

/**
 * Return true if a building has the right conditions for activating.
 *
 * Note that this is independent of if the user can *afford* to activate the building.
 */
suspend fun Building.canActivate(env: Environment, state: GameState): Boolean {
    if (this.blueprint.canActivate.isBlank()) return true

    val self = this
    val evaluator = Evaluator()
    return env.scoped {
        env.setValuesFrom(state)
        env.setValuesFrom(self)
        evaluator.evaluate(env, self.blueprint.canActivate) as Boolean
    }
}