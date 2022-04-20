package dev.bitspittle.limp

class Environment {
    private val methodsStack = mutableListOf<MutableMap<String, Method>?>()
    private val aliasesStack = mutableListOf<MutableMap<String, String>?>()
    private val variablesStack = mutableListOf<MutableMap<String, Any>?>()
    private val convertersStack = mutableListOf<MutableList<Converter<*>>?>()
    init {
        // Populate stacks
        pushScope()
    }

    fun addMethod(method: Method, allowOverwrite: Boolean = false) {
        val methods = methodsStack.last() ?: mutableMapOf()
        methodsStack[methodsStack.lastIndex] = methods

        if (!allowOverwrite) {
            require(!methods.contains(method.name)) { "Attempted to register a method named \"${method.name}\" when one already exists at the current scope." }
        }
        methods[method.name] = method
    }

    fun addAlias(alias: String, forName: String, allowOverwrite: Boolean = false) {
        val aliases = aliasesStack.last() ?: mutableMapOf()
        aliasesStack[aliasesStack.lastIndex] = aliases

        if (!allowOverwrite) {
            require(!aliases.contains(alias)) { "Attempted to register the alias \"$alias\" when it was already defined at the current scope." }
        }
        aliases[alias] = forName
    }

    fun addConverter(converter: Converter<*>) {
        val converters = convertersStack.last() ?: mutableListOf()
        convertersStack[convertersStack.lastIndex] = converters
        converters.add(converter)
    }

    fun storeValue(name: String, value: Any, allowOverwrite: Boolean = false) {
        val variables = variablesStack.last() ?: mutableMapOf()
        variablesStack[variablesStack.lastIndex] = variables

        if (!allowOverwrite) {
            require(!variables.contains(name)) { "Attempted to register a variable named \"$name\" when one already exists at the current scope." }
        }
        variables[name] = value
    }

    inline fun <T: Any?> scoped(block: Environment.() -> T): T {
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
        aliasesStack.add(null)
        variablesStack.add(null)
        convertersStack.add(null)
    }

    fun popScope() {
        check(methodsStack.size > 1) // We should always have a ground state at least, which we set up at init time

        methodsStack.removeLast()
        aliasesStack.removeLast()
        variablesStack.removeLast()
        convertersStack.removeLast()
    }

    fun getMethodNames(): Set<String> {
        return methodsStack.reversed().asSequence()
            .filterNotNull()
            .flatMap { methods -> methods.keys.asSequence() }
            .toSet()
    }

    fun getVariableNames(): Set<String> {
        return variablesStack.reversed().asSequence()
            .filterNotNull()
            .flatMap { variables -> variables.keys.asSequence() }
            .toSet()
    }

    private fun findMethod(name: String): Method? {
        return methodsStack.reversed().asSequence()
            .filterNotNull()
            .mapNotNull { methods -> methods[name] }
            .firstOrNull()
    }

    private fun findNameForAlias(alias: String): String? {
        return aliasesStack.reversed().asSequence()
            .filterNotNull()
            .mapNotNull { aliases -> aliases[alias] }
            .firstOrNull()
    }

    fun getMethod(name: String): Method? {
        return findMethod(name)
            // Maybe the passed-in name is an alias for a different method, e.g. "lget" -> "list-get"
            ?: findNameForAlias(name)?.let { findMethod(it) }
    }

    private fun findValue(name: String): Any? {
        return variablesStack.reversed().asSequence()
            .filterNotNull()
            .mapNotNull { variables -> variables[name] }
            .firstOrNull()
    }

    fun loadValue(name: String): Any? {
        return findValue(name)
            // Maybe the passed-in name is an alias for a different method, e.g. "$really-long-name" -> "$name"
            ?: findNameForAlias(name)?.let { findValue(it) }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> tryConvert(value: Any, typeChecker: TypeChecker<T>): T? {
        typeChecker.cast(value)?.let { return it }

        return convertersStack.reversed().asSequence()
            .filterNotNull()
            .flatMap { converters -> converters.asSequence() }
            .mapNotNull { converter -> converter.convert(value) as T? }
            .firstOrNull()
    }

    fun expectMethod(name: String): Method {
        return getMethod(name) ?: throw IllegalArgumentException("No method named \"$name\" is registered")
    }

    fun expectValue(name: String): Any {
        return loadValue(name) ?: throw IllegalArgumentException("No variable named \"$name\" is registered")
    }

    fun <T: Any> expectConvert(value: Any, typeChecker: TypeChecker<T>): T {
        return tryConvert(value, typeChecker) ?: throw IllegalArgumentException("Could not convert ${value::class} (value = \"${value}\") to ${typeChecker.targetClass.qualifiedName}")
    }

    inline fun <reified T: Any> tryConvert(value: Any): T? = tryConvert(value, typeOf())
    inline fun <reified T: Any> expectConvert(value: Any): T = expectConvert(value, typeOf())
}