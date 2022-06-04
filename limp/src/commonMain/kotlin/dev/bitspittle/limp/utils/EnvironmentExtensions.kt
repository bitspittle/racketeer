package dev.bitspittle.limp.utils

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.converters.CharToStringConverter
import dev.bitspittle.limp.converters.IntRangeToListConverter
import dev.bitspittle.limp.converters.IntToIntRangeConverter
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
import dev.bitspittle.limp.methods.system.*
import dev.bitspittle.limp.methods.text.ConcatMethod
import dev.bitspittle.limp.methods.text.JoinToStringMethod
import dev.bitspittle.limp.methods.text.LowerMethod
import dev.bitspittle.limp.methods.text.UpperMethod
import dev.bitspittle.limp.types.DefaultLangService
import dev.bitspittle.limp.types.LangService
import dev.bitspittle.limp.types.Placeholder

/**
 * Install default methods and values that make a limp environment somewhat useful.
 *
 * Defaults are guaranteed not to contain any blocking behavior, so it is save to evaluate them in a `runBlocking`
 * context without worrying about blocking the thread.
 */
fun Environment.installDefaults(service: LangService = DefaultLangService()) {
    // System
    storeValue("_", Placeholder)
    addMethod(SetMethod(service.logger))
    addMethod(DefMethod())
    addMethod(AliasMethod())
    addMethod(RunMethod())
    addMethod(DbgMethod(service.logger))
    addMethod(InfoMethod(service.logger))

    // Math
    addMethod(AbsMethod())
    addMethod(NegMethod())
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
    addConverter(IntToIntRangeConverter())
    addConverter(IntRangeToListConverter())

    // Collection
    addMethod(ListMethod())
    addMethod(SizeMethod())
    addMethod(EmptyMethod())
    addMethod(ListGetMethod())
    addMethod(IndexOfMethod())
    addMethod(InMethod())
    addMethod(AnyMethod())
    addMethod(AllMethod())
    addMethod(NoneMethod())
    addMethod(ForEachMethod())
    addMethod(MapMethod())
    addMethod(CountMethod())
    addMethod(FilterMethod())
    addMethod(FirstMethod())
    addMethod(SingleMethod())
    addMethod(DropMethod(service::random))
    addMethod(TakeMethod(service::random))
    addMethod(ShuffleMethod(service::random))
    addMethod(ShuffledMethod(service::random))
    addMethod(RandomMethod(service::random))
    addMethod(SortedMethod())
    addMethod(ReversedMethod())
    addMethod(DistinctMethod())
    addMethod(UnionMethod())
    addMethod(RepeatMethod())

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