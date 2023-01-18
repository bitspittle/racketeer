package dev.bitspittle.racketeer.site.model.cloud

import dev.bitspittle.firebase.database.DataSnapshot
import dev.bitspittle.firebase.database.update
import dev.bitspittle.firebase.database.value
import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import dev.bitspittle.racketeer.site.model.account.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 * A collection of user data that is backed by a server database.
 */
class Synced(private val firebase: FirebaseData, private val account: Account, private val scope: CoroutineScope) {
    private inner class SyncRoot(path: String) {
        private lateinit var snapshot: DataSnapshot
        private val ref = firebase.db.ref(path).also { ref ->
            ref.onValue { snapshot = it }
        }

        @Suppress("UNCHECKED_CAST")
        inner class SyncProperty<T>(private val path: String, private val name: String, private val default: T) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return snapshot.child("$path/$name").value() as? T ?: default
            }

            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                scope.launch {
                    ref.child(path).update(name to value)
                }
            }
        }

        fun <T> child(path: String, default: T) =
            SyncProperty<T>(path.substringBeforeLast('/'), path.substringAfterLast('/'), default)
        fun remove() {
            scope.launch { ref.remove() }
        }
    }

    inner class Settings {
        private val root = SyncRoot("/users/${account.uid}/settings")

        inner class Features {
            var buildings by root.child("features/buildings", default = false)
        }
        val features = Features()

        fun clear() { root.remove() }
    }
    val settings = Settings()
}
