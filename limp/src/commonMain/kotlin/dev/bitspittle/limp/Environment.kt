package dev.bitspittle.limp

import kotlin.reflect.KClass

class Environment {
    private val methodsStack = mutableListOf<MutableMap<String, Method>?>()
    private val variablesStack = mutableListOf<MutableMap<String, Value>?>()
    private val convertersStack = mutableListOf<Converters?>()
    init {
        // Populate stacks
        pushScope()
    }

    fun add(method: Method) {
        val methods = methodsStack.last() ?: mutableMapOf()
        methodsStack[methodsStack.lastIndex] = methods

        require(!methods.contains(method.name)) { "Attempted to register a method named \"${method.name}\" when one already exists at the current scope. Use `pushScope` first if you really want to do this."}
        methods[method.name] = method
    }

    fun add(converter: Converter<*>) {
        val converters = convertersStack.last() ?: Converters()
        convertersStack[convertersStack.lastIndex] = converters

        converters.register(converter)
    }

    fun set(name: String, value: Value) {
        val variables = variablesStack.last() ?: mutableMapOf()
        variablesStack[variablesStack.lastIndex] = variables

        require(!variables.contains(name)) { "Attempted to register a variable named \"$name\" when one already exists at the current scope. Use `pushScope` first if you really want to do this."}
        variables[name] = value
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
        methodsStack.add(null)
        variablesStack.add(null)
        convertersStack.add(null)
    }

    fun popScope() {
        check(methodsStack.size > 1) // We should always have a ground state at least, which we set up at init time

        methodsStack.removeLast()
        variablesStack.removeLast()
        convertersStack.removeLast()
    }

    fun getMethod(name: String): Method? {
        return methodsStack.reversed().asSequence()
            .filterNotNull()
            .mapNotNull { methods -> methods[name] }
            .firstOrNull()
    }

    fun getValue(name: String): Value? {
        return variablesStack.reversed().asSequence()
            .filterNotNull()
            .mapNotNull { variables -> variables[name] }
            .firstOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> convert(value: Value, toClass: KClass<T>): T? {
        if (value.wrapped::class == toClass) {
            return value.wrapped as T
        }

        return convertersStack.reversed().asSequence()
            .filterNotNull()
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