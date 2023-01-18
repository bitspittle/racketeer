package dev.bitspittle.racketeer.site.model.cloud

import dev.bitspittle.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 * Class useful for abstracting interacting with a firebase database.
 *
 * Simply create a sync root (a subtree that you're OK fetching everything for) and sync properties. You can use the
 * `by` keyword with sync properties and basically work with the database as if you're editing raw values, e.g.
 *
 * ```
 * val enabled by root.child("path/to/property", default = false)
 * enabled = true
 * ```
 */
class SyncRoot(private val db: Database, private val scope: CoroutineScope, val path: String) {
    lateinit var snapshot: DataSnapshot
        private set
    val ref = db.ref(path).also { ref ->
        ref.onValue { snapshot = it }
    }

    fun <T> child(path: String, default: T, removeIfDefault: Boolean = false) =
        SyncProperty(db, scope, path, default, relativeTo = this, removeIfDefault = removeIfDefault)

    fun removeAsync() { scope.launch { remove() } }
    suspend fun remove() { ref.remove() }
}

@Suppress("UNCHECKED_CAST")
class SyncProperty<T>(
    db: Database,
    private val scope: CoroutineScope,
    private val path: String,
    private val default: T,
    private val relativeTo: SyncRoot? = null,
    private val removeIfDefault: Boolean = false,
) {
    private val dir = path.substringBeforeLast('/', "")
    val key = path.substringAfterLast('/')
    private val root = relativeTo ?: SyncRoot(db, scope, dir)
    val ref = if (root === relativeTo && dir.isNotEmpty()) root.ref.child(dir) else root.ref

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return if (relativeTo != null) {
            root.snapshot.child(path)
        } else {
            root.snapshot
        }.value() as? T ?: default
    }

    // Note: Value setting is *async*
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        scope.launch {
            if (removeIfDefault && value == default) {
                ref.remove()
            } else {
                ref.update(key to value)
            }
        }
    }
}

