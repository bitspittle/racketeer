package dev.bitspittle.lispish.utils

import dev.bitspittle.lispish.Environment
import dev.bitspittle.lispish.Method
import dev.bitspittle.lispish.Value
import dev.bitspittle.lispish.methods.math.AddMethod
import dev.bitspittle.lispish.methods.math.SubMethod

fun Environment.installUsefulDefaults() {
    add(AddMethod())
    add(SubMethod())
}