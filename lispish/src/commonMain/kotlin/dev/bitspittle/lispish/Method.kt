package dev.bitspittle.lispish

abstract class Method(val name: String, val numArgs: Int, val consumeRest: Boolean = false) {
    abstract fun invoke(params: List<Value>): Value
}