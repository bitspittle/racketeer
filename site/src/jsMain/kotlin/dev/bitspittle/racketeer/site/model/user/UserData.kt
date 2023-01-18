package dev.bitspittle.racketeer.site.model.user

import dev.bitspittle.firebase.database.*
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import dev.bitspittle.racketeer.site.components.util.Uploads
import dev.bitspittle.racketeer.site.model.account.Account
import dev.bitspittle.racketeer.site.model.cloud.SyncProperty
import dev.bitspittle.racketeer.site.model.cloud.SyncRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * A collection of user data that is backed by a server database.
 *
 * Unlike [Uploads], this is meant for data that can be both written to and read from.
 */
class UserData(private val firebase: FirebaseData, account: Account, private val scope: CoroutineScope) {
    private val userPath = "/users/${account.uid}"

    inner class Settings {
        private val root = SyncRoot(firebase.db, scope, "$userPath/settings")

        inner class Features {
            var buildings by root.child("features/buildings", default = false, removeIfDefault = true)
        }
        val features = Features()

        fun clear() { root.remove() }
    }
    val settings = Settings()

    inner class Stats {
        private val statsPath = "$userPath/stats"

        inner class Games {
            private val gamesPath = "$statsPath/games"
            var totalVp by SyncProperty(firebase.db, scope, "$gamesPath/totalVp", 0)
                private set
            private val entriesRef = firebase.db.ref("$gamesPath/entries")
            fun add(stats: GameStats) {
                scope.launch {
                    totalVp += stats.vp
                    entriesRef.push().update(
                        "vp" to stats.vp,
                        "features" to stats.features.sorted().toTypedArray()
                    )
                }
            }
        }
        val games = Games()

        inner class Cards {
            private val root = SyncRoot(firebase.db, scope, "$statsPath/cards")

            val totalOwned get() = root.snapshot.size

            fun notifyOwnership(card: CardTemplate) {
                scope.launch {
                    root.ref.child(card.name.encodeKey()).update("ownedCount" to ServerValue.increment())
                }
            }

            fun notifyOwnership(card: Card) = notifyOwnership(card.template)

            fun ownedCount(card: Card) = ownedCount(card.template)
            fun ownedCount(card: CardTemplate) =
                root.snapshot.child("${card.name.encodeKey()}/ownedCount").value() as? Int ?: 0
        }
        val cards = Cards()

        inner class Buildings {
            private val root = SyncRoot(firebase.db, scope, "$statsPath/buildings")

            val totalBuilt get() = root.snapshot.size

            fun notifyBuilt(blueprint: Blueprint) {
                scope.launch {
                    root.ref.child(blueprint.name.encodeKey()).update("builtCount" to ServerValue.increment())
                }
            }

            fun builtCount(blueprint: Blueprint) =
                root.snapshot.child("${blueprint.name.encodeKey()}/builtCount").value() as? Int ?: 0
        }
        val buildings = Buildings()

        fun clear() { scope.launch { firebase.db.ref(statsPath).remove() } }
    }
    val stats = Stats()
}
