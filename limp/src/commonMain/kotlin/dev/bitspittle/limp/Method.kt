package dev.bitspittle.limp

abstract class Method(val name: String, val numArgs: Int, val consumeRest: Boolean = false) {
    /**
     * Invoke this method using the current [Environment].
     *
     * @param params The list of required parameters. Its length is guaranteed to match [numArgs].
     * @param options Additional parameters specified using a `--option value` syntax.
     * @param rest All remaining values to the end of the expression. Will be empty unless [consumeRest] is set to true.
     */
    abstract suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any> = mapOf(), rest: List<Any> = listOf()): Any
}