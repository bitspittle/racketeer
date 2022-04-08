package dev.bitspittle.racketeer.scripting.utils

import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

fun CardTemplate.compileActions() = actions.map { Expr.parse(it) }
fun Card.compileActions() = template.compileActions()
