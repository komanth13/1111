package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminPaymentDetails
import com.example.data.model.PaymentMethodType
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.util.StringKey
import com.example.util.appString

@Composable
fun AdminPaymentRequisitesSection(
    paymentDetails: AdminPaymentDetails,
    onSavePaymentDetails: (AdminPaymentDetails) -> Unit
) {
    val context = LocalContext.current

    var monoCard by remember(paymentDetails) { mutableStateOf(paymentDetails.monobankCard) }
    var monoJar by remember(paymentDetails) { mutableStateOf(paymentDetails.monobankJarUrl) }
    var paypalEmail by remember(paymentDetails) { mutableStateOf(paymentDetails.paypalEmail) }
    var paypalMe by remember(paymentDetails) { mutableStateOf(paymentDetails.paypalMeUrl) }
    var usdtAddr by remember(paymentDetails) { mutableStateOf(paymentDetails.usdtAddress) }
    var usdtNet by remember(paymentDetails) { mutableStateOf(paymentDetails.usdtNetwork) }
    var btcAddr by remember(paymentDetails) { mutableStateOf(paymentDetails.btcAddress) }
    var bankCard by remember(paymentDetails) { mutableStateOf(paymentDetails.bankCardNumber) }
    var bankHolder by remember(paymentDetails) { mutableStateOf(paymentDetails.bankHolderName) }
    var iban by remember(paymentDetails) { mutableStateOf(paymentDetails.ibanNumber) }
    var supportTg by remember(paymentDetails) { mutableStateOf(paymentDetails.supportTelegram) }
    var supportMail by remember(paymentDetails) { mutableStateOf(paymentDetails.supportEmail) }
    var enabledMethods by remember(paymentDetails) { mutableStateOf(paymentDetails.enabledMethodIds.toSet()) }
    val paymentDetailsSavedText = appString(StringKey.ADMIN_PAYMENT_DETAILS_SAVED)

    fun toggleMethod(methodId: String) {
        enabledMethods = if (enabledMethods.contains(methodId)) {
            enabledMethods - methodId
        } else {
            enabledMethods + methodId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_payment_requisites_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Info card explaining international & Ukrainian coverage
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = appString(StringKey.ADMIN_PAYMENT_SETTINGS_TITLE),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appString(StringKey.ADMIN_PAYMENT_SETTINGS_DESC),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Enabled Payment Methods toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = appString(StringKey.ADMIN_PAYMENT_METHODS_ENABLED),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = appString(StringKey.ADMIN_PAYMENT_METHODS_HINT),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                PaymentMethodType.entries.forEach { method ->
                    val isChecked = enabledMethods.contains(method.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { toggleMethod(method.id) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = method.title, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = method.category, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { toggleMethod(method.id) }
                        )
                    }
                }
            }
        }

        // Monobank (Ukraine) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐈", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKey.ADMIN_MONOBANK_SECTION), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = monoCard,
                    onValueChange = { monoCard = it },
                    label = { Text(appString(StringKey.ADMIN_MONOBANK_CARD_LABEL)) },
                    placeholder = { Text("5375 **** **** 1234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = monoJar,
                    onValueChange = { monoJar = it },
                    label = { Text(appString(StringKey.ADMIN_MONOBANK_JAR_LABEL)) },
                    placeholder = { Text("https://send.monobank.ua/jar/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
            }
        }

        // PayPal (International) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌍", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKey.ADMIN_PAYPAL_SECTION), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = paypalEmail,
                    onValueChange = { paypalEmail = it },
                    label = { Text(appString(StringKey.ADMIN_PAYPAL_EMAIL_LABEL)) },
                    placeholder = { Text("your.paypal@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = paypalMe,
                    onValueChange = { paypalMe = it },
                    label = { Text(appString(StringKey.ADMIN_PAYPAL_ME_LABEL)) },
                    placeholder = { Text("https://paypal.me/yourname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
            }
        }

        // Crypto USDT / BTC Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKey.ADMIN_CRYPTO_SECTION), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = usdtAddr,
                    onValueChange = { usdtAddr = it },
                    label = { Text(appString(StringKey.ADMIN_USDT_ADDRESS_LABEL)) },
                    placeholder = { Text("TRC-20 адрес (начинается с T...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = usdtNet,
                    onValueChange = { usdtNet = it },
                    label = { Text(appString(StringKey.ADMIN_USDT_NETWORK_LABEL)) },
                    placeholder = { Text("TRC-20") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = btcAddr,
                    onValueChange = { btcAddr = it },
                    label = { Text(appString(StringKey.ADMIN_BTC_ADDRESS_LABEL)) },
                    placeholder = { Text("bc1q...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
            }
        }

        // Bank Cards & IBAN
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💳", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKey.ADMIN_BANK_SECTION), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = bankCard,
                    onValueChange = { bankCard = it },
                    label = { Text(appString(StringKey.ADMIN_CARD_NUMBER_LABEL)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = bankHolder,
                    onValueChange = { bankHolder = it },
                    label = { Text(appString(StringKey.ADMIN_CARD_HOLDER_LABEL)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = iban,
                    onValueChange = { iban = it },
                    label = { Text(appString(StringKey.ADMIN_IBAN_LABEL)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
            }
        }

        // Support contacts for confirmation and receipts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📩", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKey.ADMIN_SUPPORT_CONTACTS), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = supportTg,
                    onValueChange = { supportTg = it },
                    label = { Text(appString(StringKey.ADMIN_TELEGRAM_LABEL)) },
                    placeholder = { Text("@your_support_username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = supportMail,
                    onValueChange = { supportMail = it },
                    label = { Text(appString(StringKey.ADMIN_SUPPORT_EMAIL_LABEL)) },
                    placeholder = { Text("avramenkoandron@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = AdminPaymentDetails(
                    paypalEmail = paypalEmail.trim(),
                    paypalMeUrl = paypalMe.trim(),
                    usdtAddress = usdtAddr.trim(),
                    usdtNetwork = usdtNet.trim(),
                    btcAddress = btcAddr.trim(),
                    monobankCard = monoCard.trim(),
                    monobankJarUrl = monoJar.trim(),
                    bankCardNumber = bankCard.trim(),
                    bankHolderName = bankHolder.trim(),
                    ibanNumber = iban.trim(),
                    supportTelegram = supportTg.trim(),
                    supportEmail = supportMail.trim(),
                    enabledMethodIds = enabledMethods
                )
                onSavePaymentDetails(updated)
                Toast.makeText(context, paymentDetailsSavedText, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(com.example.ui.theme.SlimTrackSizes.buttonLarge)
                .testTag("save_admin_payment_details_button"),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(appString(StringKey.ADMIN_SAVE_PAYMENT_DETAILS), fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}