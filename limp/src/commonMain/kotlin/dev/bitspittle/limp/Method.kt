package dev.bitspittle.limp

abstract class Method(val name: String, val numArgs: Int, val consumeRest: Boolean = false) {
    /**
     * Invoke this method using the current [Environment].
     *
     * @param params The list of required parameters. Its length is guaranteed to match [numArgs].
     * @param optionals Additional parameters specified using a `--optional param` syntax.
     * @param rest All remaining values to the end of the expression. Will be empty unless [consumeRest] is set to true.
     */
    abstract fun invoke(env: Environment, params: List<Value>, optionals: Map<String, Value> = mapOf(), rest: List<Value> = listOf()): Value
}