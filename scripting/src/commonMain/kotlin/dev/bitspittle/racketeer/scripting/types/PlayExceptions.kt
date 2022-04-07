package dev.bitspittle.racketeer.scripting.types

/** An "exception" used by card actions as a signal to interrupt the current card being played. */
abstract class PlayException : Exception()

/** Sent as a signal to tell the game to stop processing more actions but keep any game state changes made so far. */
class FinishPlayException : PlayException()

/** Sent as a signal to tell the game to stop processing more actions and discard any game state changes made so far. */
class CancelPlayException : PlayException()