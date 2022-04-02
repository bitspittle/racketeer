package dev.bitspittle.limp.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.logic.AndMethod
import dev.bitspittle.limp.methods.logic.NotMethod
import dev.bitspittle.limp.methods.logic.OrMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.range.IntRangeMethod

fun Environment.installUsefulDefaults() {
    // Misc
    set("_", Value.Placeholder)

    // Math
    add(AddMethod())
    add(SubMethod())
    add(MulMethod())
    add(DivMethod())
    add(RemainderMethod())

    add(LessThanMethod())
    add(LessThanEqualsMethod())
    add(EqualsMethod())
    add(GreaterThanMethod())
    add(GreaterThanEqualsMethod())

    add(MinMethod())
    add(MaxMethod())
    add(ClampMethod())

    add(AddListMethod())
    add(MulListMethod())

    // Logic
    set("true", Value(true))
    set("false", Value(false))

    add(NotMethod())
    add(AndMethod())
    add(OrMethod())

    // Range
    add(IntRangeMethod())

    // Collection
    add(ListMethod())
}