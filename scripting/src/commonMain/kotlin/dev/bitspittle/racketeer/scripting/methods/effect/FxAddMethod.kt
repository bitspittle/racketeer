package dev.bitspittle.racketeer.scripting.methods.effect

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.scripting.utils.setValuesFrom

/**
 * fx-add! --desc (String) --lifetime ('lifetime) --event ('event) --data (data: String) --if ('Expr) (effect: 'Expr)
 *
 * Create an effect and add it into the game.
 *
 * The most minimal form of this call, e.g. `fx-add! '(game-draw! 1)`, would add an effect that gets triggered for every
 * additional card that gets played this turn after the effect was added.
 *
 * However, there is a lot of customization you can do using the option parameters.
 *
 * `--desc`: Presentable text that will get shown to the user when this effect is, ahem, in effect.
 *
 * `--lifetime` and `--event`: See [Lifetime] and [GameEvent] for more information about the specific
 *   events. If not specified, [Lifetime.TURN] and [GameEvent.PLAY] are chosen as defaults.
 *
 * `--data`: A string value which, if set, will be stored and passed to the effect expression with the variable '$data'.
 *   This has to be a string and not a richer type to ensure we can serialize / deserialize it later.
 *
 * `--if`: An optional test expression which, if true, means we should continue running the effect. Note that it is
 *   often fine to just put this test _inside_ the effect expression itself. However, for [Lifetime.ONCE] lifetimes,
 *   effects will be discarded after the first successful run, so by pulling the `--if` expression out separately, we
 *   can delay triggering those effects until they are ready to fire.
 */
class FxAddMethod(private val getGameState: () -> GameState) : Method("fx-add!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val effectExpr = env.expectConvert<Expr>(params[0])

        val desc = options["desc"]?.let { env.expectConvert<String>(it) }
        val lifetime = options["lifetime"]
            ?.let { env.expectConvert<Expr.Identifier>(it).toEnum(Lifetime.values()) } ?: Lifetime.TURN
        val event = options["event"]
            ?.let { env.expectConvert<Expr.Identifier>(it).toEnum(GameEvent.values()) } ?: GameEvent.PLAY

        val data = options["data"]?.let { env.expectConvert<String>(it) }

        val testExpr = options["if"]?.let { env.expectConvert<Expr>(it) }

        val dataVariable = if (data != null) mapOf("\$data" to data) else emptyMap()

        val effect = when(event) {
            GameEvent.PLAY -> Effect<Card>(desc, lifetime, event, data, testExpr?.ctx?.text, effectExpr.ctx.text,
                test = { card ->
                    if (testExpr != null) {
                        env.expectConvert(env.scoped {
                            env.setValuesFrom(getGameState())
                            eval.extend(dataVariable + mapOf("\$card" to card)).evaluate(env, testExpr)
                        })
                    } else {
                        true
                    }
                },
                action = { card ->
                    env.scoped {
                        env.setValuesFrom(getGameState())
                        eval.extend(dataVariable + mapOf("\$card" to card)).evaluate(env, effectExpr)
                    }
                }
            )
            GameEvent.CREATE -> Effect<Card>(desc, lifetime, event, data, testExpr?.ctx?.text, effectExpr.ctx.text,
                test = { card ->
                    if (testExpr != null) {
                        env.expectConvert(env.scoped {
                            env.setValuesFrom(getGameState())
                            eval.extend(dataVariable + mapOf("\$card" to card)).evaluate(env, testExpr)
                        })
                    } else {
                        true
                    }
                },
                action = { card ->
                    env.scoped {
                        env.setValuesFrom(getGameState())
                        eval.extend(dataVariable + mapOf("\$card" to card)).evaluate(env, effectExpr)
                    }
                }
            )
            GameEvent.DESTROY -> Effect<Card>(desc, lifetime, event, data, testExpr?.ctx?.text, effectExpr.ctx.text,
                test = { card ->
                    if (testExpr != null) {
                        env.expectConvert(env.scoped {
                            env.setValuesFrom(getGameState())
                            eval.extend(dataVariable + mapOf("\$card" to card)).evaluate(env, testExpr)
                        })
                    } else {
                        true
                    }
                },
                action = { card ->
                    env.scoped {
                        env.setValuesFrom(getGameState())
                        eval.extend(dataVariable + mapOf("\$card" to card)).evaluate(env, effectExpr)
                    }
                }
            )
            GameEvent.SHUFFLE -> Effect<Pile>(desc, lifetime, event, data, testExpr?.ctx?.text, effectExpr.ctx.text,
                test = { pile ->
                    if (testExpr != null) {
                        env.expectConvert(env.scoped {
                            env.setValuesFrom(getGameState())
                            eval.extend(dataVariable + mapOf("\$pile" to pile)).evaluate(env, testExpr)
                        })
                    } else {
                        true
                    }
                },
                action = { pile ->
                    env.scoped {
                        env.setValuesFrom(getGameState())
                        eval.extend(dataVariable + mapOf("\$pile" to pile)).evaluate(env, effectExpr)
                    }
                }
            )
            GameEvent.TURN_START -> Effect<Unit>(desc, lifetime, event, data, testExpr?.ctx?.text, effectExpr.ctx.text,
                test = {
                    if (testExpr != null) {
                        env.expectConvert(env.scoped {
                            env.setValuesFrom(getGameState())
                            eval.extend(dataVariable).evaluate(env, testExpr)
                        })
                    } else {
                        true
                    }
                },
                action = {
                    env.scoped {
                        env.setValuesFrom(getGameState())
                        eval.extend(dataVariable).evaluate(env, effectExpr)
                    }
                }
            )
            GameEvent.TURN_END -> Effect<Unit>(desc, lifetime, event, data, testExpr?.ctx?.text, effectExpr.ctx.text,
                test = {
                    if (testExpr != null) {
                        env.expectConvert(env.scoped {
                            env.setValuesFrom(getGameState())
                            eval.extend(dataVariable).evaluate(env, testExpr)
                        })
                    } else {
                        true
                    }
                },
                action = {
                    env.scoped {
                        env.setValuesFrom(getGameState())
                        eval.extend(dataVariable).evaluate(env, effectExpr)
                    }
                }
            )
        }

        getGameState().apply(GameStateChange.AddEffect(effect))
        return Unit
    }
}