package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import kotlin.math.max
import kotlin.math.min

class ListMethod : Method("list", 0, consumeRest = true) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        // Convert List<Value> to a Value(List<T>), so that way if this list is consumed downstream, it looks like
        // a normal list to the consumer. This also has the nice side effect of making a copy of the list to make sure
        // things don't get deleted out from under us.
        return Value(rest.map { it.wrapped })
    }
}