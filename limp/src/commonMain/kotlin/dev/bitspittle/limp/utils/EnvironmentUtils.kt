package dev.bitspittle.limp.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.converters.CharToStringConverter
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.convert.ToIntMethod
import dev.bitspittle.limp.methods.convert.ToStringMethod
import dev.bitspittle.limp.methods.logic.AndMethod
import dev.bitspittle.limp.methods.logic.IfMethod
import dev.bitspittle.limp.methods.logic.NotMethod
import dev.bitspittle.limp.methods.logic.OrMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.range.IntRangeMethod
import dev.bitspittle.limp.methods.system.DefMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.limp.methods.text.ConcatMethod
import dev.bitspittle.limp.methods.text.JoinToStringMethod
import dev.bitspittle.limp.methods.text.LowerMethod
import dev.bitspittle.limp.methods.text.UpperMethod
import dev.bitspittle.limp.types.Placeholder

/**
 * Install default methods and values that make a limp environment somewhat useful.
 *
 * Defaults are guaranteed not to contain any blocking behavior, so it is save to evaluate them in a `runBlocking`
 * context without worrying about blocking the thread.
 */
fun Environment.installDefaults() {
    // System
    storeValue("_", Placeholder)
    addMethod(SetMethod())
    addMethod(DefMethod())

    // Math
    addMethod(AddMethod())
    addMethod(SubMethod())
    addMethod(MulMethod())
    addMethod(DivMethod())
    addMethod(PowMethod())
    addMethod(RemainderMethod())

    addMethod(MinMethod())
    addMethod(MaxMethod())
    addMethod(ClampMethod())

    addMethod(AddListMethod())
    addMethod(MulListMethod())

    // Logic
    storeValue("true", true)
    storeValue("false", false)

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
    addMethod(CompareMethod())

    // Range
    addMethod(IntRangeMethod())

    // Collection
    addMethod(ListMethod())
    addMethod(ListGetMethod())
    addMethod(InMethod())
    addMethod(MapMethod())
    addMethod(FilterMethod())
    addMethod(FirstMethod())
    addMethod(SingleMethod())
    addMethod(TakeMethod())
    addMethod(ShuffleMethod())
    addMethod(SortMethod())
    addMethod(UnionMethod())

    // Convert
    addMethod(ToIntMethod())
    addMethod(ToStringMethod())

    // Text
    addConverter(CharToStringConverter())
    addMethod(ConcatMethod())
    addMethod(UpperMethod())
    addMethod(LowerMethod())
    addMethod(JoinToStringMethod())
}