package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLevel
import com.example.data.model.AppFeature
import com.example.data.model.Gender
import com.example.data.model.GoalPace
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.ui.components.BodyAnalysisResultCard
import com.example.ui.components.AccountSection
import com.example.ui.components.ExportReportCard
import com.example.ui.components.LanguageDropdownSelector
import com.example.ui.theme.CarbRose
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.ui.theme.WaterBlue
import com.example.ui.viewmodel.FitnessViewModel
import com.example.util.CalorieCalculator
import com.example.util.AppLanguage
import com.example.util.AppUpdaterHelper
import com.example.util.LocalizationManager
import com.example.util.ProfileField
import com.example.util.ProfileValidator
import com.example.util.StringKey
import com.example.util.appString

@Composable
fun ProfileScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val currentMeals by viewModel.currentMeals.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val adminToolsEnabled by viewModel.adminAccess.collectAsStateWithLifecycle()
    val currentTier by viewModel.userTier.collectAsStateWithLifecycle()
    val appConfig by viewModel.currentConfig.collectAsStateWithLifecycle()
    val activeGuestAccess by viewModel.activeGuestAccess.collectAsStateWithLifecycle()
    val currentLang = com.example.util.appLanguage()
    val latestReceipt by viewModel.latestReceipt.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    val fileName = uri.lastPathSegment ?: "config.json"
                    viewModel.importAdminConfigFile(content, fileName)
                    Toast.makeText(context, LocalizationManager.getString(StringKey.ADMIN_SUCCESS_IMPORT), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${LocalizationManager.getString(StringKey.ADMIN_ERROR_IMPORT)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            AppUpdaterHelper.installApkFromUri(context, uri)
        }
    }

    var name by remember(profile) { mutableStateOf(profile.name) }
    var ageStr by remember(profile) { mutableStateOf(profile.age.takeIf { it > 0 }?.toString().orEmpty()) }
    var heightStr by remember(profile) { mutableStateOf(profile.heightCm.takeIf { it > 0f }?.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }.orEmpty()) }
    var startWeightStr by remember(profile) { mutableStateOf(profile.startWeightKg.takeIf { it > 0f }?.toString().orEmpty()) }
    var currentWeightStr by remember(profile) { mutableStateOf(profile.currentWeightKg.takeIf { it > 0f }?.toString().orEmpty()) }
    var targetWeightStr by remember(profile) { mutableStateOf(profile.targetWeightKg.takeIf { it > 0f }?.toString().orEmpty()) }
    var gender by remember(profile) { mutableStateOf(profile.gender) }
    var activityLevel by remember(profile) { mutableStateOf(profile.activityLevel) }
    var stepsStr by remember(profile) { mutableStateOf(profile.averageDailySteps?.toString().orEmpty()) }
    var householdStr by remember(profile) { mutableStateOf(profile.householdMinutes?.toString().orEmpty()) }
    var goalPace by remember(profile) { mutableStateOf(profile.goalPace) }

    var calorieTargetStr by remember(profile) { mutableStateOf(profile.dailyCalorieTarget.takeIf { it > 0 }?.toString().orEmpty()) }
    var proteinTargetStr by remember(profile) { mutableStateOf(profile.proteinTargetGrams.takeIf { it > 0 }?.toString().orEmpty()) }
    var fatTargetStr by remember(profile) { mutableStateOf(profile.fatTargetGrams.takeIf { it > 0 }?.toString().orEmpty()) }
    var carbTargetStr by remember(profile) { mutableStateOf(profile.carbTargetGrams.takeIf { it > 0 }?.toString().orEmpty()) }
    var waterTargetStr by remember(profile) { mutableStateOf(profile.waterGoalMl.takeIf { it > 0 }?.toString().orEmpty()) }

    var isSavedNotification by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf(emptyMap<ProfileField, StringKey>()) }

    fun buildCandidateProfile(): UserProfile = profile.copy(
        name = name.trim(),
        age = ProfileValidator.parseInteger(ageStr) ?: 0,
        heightCm = ProfileValidator.parseDecimal(heightStr) ?: 0f,
        startWeightKg = ProfileValidator.parseDecimal(startWeightStr) ?: 0f,
        currentWeightKg = ProfileValidator.parseDecimal(currentWeightStr) ?: 0f,
        targetWeightKg = ProfileValidator.parseDecimal(targetWeightStr) ?: 0f,
        gender = gender,
        activityLevel = activityLevel,
        goalPace = goalPace,
        averageDailySteps = if (stepsStr.isBlank()) null else ProfileValidator.parseInteger(stepsStr) ?: -1,
        householdMinutes = if (householdStr.isBlank()) null else ProfileValidator.parseInteger(householdStr) ?: -1,
        dailyCalorieTarget = ProfileValidator.parseInteger(calorieTargetStr) ?: 0,
        proteinTargetGrams = ProfileValidator.parseInteger(proteinTargetStr) ?: 0,
        fatTargetGrams = ProfileValidator.parseInteger(fatTargetStr) ?: 0,
        carbTargetGrams = ProfileValidator.parseInteger(carbTargetStr) ?: 0,
        waterGoalMl = ProfileValidator.parseInteger(waterTargetStr) ?: 0
    )

    fun errorText(field: ProfileField): String? =
        validationErrors[field]?.let { LocalizationManager.getString(it, currentLang) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("profile_screen_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { viewModel.accountManager?.let { AccountSection(it) } }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appString(StringKey.PROFILE_TITLE),
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = appString(StringKey.PROFILE_SUBTITLE),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.material3.MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v3.5.0 ✓",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Auto calculate button banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_calc_banner"),
                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = appString(StringKey.PROFILE_SMART_CALC_TITLE),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = appString(StringKey.PROFILE_SMART_CALC_DESC),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = appString(StringKey.PROFILE_SAFE_CALC_NOTE),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }

                    Button(
                        onClick = {
                            val result = viewModel.autoCalculateAndSaveTargets(buildCandidateProfile())
                            validationErrors = result.errors
                            isSavedNotification = false
                        },
                        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .height(com.example.ui.theme.SlimTrackSizes.buttonCompact)
                            .testTag("auto_calc_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(appString(StringKey.PROFILE_RECALC_BTN), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. Subscription & Plan Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_subscription_card"),
                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (currentTier) {
                                            SubscriptionTier.FREE -> Color.Gray.copy(alpha = 0.2f)
                                            SubscriptionTier.SLIM -> EmeraldPrimary.copy(alpha = 0.2f)
                                            SubscriptionTier.PREMIUM -> FatAmber.copy(alpha = 0.25f)
                                            SubscriptionTier.ADMIN -> ProteinBlue.copy(alpha = 0.25f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentTier == SubscriptionTier.ADMIN) Icons.Default.Shield else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = when (currentTier) {
                                        SubscriptionTier.FREE -> Color.Gray
                                        SubscriptionTier.SLIM -> EmeraldPrimary
                                        SubscriptionTier.PREMIUM -> FatAmber
                                        SubscriptionTier.ADMIN -> ProteinBlue
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = appString(StringKey.CURRENT_PLAN),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when (currentTier) {
                                        SubscriptionTier.FREE -> appString(StringKey.PLAN_FREE)
                                        SubscriptionTier.SLIM -> appString(StringKey.PLAN_SLIM)
                                        SubscriptionTier.PREMIUM -> appString(StringKey.PLAN_PREMIUM)
                                        SubscriptionTier.ADMIN -> appString(StringKey.PLAN_ADMIN)
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openPaywall() },
                            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (currentTier) {
                                    SubscriptionTier.FREE -> EmeraldPrimary
                                    SubscriptionTier.SLIM -> FatAmber
                                    SubscriptionTier.PREMIUM -> MaterialTheme.colorScheme.primary
                                    SubscriptionTier.ADMIN -> ProteinBlue
                                }
                            ),
                            modifier = Modifier
                                .height(com.example.ui.theme.SlimTrackSizes.buttonCompact)
                                .testTag("profile_upgrade_button")
                        ) {
                            Text(
                                text = appString(StringKey.PROFILE_CHANGE_PLAN_BTN),
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = appString(StringKey.LEAD_MAGNET_SUBTITLE),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (latestReceipt != null) {
                        OutlinedButton(
                            onClick = { viewModel.openPaymentReceipt(latestReceipt!!) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(com.example.ui.theme.SlimTrackSizes.buttonCompact)
                                .testTag("view_payment_receipt_button"),
                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Payment check (#${latestReceipt?.orderId})",
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.openPaymentCheckout(if (currentTier == SubscriptionTier.SLIM) SubscriptionTier.PREMIUM else SubscriptionTier.SLIM) },
                            modifier = Modifier.weight(1f).height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = androidx.compose.material3.MaterialTheme.shapes.medium
                        ) {
                            Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(appString(StringKey.PROFILE_PAYMENT_BTN), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        if (viewModel.promoActivationEnabled) {
                            OutlinedButton(
                                onClick = { viewModel.openActivatePromoDialog() },
                                modifier = Modifier.weight(1f).height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = androidx.compose.material3.MaterialTheme.shapes.medium
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(appString(StringKey.PROFILE_PROMO_BTN), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // 2. Language Selector Dropdown
        item {
            LanguageDropdownSelector()
        }

        // 3. Admin & Configuration Card (verified administrator only)
        if (adminToolsEnabled) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_admin_card"),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(FatAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = FatAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = appString(StringKey.ADMIN_PANEL_TITLE),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
    
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
    
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = appString(StringKey.ADMIN_INSTALL_APK_DESC),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
    
                        Spacer(modifier = Modifier.height(10.dp))
    
                        // Primary Action: Install Update (.APK) directly
                        Button(
                            onClick = {
                                apkPickerLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = appString(StringKey.ADMIN_INSTALL_APK_BUTTON),
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
    
                        Spacer(modifier = Modifier.height(8.dp))
    
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = androidx.compose.material3.MaterialTheme.shapes.medium
                            ) {
                                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(".JSON Config", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
    
                            Button(
                                onClick = { viewModel.openAdminPanel() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(com.example.ui.theme.SlimTrackSizes.buttonCompact),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = FatAmber)
                            ) {
                                Text(
                                    text = "👑 ${appString(StringKey.ADMIN_TAB_FAMILY)}",
                                    color = Color.Black,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
    
        }
        // Profile Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = appString(StringKey.PROFILE_PERSONAL_DATA),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (validationErrors.isNotEmpty()) {
                        Text(
                            text = appString(StringKey.PROFILE_VALIDATION_FIX_ERRORS),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Gender Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Gender.entries.forEach { g ->
                            val isSelected = gender == g
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable { gender = g }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (g == Gender.MALE) appString(StringKey.GENDER_MALE) else appString(StringKey.GENDER_FEMALE),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text(appString(StringKey.PROFILE_AGE)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_age_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey(ProfileField.AGE),
                            supportingText = errorText(ProfileField.AGE)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text(appString(StringKey.PROFILE_HEIGHT)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_height_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey(ProfileField.HEIGHT),
                            supportingText = errorText(ProfileField.HEIGHT)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startWeightStr,
                            onValueChange = { startWeightStr = it },
                            label = { Text(appString(StringKey.PROFILE_START_WEIGHT)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = validationErrors.containsKey(ProfileField.START_WEIGHT),
                            supportingText = errorText(ProfileField.START_WEIGHT)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = currentWeightStr,
                            onValueChange = { currentWeightStr = it },
                            label = { Text(appString(StringKey.PROFILE_CURRENT_WEIGHT)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = validationErrors.containsKey(ProfileField.CURRENT_WEIGHT),
                            supportingText = errorText(ProfileField.CURRENT_WEIGHT)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                    }

                    OutlinedTextField(
                        value = targetWeightStr,
                        onValueChange = { targetWeightStr = it },
                        label = { Text(appString(StringKey.PROFILE_TARGET_WEIGHT)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_target_weight_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = validationErrors.containsKey(ProfileField.TARGET_WEIGHT),
                        supportingText = errorText(ProfileField.TARGET_WEIGHT)?.let { message -> { Text(message) } },
                        shape = androidx.compose.material3.MaterialTheme.shapes.large
                    )

                    Text(appString(StringKey.LIFESTYLE_HELP), style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = stepsStr, onValueChange = { stepsStr = it },
                        label = { Text(appString(StringKey.STEPS_LABEL)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = validationErrors.containsKey(ProfileField.STEPS),
                        supportingText = errorText(ProfileField.STEPS)?.let { message -> { Text(message) } },
                        modifier = Modifier.fillMaxWidth().testTag("average_daily_steps_input")
                    )
                    OutlinedTextField(
                        value = householdStr, onValueChange = { householdStr = it },
                        label = { Text(appString(StringKey.HOUSEHOLD_LABEL)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = validationErrors.containsKey(ProfileField.HOUSEHOLD),
                        supportingText = errorText(ProfileField.HOUSEHOLD)?.let { message -> { Text(message) } },
                        modifier = Modifier.fillMaxWidth().testTag("household_minutes_input")
                    )
                    Text(appString(StringKey.LIFESTYLE_AUTO), style = MaterialTheme.typography.labelSmall)
                    // Activity Level
                    Text(
                        text = appString(StringKey.PROFILE_ACTIVITY_LEVEL),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ActivityLevel.entries.forEach { act ->
                            val isSelected = stepsStr.isBlank() && householdStr.isBlank() && activityLevel == act
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { activityLevel = act; stepsStr = ""; householdStr = "" }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (act) {
                                            ActivityLevel.SEDENTARY -> appString(StringKey.ACTIVITY_SEDENTARY)
                                            ActivityLevel.LIGHT -> appString(StringKey.ACTIVITY_LIGHT)
                                            ActivityLevel.MODERATE -> appString(StringKey.ACTIVITY_MODERATE)
                                            ActivityLevel.ACTIVE -> appString(StringKey.ACTIVITY_ACTIVE)
                                        },
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (act) {
                                            ActivityLevel.SEDENTARY -> appString(StringKey.ACTIVITY_SEDENTARY_DESC)
                                            ActivityLevel.LIGHT -> appString(StringKey.ACTIVITY_LIGHT_DESC)
                                            ActivityLevel.MODERATE -> appString(StringKey.ACTIVITY_MODERATE_DESC)
                                            ActivityLevel.ACTIVE -> appString(StringKey.ACTIVITY_ACTIVE_DESC)
                                        },
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(appString(StringKey.PACE_LIMIT), style = MaterialTheme.typography.labelSmall)
                    // Goal Pace
                    Text(
                        text = appString(StringKey.PROFILE_GOAL_PACE),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GoalPace.entries.forEach { pace ->
                            val isSelected = goalPace == pace
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable { goalPace = pace }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (pace) {
                                        GoalPace.EASY -> appString(StringKey.PACE_EASY)
                                        GoalPace.RECOMMENDED -> appString(StringKey.PACE_RECOMMENDED)
                                        GoalPace.FAST -> appString(StringKey.PACE_FAST)
                                        GoalPace.MAX -> appString(StringKey.PACE_MAX)
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "-${pace.deficitKcal} ${appString(StringKey.UNIT_KCAL)}",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Result of Body Parameters Analysis with Question Mark (?) pop-up
        item {
            val candidate = buildCandidateProfile()
            val analysisValidation = ProfileValidator.validateForCalculations(candidate)

            if (analysisValidation.isValid) {
                val plan = CalorieCalculator.calculateRecommendedTargets(candidate.gender, candidate.age,
                    candidate.heightCm, candidate.currentWeightKg, candidate.activityLevel,
                    candidate.goalPace, candidate.targetWeightKg, candidate.averageDailySteps, candidate.householdMinutes)
                Card(modifier = Modifier.fillMaxWidth().testTag("nutrition_plan_preview")) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${appString(StringKey.PLAN_MAINTENANCE)}: ${plan.tdee} ${appString(StringKey.UNIT_KCAL)}")
                        Text("${appString(StringKey.PLAN_DEFICIT)}: ${plan.appliedDeficit} ${appString(StringKey.UNIT_KCAL)}")
                        Text("${appString(StringKey.PLAN_TARGET)}: ${plan.calories} ${appString(StringKey.UNIT_KCAL)}",
                            fontWeight = FontWeight.Bold)
                        Text("${appString(StringKey.PROFILE_PROTEIN_GOAL)}: ${plan.proteinGrams} · " +
                            "${appString(StringKey.PROFILE_FAT_GOAL)}: ${plan.fatGrams} · " +
                            "${appString(StringKey.PROFILE_CARBS_GOAL)}: ${plan.carbGrams}")
                        Text(appString(StringKey.PLAN_ESTIMATE), style = MaterialTheme.typography.bodySmall)
                        Button(onClick = {
                            val result = viewModel.autoCalculateAndSaveTargets(candidate)
                            validationErrors = result.errors
                            isSavedNotification = result.isValid
                        }, modifier = Modifier.fillMaxWidth().testTag("apply_nutrition_plan_button")) {
                            Text(appString(StringKey.PROFILE_RECALC_BTN))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                BodyAnalysisResultCard(
                    age = candidate.age,
                    heightCm = candidate.heightCm,
                    currentWeightKg = candidate.currentWeightKg,
                    targetWeightKg = candidate.targetWeightKg,
                    gender = candidate.gender,
                    activityLevel = candidate.activityLevel,
                    goalPace = candidate.goalPace,
                    averageDailySteps = candidate.averageDailySteps,
                    householdMinutes = candidate.householdMinutes
                )
            }
        }

        // Daily Targets Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = appString(StringKey.PROFILE_MACROS_TITLE),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = calorieTargetStr,
                        onValueChange = { calorieTargetStr = it },
                        label = { Text(appString(StringKey.PROFILE_CALORIES_GOAL)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_calories_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = validationErrors.containsKey(ProfileField.CALORIES),
                        supportingText = errorText(ProfileField.CALORIES)?.let { message -> { Text(message) } },
                        shape = androidx.compose.material3.MaterialTheme.shapes.large
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = proteinTargetStr,
                            onValueChange = { proteinTargetStr = it },
                            label = { Text(appString(StringKey.PROFILE_PROTEIN_GOAL)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey(ProfileField.PROTEIN),
                            supportingText = errorText(ProfileField.PROTEIN)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = fatTargetStr,
                            onValueChange = { fatTargetStr = it },
                            label = { Text(appString(StringKey.PROFILE_FAT_GOAL)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey(ProfileField.FAT),
                            supportingText = errorText(ProfileField.FAT)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = carbTargetStr,
                            onValueChange = { carbTargetStr = it },
                            label = { Text(appString(StringKey.PROFILE_CARBS_GOAL)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey(ProfileField.CARBS),
                            supportingText = errorText(ProfileField.CARBS)?.let { message -> { Text(message) } },
                            shape = androidx.compose.material3.MaterialTheme.shapes.large
                        )
                    }

                    OutlinedTextField(
                        value = waterTargetStr,
                        onValueChange = { waterTargetStr = it },
                        label = { Text(appString(StringKey.PROFILE_WATER_GOAL)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = validationErrors.containsKey(ProfileField.WATER),
                        supportingText = errorText(ProfileField.WATER)?.let { message -> { Text(message) } },
                        shape = androidx.compose.material3.MaterialTheme.shapes.large
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val candidate = buildCandidateProfile()
                    val inputsChanged = candidate.gender != profile.gender || candidate.age != profile.age ||
                        candidate.heightCm != profile.heightCm || candidate.currentWeightKg != profile.currentWeightKg ||
                        candidate.targetWeightKg != profile.targetWeightKg || candidate.activityLevel != profile.activityLevel ||
                        candidate.goalPace != profile.goalPace || candidate.averageDailySteps != profile.averageDailySteps ||
                        candidate.householdMinutes != profile.householdMinutes
                    val manualTargetsChanged = candidate.dailyCalorieTarget != profile.dailyCalorieTarget ||
                        candidate.proteinTargetGrams != profile.proteinTargetGrams ||
                        candidate.fatTargetGrams != profile.fatTargetGrams || candidate.carbTargetGrams != profile.carbTargetGrams
                    val result = if (inputsChanged && !manualTargetsChanged)
                        viewModel.autoCalculateAndSaveTargets(candidate) else viewModel.updateUserProfile(candidate)
                    validationErrors = result.errors
                    isSavedNotification = result.isValid
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(com.example.ui.theme.SlimTrackSizes.buttonLarge)
                    .testTag("save_profile_settings_button"),
                shape = androidx.compose.material3.MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSavedNotification) appString(StringKey.PROFILE_SAVED_SUCCESS) else appString(StringKey.PROFILE_SAVE_SETTINGS),
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Export Report Card (Locked for Free/Slim, Unlocked for Premium/Admin)
        item {
            val isExportUnlocked = viewModel.isFeatureUnlocked(AppFeature.EXPORT_REPORTS)
            if (isExportUnlocked) {
                ExportReportCard(
                    userProfile = profile,
                    weightLogs = weightLogs,
                    currentMeals = currentMeals
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openPaywall(AppFeature.EXPORT_REPORTS) }
                        .testTag("locked_export_report_card"),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FatAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appString(StringKey.INSIGHTS_EXPORT_REPORT),
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appString(StringKey.PREMIUM_REQUIRED),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.openPaywall(AppFeature.EXPORT_REPORTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = FatAmber),
                            shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                            modifier = Modifier.height(com.example.ui.theme.SlimTrackSizes.buttonCompact)
                        ) {
                            Text(appString(StringKey.UPGRADE_PLAN), color = Color.Black, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
