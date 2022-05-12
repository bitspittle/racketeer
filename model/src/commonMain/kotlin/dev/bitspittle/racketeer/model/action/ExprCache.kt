package dev.bitspittle.racketeer.model.action

import dev.bitspittle.limp.types.Expr

class ExprCache {
    private val codeToExpr = mutableMapOf<String, Expr>()
    fun parse(code: String) = codeToExpr.getOrPut(code) { Expr.parse(code) }
}