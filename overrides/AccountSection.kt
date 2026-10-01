package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.AccountManager
import com.example.security.SecurityPolicy
import com.example.util.AppLanguage
import com.example.util.appLanguage
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable fun AccountSection(manager: AccountManager) {
    val language = appLanguage()
    fun tr(ru: String, uk: String, en: String) = when (language) {
        AppLanguage.RU -> ru; AppLanguage.UK -> uk; AppLanguage.EN -> en
    }
    val state by manager.state.collectAsStateWithLifecycle()
    val access by SecurityPolicy.accountAccess.collectAsStateWithLifecycle()
    val isAdmin = access.uid == state.uid && SecurityPolicy.adminToolsEnabled
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var registering by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    fun action(success: String = "", block: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = ""
        scope.launch {
            try {
                block()
                password = ""
                confirmation = ""
                message = success
            } catch (_: GetCredentialCancellationException) {
                message = tr("Вход отменён.", "Вхід скасовано.", "Sign-in cancelled.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: FirebaseNetworkException) {
                message = tr("Проверьте подключение к интернету.", "Перевірте підключення до інтернету.", "Check your internet connection.")
            } catch (_: FirebaseTooManyRequestsException) {
                message = tr("Слишком много попыток. Повторите позже.", "Забагато спроб. Повторіть пізніше.", "Too many attempts. Try again later.")
            } catch (_: Exception) {
                message = tr("Не удалось выполнить действие. Проверьте данные и повторите попытку.",
                    "Не вдалося виконати дію. Перевірте дані та повторіть спробу.", "Could not complete the action. Check your details and try again.")
            } finally { busy = false }
        }
    }
    Card(modifier = Modifier.fillMaxWidth().testTag("account_section")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tr("Аккаунт", "Акаунт", "Account"), style = MaterialTheme.typography.titleMedium)
            Text(tr("Дневник сохраняется на этом устройстве. Вход не удаляет ваши записи.",
                "Щоденник зберігається на цьому пристрої. Вхід не видаляє ваші записи.",
                "Your diary stays on this device. Signing in does not erase your entries."), style = MaterialTheme.typography.bodySmall)
            if (!manager.configured) {
                Text(tr("Вы пользуетесь Free. Вход в аккаунт пока недоступен.", "Ви користуєтеся Free. Вхід в акаунт поки недоступний.",
                    "You are using Free. Account sign-in is not available yet."), modifier = Modifier.testTag("account_not_configured"))
            } else if (state.signedIn) {
                Text(state.email)
                Text(if (isAdmin) tr("Администратор", "Адміністратор", "Administrator") else "Free",
                    modifier = Modifier.testTag("account_role"))
                if (state.checking) Text(tr("Проверяем доступ…", "Перевіряємо доступ…", "Checking access…"))
                if (!state.emailVerified) {
                    Text(tr("Подтвердите email по ссылке в письме.", "Підтвердьте email за посиланням у листі.", "Verify your email using the link in the message."))
                    OutlinedButton(enabled = !busy, onClick = {
                        action(tr("Письмо отправлено.", "Лист надіслано.", "Email sent.")) { manager.sendVerification() }
                    }) { Text(tr("Отправить письмо ещё раз", "Надіслати лист ще раз", "Resend verification")) }
                }
                OutlinedButton(enabled = !busy && !state.checking, onClick = { manager.refresh() }) {
                    Text(tr("Обновить статус аккаунта", "Оновити статус акаунта", "Refresh account status"))
                }
                OutlinedButton(enabled = !busy, onClick = { action { manager.signOut(); email = "" } }) {
                    Text(tr("Выйти", "Вийти", "Sign out"))
                }
            } else {
                Text(tr("Новый аккаунт начинает с Free.", "Новий акаунт починає з Free.", "New accounts start on Free."))
                if (manager.googleConfigured) {
                    OutlinedButton(enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("account_google_signin"), onClick = {
                        val activity = context.findActivity()
                        if (activity != null) action { manager.signInGoogle(activity) }
                    }) { Text(tr("Войти через Google", "Увійти через Google", "Sign in with Google")) }
                }
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !busy,
                    modifier = Modifier.fillMaxWidth().testTag("account_email"))
                OutlinedTextField(password, { password = it }, label = { Text(tr("Пароль", "Пароль", "Password")) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("account_password"))
                if (registering) {
                    Text(tr("Не менее 8 символов.", "Щонайменше 8 символів.", "At least 8 characters."))
                    OutlinedTextField(confirmation, { confirmation = it }, label = { Text(tr("Повторите пароль", "Повторіть пароль", "Confirm password")) },
                        singleLine = true, visualTransformation = PasswordVisualTransformation(), enabled = !busy,
                        modifier = Modifier.fillMaxWidth())
                }
                val validEmail = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
                Button(enabled = !busy && validEmail && password.isNotBlank() &&
                    (!registering || password.length >= 8 && password == confirmation),
                    modifier = Modifier.fillMaxWidth().testTag("account_email_submit"), onClick = {
                        if (registering) action(tr("Аккаунт создан. Подтвердите email по письму.",
                            "Акаунт створено. Підтвердьте email у листі.", "Account created. Check your email to verify it.")) {
                            manager.registerEmail(email, password)
                        } else action { manager.signInEmail(email, password) }
                    }) { Text(if (registering) tr("Зарегистрироваться", "Зареєструватися", "Create account") else tr("Войти", "Увійти", "Sign in")) }
                TextButton(enabled = !busy, onClick = { registering = !registering; message = ""; password = ""; confirmation = "" }) {
                    Text(if (registering) tr("Уже есть аккаунт", "Вже є акаунт", "I already have an account")
                        else tr("Создать аккаунт", "Створити акаунт", "Create account"))
                }
                TextButton(enabled = !busy && validEmail, onClick = {
                    action(tr("Если этот email зарегистрирован, придёт письмо для сброса пароля.",
                        "Якщо цей email зареєстровано, надійде лист для скидання пароля.", "If this email is registered, you will receive a password reset message.")) {
                        manager.resetPassword(email)
                    }
                }) { Text(tr("Забыли пароль?", "Забули пароль?", "Forgot password?")) }
            }
            if (busy) CircularProgressIndicator()
            if (message.isNotBlank()) Text(message, modifier = Modifier.testTag("account_message"))
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
