package dev.bitspittle.racketeer.console.game

@JvmInline
value class Version(val value: String): Comparable<Version> {
    init {
        require(value.parts().size == 3)
    }

    private fun String.parts() = this.split('.').map { it.toInt() }

    override fun compareTo(other: Version): Int {
        val zipped = this.value.parts().zip(other.value.parts())
        zipped.forEach { (selfPart, otherPart) ->
            if (selfPart < otherPart) return -1
            if (selfPart > otherPart) return 1
        }
        return 0
    }

    override fun toString() = value
}