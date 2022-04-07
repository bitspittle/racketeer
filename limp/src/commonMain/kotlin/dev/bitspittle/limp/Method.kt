package dev.bitspittle.limp

abstract class Method(val name: String, val numArgs: Int, val consumeRest: Boolean = false) {
    /**
     * Invoke this method using the current [Environment].
     *
     * Note that all values passed in are dynamically typed. Although you could use default Kotlin cast operations to
     * convert them to the actual data type, it is better to use [Environment.expectConvert] which both throws a more
     * informative error with less typing but, more importantly, checks the environment's converters to see if there are any appropriate
     * ones.
     *
     * In other words:
     *
     * ```
     * // YES
     * val name = env.expectConvert<String>(params[0])
     *
     * // NO
     * val name = params[0] as String
     * val name = params[0] as? String ?: error("Expected a name value")
     * ```
     *
     * @param eval The evaluator used to invoke this method. If you need your own evaluator for some reason, you may
     *  re-use or extend this one.
     * @param params The list of required parameters. Its length is guaranteed to match [numArgs].
     * @param options Additional parameters specified using a `--option value` syntax.
     * @param rest All remaining values to the end of the expression. Will be empty unless [consumeRest] is set to true.
     *
     * @return The result of invoking the method. You may return Unit here if the method doesn't return any value,
     *   particularly useful for methods with side effects.
     */
    abstract suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any> = mapOf(),
        rest: List<Any> = listOf()
    ): Any
}