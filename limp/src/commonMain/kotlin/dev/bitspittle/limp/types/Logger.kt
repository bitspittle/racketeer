package dev.bitspittle.limp.types

interface Logger {
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun debug(message: String)
}

abstract class DelegatingLogger : Logger {
    protected abstract fun log(message: String)

    override fun info(message: String) = log("[I] $message")
    override fun warn(message: String) = log("[W] $message")
    override fun error(message: String) = log("[E] $message")
    override fun debug(message: String) = log("[D] $message")
}

class ConsoleLogger : DelegatingLogger() {
    override fun log(message: String) = println(message)
}
