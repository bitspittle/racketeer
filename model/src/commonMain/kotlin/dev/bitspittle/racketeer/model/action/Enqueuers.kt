package dev.bitspittle.racketeer.model.action

import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.location.LocationEnqueuer

/**
 * A simple wrapping class so we don't have to keep adding parameters into function signatures everywhere each time
 * we come up with a new enqueuer type...
 */
class Enqueuers(
    val actionQueue: ActionQueue,
    val card: CardEnqueuer,
    val location: LocationEnqueuer,
)