package dev.bitspittle.lispish

import kotlin.reflect.KClass

class Environment {
    private val methodsStack = mutableListOf<MutableMap<String, Method>>()
    private val variablesStack = mutableListOf<MutableMap<String, Value>>()
    private val convertersStack = mutableListOf<Converters>()
    init {
        // Populate stacks
        pushScope()
    }

    fun add(method: Method) {
        methodsStack.last()[method.name] = method
    }

    fun set(name: String, value: Value) {
        variablesStack.last()[name] = value
    }

    fun pushScope() {
        methodsStack.add(mutableMapOf())
        variablesStack.add(mutableMapOf())
        convertersStack.add(Converters())
    }

    fun popScope() {
        check(methodsStack.size > 1) // We should always have a ground state at least, which we set up at init time

        methodsStack.removeLast()
        variablesStack.removeLast()
        convertersStack.removeLast()
    }

    fun getMethod(name: String): Method? {
        return methodsStack.reversed().asSequence()
            .mapNotNull { methods -> methods[name] }
            .firstOrNull()
    }

    fun getValue(name: String): Value? {
        return variablesStack.reversed().asSequence()
            .mapNotNull { methods -> methods[name] }
            .firstOrNull()
    }

    fun <T: Any> convert(value: Value, toClass: KClass<T>): T? {
        return convertersStack.reversed().asSequence()
            .mapNotNull { converters -> value.into(converters, toClass) }
            .firstOrNull()
    }
}