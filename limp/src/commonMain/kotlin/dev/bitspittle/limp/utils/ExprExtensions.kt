package dev.bitspittle.limp.utils

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr

fun String.toIdentifierName(): String {
    return this.lowercase().replace('_', '-')
}

fun <E: Enum<E>> E.toIdentifierName(): String {
    return this.name.toIdentifierName()
}

fun <E: Enum<E>> Array<E>.toIdentifierNames(): Sequence<Pair<E, String>> {
    return this
        .asSequence()
        .map { enum -> enum to enum.name.toIdentifierName() }
}

/**
 * Convert an expression identifier (e.g. `'random`) into an enum with the same name
 * (e.g. `enum ListStrategy { RANDOM }`)
 *
 * Due to reflection limitations in Kotlin multiplatform, you need to pass in all enumeration values rather than being
 * able to simply figure out the values based on the enum type.
 *
 * For example:
 *
 * ```
 * val strategyIdent: Expr.Identifier
 * val strategy = strategyIdent.toEnumOrNull(ListStrategy.values())
 * ```
 *
 * NOTE: Enum names with underscores will be translated into hyphens, e.g. `SOME_EXAMPLE` will match `some-example`
 */
fun <E: Enum<E>> Expr.Identifier.toEnumOrNull(values: Array<E>): E? {
    return values
        .toIdentifierNames()
        .filter { it.second == this.name }
        .firstOrNull()
        ?.first
}

fun <E: Enum<E>> Expr.Identifier.toEnum(values: Array<E>): E = toEnumOrNull(values)
    ?: throw EvaluationException(ctx, "Identifier name expected to match one of: ${values.toIdentifierNames().map { it.second }.joinToString()}")

fun Expr.Identifier.toValueOrNull(values: Iterable<String>): String? {
    return values
        .asSequence()
        .filter { it.toIdentifierName() == this.name }
        .firstOrNull()
}

fun Expr.Identifier.toValue(values: Iterable<String>): String = toValueOrNull(values)
    ?: throw EvaluationException(ctx, "Identifier name expected to match one of: ${values.joinToString { it.toIdentifierName() }}")
