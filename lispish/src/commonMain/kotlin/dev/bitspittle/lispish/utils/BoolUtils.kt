package dev.bitspittle.lispish.utils

fun <T> Boolean.ifTrue(block: () -> T): T? = if (this) block() else null
fun <T> Boolean.ifFalse(block: () -> T): T? = if (this) null else block()