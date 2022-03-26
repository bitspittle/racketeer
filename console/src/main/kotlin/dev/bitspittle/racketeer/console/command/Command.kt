package dev.bitspittle.racketeer.console.command

interface Command {
    val title: String
    val description: String? get() = null
}