package dev.bitspittle.racketeer.site.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

typealias Events = MutableSharedFlow<Event>

fun Events.emitAsync(scope: CoroutineScope, event: Event) {
    scope.launch { emit(event) }
}

sealed interface Event {
    class GameStateUpdated(val ctx: GameContext) : Event
    class SettingsChanged(val settings: Settings) : Event
}