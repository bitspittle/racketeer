package dev.bitspittle.limp.types

interface Logger {
    fun log(message: String)
}

class ConsoleLogger : Logger {
    override fun log(message: String) {
        println(message)
    }
}

fun Logger.warn(message: String) = log("Warning: $message")