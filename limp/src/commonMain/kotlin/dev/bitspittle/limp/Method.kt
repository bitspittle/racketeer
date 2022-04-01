package dev.bitspittle.limp

abstract class Method(val name: String, val numArgs: Int, val consumeRest: Boolean = false) {
    abstract fun invoke(env: Environment, params: List<Value>): Value
}