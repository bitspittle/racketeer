package dev.bitspittle.racketeer.site.components.util

import kotlin.js.Date

/** Convert this date into a String that's safe to put into a filename. */
fun Date.toFilenameString(): String {
    fun Int.twoDigitStr() = this.toString().padStart(2, '0')

    val year = getFullYear()
    val month = getMonth() + 1
    val day = getDate()
    val hour = getHours()
    val mins = getMinutes()
    val secs = getSeconds()

    return "$year-${month.twoDigitStr()}-${day.twoDigitStr()}_${hour.twoDigitStr()}-${mins.twoDigitStr()}-${secs.twoDigitStr()}"
}