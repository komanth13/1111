package com.example.security

import java.util.Locale

/** Only populated after Firebase has refreshed and accepted the account token online. */
data class AccountAccess(
    val uid: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val adminClaim: Boolean = false,
    val validUntilMillis: Long = 0L
)

object AccountAccessPolicy {
    fun isAdmin(access: AccountAccess, administratorEmail: String, nowMillis: Long): Boolean =
        access.uid.isNotBlank() && access.emailVerified && access.adminClaim &&
            administratorEmail.isNotBlank() && normalize(access.email) == normalize(administratorEmail) &&
            nowMillis < access.validUntilMillis

    private fun normalize(email: String) = email.trim().lowercase(Locale.ROOT)
}
