package dev.bitspittle.racketeer.model.game

enum class GameEvent {
    /** An event fired when the turn begins (just after the draw finished). */
    TURN_START,

    /** An event fired when the turn ends (just before all cards are discarded). */
    TURN_END,

    /**
     * An event fired just after a card is played (when it moved from the hand into the street).
     *
     * Play events will include the played card set to the variable `$card`.
     */
    PLAY,

    /**
     * An event fired just after a pile is shuffled.
     *
     * Shuffle events will include the shuffled pile set to the variable `$pile`.
     */
    SHUFFLE,

    /**
     * An event fired whenever the shop is restocked.
     *
     * This only happens for restocks triggered by the player, e.g. by cards that reroll your shop or by spending
     * resources to do so. Rerolls that automatically occur at the end of turn don't count.
     */
    RESTOCK,

    /**
     * An event fired just after a card becomes newly created.
     *
     * This happens when you buy a card from the store, jailbreak it, or a few other ways caused by card actions
     * (e.g. mimes creating a new card, or a pair of earrings splitting into two, etc.)
     *
     * Create events will include the newly created card set to the variable `$card`.
     */
    CREATE,

    /**
     * An event fired whenever one (or more) cards is discarded as the result of an action.
     *
     * This only happens for discards initialized by the player. Automatic discards that occur at the end of the turn
     * don't count.
     *
     * All Discard events will include the list of discarded cards set to the variable `$cards`.
     */
    DISCARD,
}