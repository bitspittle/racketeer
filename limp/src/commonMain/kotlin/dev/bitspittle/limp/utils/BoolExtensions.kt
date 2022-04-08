package dev.bitspittle.limp.utils

inline fun <T> Boolean.ifTrue(block: () -> T): T? = if (this) block() else null
inline fun <T> Boolean.ifFalse(block: () -> T): T? = (!this).ifTrue(block)
inline fun <T> Boolean?.ifTrueOrNull(block: () -> T): T? = (this == null || this).ifTrue(block)
inline fun <T> Boolean?.ifFalseOrNull(block: () -> T): T? = (this == null || !this).ifTrue(block)
