package dev.bitspittle.limp.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.logic.AndMethod
import dev.bitspittle.limp.methods.logic.NotMethod
import dev.bitspittle.limp.methods.logic.OrMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.range.IntRangeMethod

fun Environment.installUsefulDefaults() {
    // Math
    add(AddMethod())
    add(SubMethod())
    add(MulMethod())
    add(DivMethod())

    add(MinMethod())
    add(MaxMethod())
    add(ClampMethod())

    // Logic
    set("true", Value(true))
    set("false", Value(false))

    add(NotMethod())
    add(AndMethod())
    add(OrMethod())

    // Range
    add(IntRangeMethod())
}