package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.data.model.ActiveGuestAccess
import com.example.data.model.AdminPaymentDetails
import com.example.data.model.AppConfig
import com.example.data.model.AppFeature
import com.example.data.model.GuestInvitePass
import com.example.data.model.PaymentMethodType
import com.example.data.model.PaymentReceipt
import com.example.data.model.SubscriptionTier
import com.example.security.SecurityPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.Locale

class AdminConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentConfig = MutableStateFlow(loadSavedConfig())
    val currentConfig: StateFlow<AppConfig> = _currentConfig.asStateFlow()

    private val _userTier = MutableStateFlow(loadSavedTier())
    val userTier: StateFlow<SubscriptionTier> = _userTier.asStateFlow()

    private val _activeGuestAccess = MutableStateFlow(loadActiveGuestAccess())
    val activeGuestAccess: StateFlow<ActiveGuestAccess?> = _activeGuestAccess.asStateFlow()

    private val _savedInvites = MutableStateFlow(loadSavedInvites())
    val savedInvites: StateFlow<List<GuestInvitePass>> = _savedInvites.asStateFlow()

    private val _paymentDetails = MutableStateFlow(loadPaymentDetails())
    val paymentDetails: StateFlow<AdminPaymentDetails> = _paymentDetails.asStateFlow()

    private val _latestReceipt = MutableStateFlow(loadLatestReceipt())
    val latestReceipt: StateFlow<PaymentReceipt?> = _latestReceipt.asStateFlow()

    init {
        if (!prefs.getBoolean("verified_account_migration_v1", false)) {
            // Remove entitlements that could have been created by older insecure client-side flows.
            prefs.edit()
                .remove(KEY_ACTIVE_TIER)
                .remove(KEY_ACTIVE_GUEST_ACCESS)
                .remove(KEY_SAVED_INVITES)
                .remove(KEY_CONFIG_JSON)
                .remove(KEY_SOURCE_NAME)
                .putBoolean("verified_account_migration_v1", true)
                .apply()
        }
        checkAndUpdateGuestAccess()
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            SecurityPolicy.accountAccess.collect {
                _userTier.value = if (SecurityPolicy.adminToolsEnabled) SubscriptionTier.ADMIN else SubscriptionTier.FREE
                _currentConfig.value = loadSavedConfig()
                _savedInvites.value = loadSavedInvites()
                _activeGuestAccess.value = null
            }
        }
    }

    private fun loadSavedConfig(): AppConfig {
        if (!SecurityPolicy.adminToolsEnabled) return AppConfig()
        val savedJson = prefs.getString(KEY_CONFIG_JSON, null)
        val sourceName = prefs.getString(KEY_SOURCE_NAME, "Встроенная") ?: "Встроенная"
        if (!savedJson.isNullOrBlank()) {
            try {
                return AppConfig.fromJsonString(savedJson, sourceName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return AppConfig()
    }

    private fun loadSavedTier(): SubscriptionTier {
        if (!SecurityPolicy.localTierSimulationEnabled) return SubscriptionTier.FREE
        val tierId = prefs.getString(KEY_ACTIVE_TIER, SubscriptionTier.FREE.id) ?: SubscriptionTier.FREE.id
        return SubscriptionTier.fromId(tierId)
    }

    private fun loadActiveGuestAccess(): ActiveGuestAccess? {
        if (!SecurityPolicy.localPromoActivationEnabled) return null
        val json = prefs.getString(KEY_ACTIVE_GUEST_ACCESS, null) ?: return null
        return ActiveGuestAccess.fromJson(json)
    }

    private fun loadSavedInvites(): List<GuestInvitePass> {
        if (!SecurityPolicy.adminToolsEnabled) return emptyList()
        val json = prefs.getString(KEY_SAVED_INVITES, null) ?: return defaultInitialInvites()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<GuestInvitePass>()
            for (i in 0 until arr.length()) {
                list.add(GuestInvitePass.fromJson(arr.getJSONObject(i)))
            }
            if (list.isEmpty()) defaultInitialInvites() else list
        } catch (e: Exception) {
            defaultInitialInvites()
        }
    }

    private fun defaultInitialInvites(): List<GuestInvitePass> {
        return listOf(
            GuestInvitePass(
                code = "FAMILY-VIP",
                tier = SubscriptionTier.PREMIUM,
                durationDays = 0,
                label = "Семейный доступ (Premium Навсегда 👑)",
                note = "Для родных и близких"
            ),
            GuestInvitePass(
                code = "PREMIUM-7D-GIFT",
                tier = SubscriptionTier.PREMIUM,
                durationDays = 7,
                label = "Пробный Premium (7 дней 🎁)",
                note = "Для привлечения новых клиентов"
            ),
            GuestInvitePass(
                code = "SLIM-7D-TRIAL",
                tier = SubscriptionTier.SLIM,
                durationDays = 7,
                label = "Гостевой Slim (7 дней)",
                note = "Стартовый тестовый период"
            )
        )
    }

    fun checkAndUpdateGuestAccess() {
        if (!SecurityPolicy.localPromoActivationEnabled) {
            if (_activeGuestAccess.value != null) _activeGuestAccess.value = null
            val verifiedTier = if (SecurityPolicy.adminToolsEnabled) SubscriptionTier.ADMIN else SubscriptionTier.FREE
            if (_userTier.value != verifiedTier) _userTier.value = verifiedTier
            return
        }
        val access = _activeGuestAccess.value ?: return
        if (access.isExpired) {
            _activeGuestAccess.value = null
            prefs.edit().remove(KEY_ACTIVE_GUEST_ACCESS).apply()
            if (_userTier.value != SubscriptionTier.ADMIN) {
                setTierInternal(SubscriptionTier.FREE)
            }
        } else {
            if (_userTier.value != SubscriptionTier.ADMIN && _userTier.value != access.tier) {
                _userTier.value = access.tier
                prefs.edit().putString(KEY_ACTIVE_TIER, access.tier.id).apply()
            }
        }
    }

    private fun setTierInternal(tier: SubscriptionTier) {
        _userTier.value = tier
        prefs.edit().putString(KEY_ACTIVE_TIER, tier.id).apply()
    }

    fun selectTierFromPaywall(tier: SubscriptionTier) {
        // The public paywall may only downgrade to FREE locally. Paid entitlements
        // must come from a verified source. Debug builds may simulate tiers.
        if (tier == SubscriptionTier.FREE) {
            revokeGuestAccess()
            _userTier.value = if (SecurityPolicy.adminToolsEnabled) SubscriptionTier.ADMIN else SubscriptionTier.FREE
            return
        }
        if (SecurityPolicy.localTierSimulationEnabled) {
            setTierInternal(tier)
        }
    }

    fun setUserTierForDebug(tier: SubscriptionTier) {
        check(SecurityPolicy.localTierSimulationEnabled) {
            "Local tier simulation is requires a verified administrator account"
        }
        setTierInternal(tier)
    }

    fun isFeatureUnlocked(feature: AppFeature): Boolean {
        checkAndUpdateGuestAccess()
        return _currentConfig.value.isFeatureUnlocked(feature, _userTier.value)
    }

    fun getRequiredTier(feature: AppFeature): SubscriptionTier {
        return _currentConfig.value.getRequiredTier(feature)
    }

    // --- Guest Invites Management ---

    fun createInvite(tier: SubscriptionTier, durationDays: Int, label: String, customCode: String? = null): GuestInvitePass {
        check(SecurityPolicy.adminToolsEnabled) { "Invite creation is requires a verified administrator account" }
        require(tier == SubscriptionTier.SLIM || tier == SubscriptionTier.PREMIUM) { "Only paid user tiers may be invited" }
        require(durationDays == 0 || durationDays in 1..365) { "Invite duration must be 1..365 days or 0 for debug lifetime" }

        val code = if (!customCode.isNullOrBlank()) {
            customCode.trim().uppercase(Locale.ROOT).also { normalized ->
                require(normalized.matches(Regex("[A-Z0-9-]{6,64}"))) { "Promo code contains unsupported characters" }
            }
        } else {
            when {
                durationDays <= 0 -> GuestInvitePass.generateCode("${tier.id.uppercase(Locale.ROOT)}-LIFE")
                else -> GuestInvitePass.generateCode("${tier.id.uppercase(Locale.ROOT)}-${durationDays}D")
            }
        }

        val invite = GuestInvitePass(
            code = code,
            tier = tier,
            durationDays = durationDays,
            label = label.ifBlank {
                if (durationDays <= 0) "Семейный доступ (${tier.defaultName} Навсегда)"
                else "Гостевой ${tier.defaultName} ($durationDays дн.)"
            }
        )

        val updated = _savedInvites.value.toMutableList()
        // Replace if code exists, else prepend
        updated.removeAll { it.code.equals(code, ignoreCase = true) }
        updated.add(0, invite)
        _savedInvites.value = updated
        saveInvitesToPrefs(updated)
        return invite
    }

    fun deleteInvite(code: String) {
        check(SecurityPolicy.adminToolsEnabled) { "Invite deletion is requires a verified administrator account" }
        val updated = _savedInvites.value.filterNot { it.code.equals(code, ignoreCase = true) }
        _savedInvites.value = updated
        saveInvitesToPrefs(updated)
    }

    private fun saveInvitesToPrefs(invites: List<GuestInvitePass>) {
        val arr = JSONArray()
        invites.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_SAVED_INVITES, arr.toString()).apply()
    }

    fun activateCodeOrUrl(input: String): Result<ActiveGuestAccess> {
        if (!SecurityPolicy.localPromoActivationEnabled) {
            return Result.failure(IllegalStateException(
                "Промокоды требуют серверной проверки и отключены в production-сборке"
            ))
        }

        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Код или ссылка не могут быть пустыми"))
        }

        val resolvedCode = try {
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("slimtrack://")) {
                val uri = Uri.parse(trimmed)
                val isAllowedLink =
                    (uri.scheme == "slimtrack" && uri.host == "invite") ||
                    (uri.scheme == "https" && uri.host == "slimtrack.app" && uri.path?.startsWith("/invite") == true)
                if (!isAllowedLink) {
                    return Result.failure(IllegalArgumentException("Неверная ссылка SlimTrack"))
                }
                uri.getQueryParameter("code")?.trim()?.uppercase(Locale.ROOT)
                    ?: return Result.failure(IllegalArgumentException("В ссылке отсутствует код"))
            } else {
                trimmed.uppercase(Locale.ROOT)
            }
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Неверный формат ссылки"))
        }

        // Fail closed: tier/duration from a URL are never trusted. Only an exact invite
        // already created by the debug/admin tool may grant access.
        val invite = _savedInvites.value.firstOrNull { it.code.equals(resolvedCode, ignoreCase = true) }
            ?: return Result.failure(IllegalArgumentException("Промокод недействителен или истёк"))

        val activatedAt = System.currentTimeMillis()
        val expiresAt = if (invite.isLifetime) {
            0L
        } else {
            activatedAt + (invite.durationDays.toLong() * 24L * 60L * 60L * 1000L)
        }

        val activeAccess = ActiveGuestAccess(
            code = invite.code,
            tier = invite.tier,
            isLifetime = invite.isLifetime,
            activatedAt = activatedAt,
            expiresAt = expiresAt,
            label = invite.label
        )

        _activeGuestAccess.value = activeAccess
        prefs.edit().putString(KEY_ACTIVE_GUEST_ACCESS, activeAccess.toJson().toString()).apply()
        if (_userTier.value != SubscriptionTier.ADMIN) {
            setTierInternal(invite.tier)
        }

        return Result.success(activeAccess)
    }

    fun revokeGuestAccess() {
        _activeGuestAccess.value = null
        prefs.edit().remove(KEY_ACTIVE_GUEST_ACCESS).apply()
        if (_userTier.value != SubscriptionTier.ADMIN) {
            setTierInternal(SubscriptionTier.FREE)
        }
    }

    // --- JSON Config & Reset ---

    fun applyJsonConfig(jsonContent: String, sourceName: String): Result<AppConfig> {
        if (!SecurityPolicy.adminToolsEnabled) {
            return Result.failure(SecurityException("Admin configuration is requires a verified administrator account"))
        }
        return try {
            val parsed = AppConfig.fromJsonString(jsonContent, sourceName).copy(
                lastUpdated = System.currentTimeMillis(),
                source = sourceName
            )
            _currentConfig.value = parsed
            prefs.edit()
                .putString(KEY_CONFIG_JSON, parsed.toJsonString())
                .putString(KEY_SOURCE_NAME, sourceName)
                .apply()
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resetToDefaults(): AppConfig {
        check(SecurityPolicy.adminToolsEnabled) { "Admin configuration is requires a verified administrator account" }
        val defaultConfig = AppConfig()
        _currentConfig.value = defaultConfig
        prefs.edit()
            .remove(KEY_CONFIG_JSON)
            .putString(KEY_SOURCE_NAME, "Встроенная")
            .apply()
        return defaultConfig
    }

    private fun loadPaymentDetails(): AdminPaymentDetails {
        val json = prefs.getString(KEY_PAYMENT_DETAILS, null) ?: return AdminPaymentDetails()
        return AdminPaymentDetails.fromJson(json)
    }

    private fun loadLatestReceipt(): PaymentReceipt? {
        val json = prefs.getString(KEY_LATEST_RECEIPT, null) ?: return null
        val receipt = PaymentReceipt.fromJson(json) ?: return null
        // Legacy client-generated "paid" receipts are not trusted in release builds.
        if (!SecurityPolicy.localTierSimulationEnabled && receipt.status != PaymentReceipt.STATUS_PENDING_VERIFICATION) {
            return null
        }
        return receipt
    }

    fun updatePaymentDetails(details: AdminPaymentDetails) {
        check(SecurityPolicy.adminToolsEnabled) { "Payment requisites editing is requires a verified administrator account" }
        _paymentDetails.value = details
        prefs.edit().putString(KEY_PAYMENT_DETAILS, details.toJson().toString()).apply()
    }

    fun submitPaymentForVerification(
        tier: SubscriptionTier,
        amount: String,
        methodType: PaymentMethodType,
        payerNote: String
    ): PaymentReceipt {
        require(tier == SubscriptionTier.SLIM || tier == SubscriptionTier.PREMIUM) {
            "Only paid tiers can be submitted for payment verification"
        }

        val receipt = PaymentReceipt(
            tier = tier,
            amount = amount,
            methodType = methodType,
            payerNoteOrTx = payerNote,
            timestamp = System.currentTimeMillis(),
            status = PaymentReceipt.STATUS_PENDING_VERIFICATION
        )
        _latestReceipt.value = receipt
        prefs.edit().putString(KEY_LATEST_RECEIPT, receipt.toJson().toString()).apply()

        // IMPORTANT: no local tier activation here. A future backend / Google Play
        // Billing verifier must confirm the transaction before granting access.
        return receipt
    }

    fun clearLatestReceipt() {
        _latestReceipt.value = null
        prefs.edit().remove(KEY_LATEST_RECEIPT).apply()
    }

    fun exportConfigJson(): String {
        check(SecurityPolicy.adminToolsEnabled) { "Admin configuration export is requires a verified administrator account" }
        return _currentConfig.value.toJsonString(indentSpaces = 2)
    }

    companion object {
        private const val PREFS_NAME = "slimtrack_admin_config_prefs"
        private const val KEY_CONFIG_JSON = "saved_app_config_json"
        private const val KEY_SOURCE_NAME = "saved_config_source_name"
        private const val KEY_ACTIVE_TIER = "active_subscription_tier"
        private const val KEY_ACTIVE_GUEST_ACCESS = "active_guest_access_data"
        private const val KEY_SAVED_INVITES = "saved_guest_invites_list"
        private const val KEY_PAYMENT_DETAILS = "admin_payment_details_data"
        private const val KEY_LATEST_RECEIPT = "latest_payment_receipt_data"

        @Volatile
        private var INSTANCE: AdminConfigManager? = null

        fun getInstance(context: Context): AdminConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdminConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
