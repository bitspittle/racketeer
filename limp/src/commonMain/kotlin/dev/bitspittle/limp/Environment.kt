package dev.bitspittle.limp

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
        require(!methodsStack.last().contains(method.name)) { "Attempted to register a method named \"${method.name}\" when one already exists at the current scope. Use `pushScope` first if you really want to do this."}
        methodsStack.last()[method.name] = method
    }

    fun add(converter: Converter<*>) {
        convertersStack.last().register(converter)
    }

    fun set(name: String, value: Value) {
        require(!variablesStack.last().contains(name)) { "Attempted to register a variable named \"$name\" when one already exists at the current scope. Use `pushScope` first if you really want to do this."}
        variablesStack.last()[name] = value
    }

    fun <T: Any> scoped(block: () -> T): T {
        return try {
            pushScope()
            block()
        }
        finally {
            popScope()
        }
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

    fun expectMethod(name: String): Method {
        return getMethod(name) ?: error("No method named \"$name\" is registered")
    }

    fun expectValue(name: String): Value {
        return getValue(name) ?: error("No variable named \"$name\" is registered")
    }

    fun <T: Any> expectConvert(value: Value, toClass: KClass<T>): T {
        return convert(value, toClass) ?: error("Could not convert $value to $toClass")
    }

    inline fun <reified T: Any> convert(value: Value): T? = convert(value, T::class)
    inline fun <reified T: Any> expectConvert(value: Value): T = expectConvert(value, T::class)
}