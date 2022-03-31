package dev.bitspittle.lispish

class Environment {
    private val methodsStack = mutableListOf<MutableMap<String, Method>>()
    private val variablesStack = mutableListOf<MutableMap<String, Value>>()

    fun snapshot() {
        methodsStack.add(mutableMapOf())
        variablesStack.add(mutableMapOf())
    }

    fun restore() {
        require(methodsStack.isNotEmpty())

        methodsStack.removeLast()
        variablesStack.removeLast()
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
}