package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Switch
import com.example.data.model.AdminPaymentDetails
import com.example.data.model.AppConfig
import com.example.data.model.GuestInvitePass
import com.example.data.model.PaymentMethodType
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.CarbRose
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.util.AppUpdaterHelper
import com.example.util.StringKey
import com.example.util.appString

@Composable
fun AdminPanelDialog(
    currentTier: SubscriptionTier,
    appConfig: AppConfig,
    savedInvites: List<GuestInvitePass>,
    paymentDetails: AdminPaymentDetails,
    onSelectTier: (SubscriptionTier) -> Unit,
    onImportConfig: (jsonText: String, fileName: String) -> Unit,
    onResetConfig: () -> Unit,
    onCreateInvite: (tier: SubscriptionTier, durationDays: Int, label: String, customCode: String?) -> GuestInvitePass,
    onDeleteInvite: (code: String) -> Unit,
    onUpdatePaymentDetails: (AdminPaymentDetails) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Гости & Семья, 1 = Оплата & Реквизиты, 2 = Файлы и Тарифы
    var isJsonViewerExpanded by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImportSuccess by remember { mutableStateOf(true) }

    // State for creating custom invite
    var showCustomInviteDialog by remember { mutableStateOf(false) }
    var customTier by remember { mutableStateOf(SubscriptionTier.PREMIUM) }
    var customDays by remember { mutableIntStateOf(7) }
    var customLabel by remember { mutableStateOf("") }
    var customCodeInput by remember { mutableStateOf("") }

    // File picker launcher for .json or any file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    val fileName = uri.lastPathSegment ?: "imported_config.json"
                    onImportConfig(content, fileName)
                    importStatusMessage = "Конфигурация из файла успешно загружена и применена!"
                    isImportSuccess = true
                    Toast.makeText(context, "Файл успешно загружен!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                importStatusMessage = "Ошибка чтения файла: ${e.localizedMessage}"
                isImportSuccess = false
                Toast.makeText(context, "Ошибка импорта: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // APK picker launcher for in-app updates
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            AppUpdaterHelper.installApkFromUri(context, uri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 700.dp)
                .clip(RoundedCornerShape(26.dp))
                .testTag("admin_panel_container"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FatAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = FatAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appString(StringKey.ADMIN_PANEL_TITLE),
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = appString(StringKey.ADMIN_PANEL_SUBTITLE),
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("admin_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = appString(StringKey.CLOSE))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick In-App Update Notice Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTab = 3 },
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appString(StringKey.ADMIN_INSTALL_APK_BUTTON),
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "APK ➔",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Tabs: 0) Семья & Гости, 1) Реквизиты, 2) Тарифы & JSON, 3) Обновление (.APK)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 4.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(appString(StringKey.ADMIN_TAB_FAMILY), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(appString(StringKey.ADMIN_TAB_REQUISITES), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(appString(StringKey.ADMIN_TAB_TIERS_JSON), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(appString(StringKey.ADMIN_TAB_UPDATE), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable content based on Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        // TAB 0: GUEST INVITES & FAMILY ACCESS

                        // Quick Family Access Banner
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = FatAmber.copy(alpha = 0.14f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FatAmber.copy(alpha = 0.45f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "👑", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = appString(StringKey.ADMIN_FAMILY_TITLE),
                                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = appString(StringKey.ADMIN_FAMILY_DESC),
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                val invite = onCreateInvite(
                                                    SubscriptionTier.PREMIUM,
                                                    0,
                                                    "Family VIP 👑",
                                                    "FAMILY-VIP"
                                                )
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, invite.toShareText())
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Family VIP"))
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = FatAmber,
                                                contentColor = Color(0xFF1E1E1E)
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF1E1E1E), modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = appString(StringKey.ADMIN_FAMILY_SHARE_BTN),
                                                color = Color(0xFF1E1E1E),
                                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("FamilyCode", "FAMILY-VIP")
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "FAMILY-VIP ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(appString(StringKey.ADMIN_COPY_CODE), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        // Quick 1-click generators for guests - Clean aligned rows for all screen widths
                        item {
                            Text(
                                text = appString(StringKey.ADMIN_QUICK_GUEST_LINKS),
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. 7 days Slim
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val invite = onCreateInvite(SubscriptionTier.SLIM, 7, "Slim 7d", null)
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, invite.toShareText())
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Slim 7d"))
                                        },
                                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.12f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = appString(StringKey.ADMIN_GUEST_SLIM_7D),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = androidx.compose.material3.MaterialTheme.shapes.small,
                                            color = EmeraldPrimary
                                        ) {
                                            Text(
                                                text = "${appString(StringKey.ADMIN_SHARE_ACTION)} ➔",
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // 2. 7 days Premium
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val invite = onCreateInvite(SubscriptionTier.PREMIUM, 7, "Premium 7d", null)
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, invite.toShareText())
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Premium 7d"))
                                        },
                                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = ProteinBlue.copy(alpha = 0.12f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ProteinBlue.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = appString(StringKey.ADMIN_GUEST_PREMIUM_7D),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = androidx.compose.material3.MaterialTheme.shapes.small,
                                            color = ProteinBlue
                                        ) {
                                            Text(
                                                text = "${appString(StringKey.ADMIN_SHARE_ACTION)} ➔",
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // 3. 30 days Premium
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val invite = onCreateInvite(SubscriptionTier.PREMIUM, 30, "Premium 30d", null)
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, invite.toShareText())
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Premium 30d"))
                                        },
                                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = FatAmber.copy(alpha = 0.14f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FatAmber.copy(alpha = 0.45f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = appString(StringKey.ADMIN_GUEST_PREMIUM_30D),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = androidx.compose.material3.MaterialTheme.shapes.small,
                                            color = FatAmber
                                        ) {
                                            Text(
                                                text = "${appString(StringKey.ADMIN_SHARE_ACTION)} ➔",
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E1E1E),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Create Custom Invite Button
                        item {
                            Button(
                                onClick = { showCustomInviteDialog = !showCustomInviteDialog },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(com.example.ui.theme.SlimTrackSizes.button),
                                shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = if (showCustomInviteDialog) Icons.Default.Close else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showCustomInviteDialog) appString(StringKey.ADMIN_CUSTOM_INVITE_HIDE) else appString(StringKey.ADMIN_CUSTOM_INVITE_CREATE),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            AnimatedVisibility(visible = showCustomInviteDialog) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Select Tier
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(SubscriptionTier.SLIM to "Slim ($20)", SubscriptionTier.PREMIUM to "Premium ($50)").forEach { (tier, name) ->
                                                val isSelected = customTier == tier
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { customTier = tier }
                                                        .border(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                                        ),
                                                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                                                ) {
                                                    Text(
                                                        text = name,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        modifier = Modifier.padding(8.dp),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Select Duration
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(3 to "3d", 7 to "7d", 14 to "14d", 30 to "30d", 0 to "VIP 👑").forEach { (days, label) ->
                                                val isSelected = customDays == days
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { customDays = days }
                                                        .border(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                            shape = androidx.compose.material3.MaterialTheme.shapes.small
                                                        ),
                                                    shape = androidx.compose.material3.MaterialTheme.shapes.small,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = customCodeInput,
                                            onValueChange = { customCodeInput = it },
                                            label = { Text(appString(StringKey.ADMIN_PROMO_CODE_LABEL)) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                val invite = onCreateInvite(
                                                    customTier,
                                                    customDays,
                                                    if (customDays <= 0) "VIP ${customTier.defaultName}" else "${customTier.defaultName} (${customDays}d)",
                                                    customCodeInput.ifBlank { null }
                                                )
                                                showCustomInviteDialog = false
                                                customCodeInput = ""
                                                Toast.makeText(context, "${invite.code} ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Text(appString(StringKey.ADD), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // List of Created Invites
                        item {
                            Text(
                                text = "${appString(StringKey.ADMIN_ACTIVE_INVITES_LIST)} (${savedInvites.size}):",
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(savedInvites) { invite ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (invite.isLifetime) FatAmber.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = invite.code,
                                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(androidx.compose.material3.MaterialTheme.shapes.extraSmall)
                                                    .background(
                                                        when (invite.tier) {
                                                            SubscriptionTier.SLIM -> EmeraldPrimary.copy(alpha = 0.2f)
                                                            SubscriptionTier.PREMIUM -> FatAmber.copy(alpha = 0.25f)
                                                            else -> Color.Gray.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = invite.tier.defaultName,
                                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (invite.tier) {
                                                        SubscriptionTier.SLIM -> EmeraldPrimary
                                                        SubscriptionTier.PREMIUM -> MaterialTheme.colorScheme.onSurface
                                                        else -> Color.Gray
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (invite.isLifetime) appString(StringKey.ADMIN_DURATION_FOREVER) else "${invite.durationDays}d",
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, invite.toShareText())
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, invite.code))
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = appString(StringKey.ADMIN_SHARE_ACTION), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("SlimTrackInvite", invite.toShareableUrl())
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "✓", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Link, contentDescription = appString(StringKey.ADMIN_COPY_CODE), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                                        }

                                        if (invite.code != "FAMILY-VIP") {
                                            IconButton(
                                                onClick = { onDeleteInvite(invite.code) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = appString(StringKey.DELETE), tint = CarbRose, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedTab == 1) {
                        // TAB 1: PAYMENT REQUISITES & METHODS
                        item {
                            AdminPaymentRequisitesSection(
                                paymentDetails = paymentDetails,
                                onSavePaymentDetails = onUpdatePaymentDetails
                            )
                        }
                    } else if (selectedTab == 2) {
                        // TAB 2: CONFIGURATION & TIERS & JSON FILE

                        // Status Notification if any
                        if (importStatusMessage != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isImportSuccess) EmeraldPrimary.copy(alpha = 0.15f) else CarbRose.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isImportSuccess) Icons.Default.Check else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (isImportSuccess) EmeraldPrimary else CarbRose
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = importStatusMessage ?: "",
                                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isImportSuccess) EmeraldPrimary else CarbRose
                                        )
                                    }
                                }
                            }
                        }

                        // Section 1: File Import / Upload
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = appString(StringKey.ADMIN_FILE_IMPORT_TITLE),
                                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = appString(StringKey.ADMIN_FILE_IMPORT_DESC),
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            filePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(com.example.ui.theme.SlimTrackSizes.button)
                                            .testTag("admin_upload_file_button"),
                                        shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = appString(StringKey.ADMIN_IMPORT_BUTTON),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Section 3: Current Config Metadata & Actions
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = appString(StringKey.ADMIN_SOURCE_LABEL), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = appConfig.source, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = appString(StringKey.ADMIN_VERSION_LABEL), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "v${appConfig.version}", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val json = appConfig.toJsonString()
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("SlimTrackConfig", json)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "JSON ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "JSON", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val json = appConfig.toJsonString()
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, json)
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "JSON")
                                                context.startActivity(shareIntent)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = appString(StringKey.ADMIN_SHARE_ACTION), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }

                                        OutlinedButton(
                                            onClick = onResetConfig,
                                            modifier = Modifier.weight(1f),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = { isJsonViewerExpanded = !isJsonViewerExpanded },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                    ) {
                                        Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = ".JSON Config",
                                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    AnimatedVisibility(visible = isJsonViewerExpanded) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Text(
                                                text = appConfig.toJsonString(),
                                                fontFamily = FontFamily.Monospace,
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                lineHeight = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedTab == 3) {
                        // TAB 3: IN-APP APK UPDATE & DIRECT INSTALLATION
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = EmeraldPrimary.copy(alpha = 0.12f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = appString(StringKey.ADMIN_INSTALL_APK_BUTTON),
                                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = appString(StringKey.ADMIN_TAB_UPDATE),
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = appString(StringKey.ADMIN_INSTALL_APK_DESC),
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            apkPickerLauncher.launch(arrayOf("*/*"))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(com.example.ui.theme.SlimTrackSizes.button)
                                            .testTag("admin_pick_apk_button"),
                                        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = appString(StringKey.ADMIN_INSTALL_APK_BUTTON),
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Important Rule Note Card (No uninstall needed)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = appString(StringKey.ADMIN_APK_NO_DELETE_NOTE),
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Config and Customization File Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = appString(StringKey.ADMIN_FILE_IMPORT_TITLE),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = appString(StringKey.ADMIN_FILE_IMPORT_DESC),
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                filePickerLauncher.launch(arrayOf("application/json", "*/*"))
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Text(appString(StringKey.ADMIN_IMPORT_BUTTON), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("SlimTrackConfig", appConfig.toJsonString()))
                                                Toast.makeText(context, "JSON ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                                        ) {
                                            Text(appString(StringKey.ADMIN_EXPORT_BUTTON), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
