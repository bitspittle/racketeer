package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.firebase.database.ServerValue
import dev.bitspittle.firebase.database.update
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.model.GameContext
import kotlin.js.Date
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

sealed class Payload(val ctx: GameContext) {
    abstract val name: String
    val state: GameState = ctx.state // Grab state before external code resets it
    abstract fun addInto(values: MutableList<Pair<String, Any>>)

    class Crash(ctx: GameContext, val ex: Exception) : Payload(ctx) {
        override val name = "crashed"
        override fun addInto(values: MutableList<Pair<String, Any>>) {
            values.add("message" to (ex.message ?: "${ex::class.simpleName} thrown with no message"))
        }
    }
    class Abort(ctx: GameContext) : Payload(ctx) {
        override val name = "aborted"
        override fun addInto(values: MutableList<Pair<String, Any>>) {
            values.add("turn" to state.turn + 1)
            values.add("vp" to state.vp)
        }
    }
    class Finish(ctx: GameContext) : Payload(ctx) {
        override val name = "finished"
        override fun addInto(values: MutableList<Pair<String, Any>>) {
            values.add("vp" to state.vp)
        }
    }
}

private val DEFAULT_THROTTLE_DURATION = 1.minutes
// 2MB chosen somewhat randomly, in case finished games go long. Most games should be no more than 300 KB however.
private val DEFAULT_THROTTLE_SIZE = (2000 * 1024).toLong()

object Uploads {
    private val throttleDurations = mutableMapOf<KClass<out Payload>, Duration>()
    private val throttleSizes = mutableMapOf<KClass<out Payload>, Long>()
    private val nextAllowedUpload = mutableMapOf<KClass<out Payload>, Long>()

    fun registerThrottleDuration(payloadType: KClass<out Payload>, duration: Duration) {
        throttleDurations[payloadType] = duration
    }

    fun registerThrottleSize(payloadType: KClass<out Payload>, sizeBytes: Long) {
        throttleSizes[payloadType] = sizeBytes
    }

    suspend fun upload(payload: Payload) {
        try {
            val type = payload::class
            val uid = payload.ctx.firebase.auth.currentUser?.uid ?: return

            val now = Date.now().toLong()
            if (now < (nextAllowedUpload[type] ?: 0)) {
                payload.ctx.logger.debug("Upload (type = ${type.simpleName}) was throttled due to frequency.")
                return
            }
            nextAllowedUpload[type] = now + (throttleDurations[type] ?: DEFAULT_THROTTLE_DURATION).inWholeMilliseconds

            val snapshot = GameSnapshot.from(payload.ctx.describer, payload.state).encodeToYaml()
            if (snapshot.length.toLong() > (throttleSizes[type] ?: DEFAULT_THROTTLE_SIZE)) {
                payload.ctx.logger.debug("Upload (type = ${type.simpleName}) was throttled due to payload size.")
                return
            }

            val dbRef = payload.ctx.firebase.db.ref("games/${payload.name}").push()

            val keyValues = buildList {
                add("timestamp" to ServerValue.timestamp())
                add("version" to G.version)
                add("uid" to uid)
                add("snapshot" to snapshot)
                payload.addInto(this)
            }
            dbRef.update(keyValues)
        } catch (ignored: Throwable) {
            // Uploading information should never block the player from playing more games
            // If we lose information, so be it.
        }
    }
}