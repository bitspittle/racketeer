package dev.bitspittle.racketeer.scripting.types

class CancelPlayException(msg: String = "Script requested cancelling the current play") : Exception(msg)