package dev.bitspittle.limp

class Value(val wrapped: Any) {
    companion object {
        val Empty = Value(Unit)
        val Placeholder = Value(dev.bitspittle.limp.types.Placeholder)
        val False = Value(false)
        val True = Value(true)
    }

    override fun toString(): String {
        return "Value { $wrapped }"
    }

    override fun equals(other: Any?): Boolean {
        return (other is Value) && wrapped == other.wrapped
    }

    override fun hashCode(): Int {
        return wrapped.hashCode()
    }
}