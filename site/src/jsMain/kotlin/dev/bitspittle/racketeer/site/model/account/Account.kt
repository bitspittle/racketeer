package dev.bitspittle.racketeer.site.model.account

import kotlinx.serialization.Serializable

@Serializable
class Account(
    val uid: String,
    val email: String?
)