package com.example.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.BuildConfig
import com.example.security.AccountAccess
import com.example.security.SecurityPolicy
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AccountState(val uid: String = "", val email: String = "", val emailVerified: Boolean = false,
    val checking: Boolean = false) {
    val signedIn get() = uid.isNotBlank()
}

class AccountManager(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(AccountState())
    val state = _state.asStateFlow()
    val configured = BuildConfig.FIREBASE_API_KEY.isNotBlank() && BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
        BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
    val googleConfigured get() = configured && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
    private var refreshJob: Job? = null
    private var expiryJob: Job? = null
    private var generation = 0L

    private val auth: FirebaseAuth? = if (configured) {
        val options = FirebaseOptions.Builder().setApiKey(BuildConfig.FIREBASE_API_KEY)
            .setApplicationId(BuildConfig.FIREBASE_APP_ID).setProjectId(BuildConfig.FIREBASE_PROJECT_ID).build()
        val app = FirebaseApp.getApps(appContext).firstOrNull { it.name == APP_NAME }
            ?: FirebaseApp.initializeApp(appContext, options, APP_NAME)
        FirebaseAuth.getInstance(app)
    } else null

    init {
        SecurityPolicy.updateAccountAccess(AccountAccess())
        auth?.addAuthStateListener { refresh() }
    }

    /** Each refresh removes the old privilege before checking revocation/verification online. */
    fun refresh() {
        val ticket = ++generation
        refreshJob?.cancel()
        expiryJob?.cancel()
        SecurityPolicy.updateAccountAccess(AccountAccess())
        val user = auth?.currentUser
        _state.value = user.toState(checking = user != null)
        if (user == null) return
        refreshJob = scope.launch {
            try {
                user.reload().await()
                val token = user.getIdToken(true).await()
                if (ticket != generation || auth?.currentUser?.uid != user.uid) return@launch
                // SDK exchange validates identity; role must be a Boolean claim from the Admin SDK.
                val verified = user.isEmailVerified && token.claims["email_verified"] == true &&
                    token.claims["email"] == user.email && token.claims["sub"] == user.uid
                val validUntil = minOf(token.expirationTimestamp * 1000L, System.currentTimeMillis() + ROLE_CHECK_INTERVAL)
                SecurityPolicy.updateAccountAccess(AccountAccess(user.uid, user.email.orEmpty(), verified,
                    token.claims["admin"] == true, validUntil))
                _state.value = user.toState()
                expiryJob = scope.launch {
                    delay((validUntil - System.currentTimeMillis()).coerceAtLeast(0L))
                    if (ticket == generation) refresh()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (ticket == generation) {
                    SecurityPolicy.updateAccountAccess(AccountAccess())
                    _state.value = auth?.currentUser.toState()
                }
            }
        }
    }

    suspend fun signInEmail(email: String, password: String) {
        requireAuth().signInWithEmailAndPassword(email.trim(), password).await()
        refresh()
    }

    suspend fun registerEmail(email: String, password: String) {
        require(password.length >= 8)
        val user = requireAuth().createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Account creation failed")
        refresh()
        user.sendEmailVerification().await()
    }

    suspend fun signInGoogle(activity: Activity) {
        check(googleConfigured)
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
        val result = CredentialManager.create(activity).getCredential(activity,
            GetCredentialRequest.Builder().addCredentialOption(option).build())
        val credential = result.credential
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        requireAuth().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        refresh()
    }

    suspend fun sendVerification() { (requireAuth().currentUser ?: error("Sign in first")).sendEmailVerification().await() }
    suspend fun resetPassword(email: String) { requireAuth().sendPasswordResetEmail(email.trim()).await() }

    suspend fun signOut() {
        ++generation
        refreshJob?.cancel()
        expiryJob?.cancel()
        SecurityPolicy.updateAccountAccess(AccountAccess())
        auth?.signOut()
        _state.value = AccountState()
        try { CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest()) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* Firebase session and privileges are already cleared. */ }
    }

    private fun requireAuth() = checkNotNull(auth) { "Account service is not configured" }
    private fun FirebaseUser?.toState(checking: Boolean = false) =
        AccountState(this?.uid.orEmpty(), this?.email.orEmpty(), this?.isEmailVerified == true, checking)

    companion object {
        private const val APP_NAME = "SlimTrackIdentity"
        private const val ROLE_CHECK_INTERVAL = 5 * 60 * 1000L
    }
}
