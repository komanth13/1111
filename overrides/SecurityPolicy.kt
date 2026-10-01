package com.example.security

import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Build type, local email/profile fields and preferences never grant privileges. */
object SecurityPolicy {
    private val _accountAccess = MutableStateFlow(AccountAccess())
    val accountAccess = _accountAccess.asStateFlow()

    internal fun updateAccountAccess(access: AccountAccess) { _accountAccess.value = access }

    val adminToolsEnabled: Boolean
        get() = AccountAccessPolicy.isAdmin(_accountAccess.value, BuildConfig.ADMIN_EMAIL, System.currentTimeMillis())

    // Paid tiers/promos require a future server entitlement verifier, even for administrators.
    val localTierSimulationEnabled: Boolean get() = false
    val localPromoActivationEnabled: Boolean get() = false
}
