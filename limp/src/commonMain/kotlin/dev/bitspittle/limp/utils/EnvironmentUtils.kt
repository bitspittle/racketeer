package dev.bitspittle.limp.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.math.AddMethod
import dev.bitspittle.limp.methods.math.DivMethod
import dev.bitspittle.limp.methods.math.MulMethod
import dev.bitspittle.limp.methods.math.SubMethod
import dev.bitspittle.limp.methods.range.IntRangeMethod

fun Environment.installUsefulDefaults() {
    add(AddMethod())
    add(SubMethod())
    add(MulMethod())
    add(DivMethod())
    add(IntRangeMethod())
}