package dev.bitspittle.limp.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.logic.AndMethod
import dev.bitspittle.limp.methods.logic.IfMethod
import dev.bitspittle.limp.methods.logic.NotMethod
import dev.bitspittle.limp.methods.logic.OrMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.range.IntRangeMethod
import dev.bitspittle.limp.methods.system.DefMethod
import dev.bitspittle.limp.methods.system.SetMethod

fun Environment.installUsefulDefaults() {
    // System
    storeValue("_", Value.Placeholder)
    addMethod(SetMethod())
    addMethod(DefMethod())

    // Math
    addMethod(AddMethod())
    addMethod(SubMethod())
    addMethod(MulMethod())
    addMethod(DivMethod())
    addMethod(RemainderMethod())

    addMethod(MinMethod())
    addMethod(MaxMethod())
    addMethod(ClampMethod())

    addMethod(AddListMethod())
    addMethod(MulListMethod())

    // Logic
    storeValue("true", Value.True)
    storeValue("false", Value.False)

    addMethod(NotMethod())
    addMethod(AndMethod())
    addMethod(OrMethod())

    addMethod(IfMethod())

    // Comparison
    addMethod(LessThanMethod())
    addMethod(LessThanEqualsMethod())
    addMethod(EqualsMethod())
    addMethod(NotEqualsMethod())
    addMethod(GreaterThanMethod())
    addMethod(GreaterThanEqualsMethod())

    // Range
    addMethod(IntRangeMethod())

    // Collection
    addMethod(ListMethod())
    addMethod(InMethod())
    addMethod(FilterMethod())
    addMethod(FirstMethod())
    addMethod(SingleMethod())
    addMethod(TakeMethod())
    addMethod(ShuffleMethod())
    addMethod(SortMethod())
    addMethod(UnionMethod())
}