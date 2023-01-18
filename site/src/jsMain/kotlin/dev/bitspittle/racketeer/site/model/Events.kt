package dev.bitspittle.racketeer.site.model

import dev.bitspittle.racketeer.site.model.account.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

typealias Events = MutableSharedFlow<Event>

fun Events.emitAsync(scope: CoroutineScope, event: Event) {
    scope.launch { emit(event) }
}

sealed interface Event {
    /** @param account The account that was changed. Will be null if the user logged out. */
    class AccountChanged(val account: Account?) : Event
    class GameStateUpdated(val ctx: GameContext) : Event
}