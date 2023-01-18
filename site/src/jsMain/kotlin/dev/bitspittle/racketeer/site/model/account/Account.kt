package dev.bitspittle.racketeer.site.model.account

import dev.bitspittle.racketeer.site.components.layouts.FirebaseData
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Account(
    val uid: String,
    val email: String?,
) {
    @Transient
    var isAdmin = false
        private set

    suspend fun updateAdmin(firebase: FirebaseData) {
        isAdmin = firebase.db.ref("/admins/$uid").get().exists()
    }
}