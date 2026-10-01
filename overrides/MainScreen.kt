package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SubscriptionTier
import com.example.ui.components.ActivatePromoCodeDialog
import com.example.ui.components.AdminPanelDialog
import com.example.ui.components.PaymentCheckoutDialog
import com.example.ui.components.PaymentReceiptDialog
import com.example.ui.components.SubscriptionPaywallDialog
import com.example.ui.screens.DiaryScreen
import com.example.ui.screens.FoodDatabaseScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WeightScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.ui.viewmodel.FitnessViewModel
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.util.StringKey
import com.example.util.appString

enum class AppTab(
    val stringKey: StringKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DIARY(StringKey.TAB_DIARY, Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "tab_diary"),
    WEIGHT(StringKey.TAB_WEIGHT, Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight, "tab_weight"),
    FOODS(StringKey.TAB_FOODS, Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "tab_foods"),
    INSIGHTS(StringKey.TAB_INSIGHTS, Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, "tab_insights"),
    PROFILE(StringKey.TAB_PROFILE, Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppTab.DIARY) }
    val adminToolsEnabled by viewModel.adminAccess.collectAsStateWithLifecycle()
    val currentTier by viewModel.userTier.collectAsStateWithLifecycle()
    val appConfig by viewModel.currentConfig.collectAsStateWithLifecycle()
    val activeGuestAccess by viewModel.activeGuestAccess.collectAsStateWithLifecycle()
    val savedInvites by viewModel.savedInvites.collectAsStateWithLifecycle()
    val paymentDetails by viewModel.paymentDetails.collectAsStateWithLifecycle()
    val latestReceipt by viewModel.latestReceipt.collectAsStateWithLifecycle()
    val currentLang = com.example.util.appLanguage()

    var isLangMenuExpanded by remember { mutableStateOf(false) }

    // BackHandler: if not on DIARY tab, back button returns to DIARY tab
    if (currentTab != AppTab.DIARY) {
        BackHandler {
            currentTab = AppTab.DIARY
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Logo & Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { currentTab = AppTab.DIARY }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(androidx.compose.material3.MaterialTheme.shapes.small)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SlimTrack",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Action Controls: Language dropdown, Subscription badge, Admin shield
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Language Selector Dropdown
                            Box {
                                Surface(
                                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { isLangMenuExpanded = true }
                                        .testTag("language_selector_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentLang.flag,
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = currentLang.code.uppercase(),
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isLangMenuExpanded,
                                    onDismissRequest = { isLangMenuExpanded = false }
                                ) {
                                    AppLanguage.entries.forEach { lang ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = lang.flag, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = lang.displayName,
                                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (lang == currentLang) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (lang == currentLang) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = EmeraldPrimary,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                LocalizationManager.setLanguage(lang)
                                                isLangMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Subscription Badge (clickable -> opens Paywall)
                            Surface(
                                shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                                color = when {
                                    activeGuestAccess?.isLifetime == true -> FatAmber.copy(alpha = 0.25f)
                                    activeGuestAccess != null -> EmeraldPrimary.copy(alpha = 0.2f)
                                    currentTier == SubscriptionTier.FREE -> FatAmber.copy(alpha = 0.18f)
                                    currentTier == SubscriptionTier.SLIM -> EmeraldPrimary.copy(alpha = 0.18f)
                                    currentTier == SubscriptionTier.PREMIUM -> FatAmber.copy(alpha = 0.25f)
                                    else -> ProteinBlue.copy(alpha = 0.2f)
                                },
                                modifier = Modifier
                                    .clickable { viewModel.openPaywall() }
                                    .testTag("subscription_badge_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = when {
                                            activeGuestAccess?.isLifetime == true -> FatAmber
                                            activeGuestAccess != null -> EmeraldPrimary
                                            currentTier == SubscriptionTier.FREE -> FatAmber
                                            currentTier == SubscriptionTier.SLIM -> EmeraldPrimary
                                            currentTier == SubscriptionTier.PREMIUM -> FatAmber
                                            else -> ProteinBlue
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = when {
                                            activeGuestAccess?.isLifetime == true -> "VIP 👑"
                                            activeGuestAccess != null -> "${activeGuestAccess?.tier?.defaultName} 🎁"
                                            currentTier == SubscriptionTier.FREE -> appString(StringKey.HEADER_PLANS_BTN)
                                            currentTier == SubscriptionTier.SLIM -> "Slim PRO"
                                            currentTier == SubscriptionTier.PREMIUM -> "VIP $50"
                                            currentTier == SubscriptionTier.ADMIN -> "Admin"
                                            else -> appString(StringKey.HEADER_PLANS_BTN)
                                        },
                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = when {
                                            activeGuestAccess?.isLifetime == true -> FatAmber
                                            activeGuestAccess != null -> EmeraldPrimary
                                            currentTier == SubscriptionTier.FREE -> FatAmber
                                            currentTier == SubscriptionTier.SLIM -> EmeraldPrimary
                                            currentTier == SubscriptionTier.PREMIUM -> FatAmber
                                            else -> ProteinBlue
                                        }
                                    )
                                }
                            }

                            // Admin tools are available only after verified account authorization.
                            if (adminToolsEnabled) {
                                IconButton(
                                    onClick = { viewModel.openAdminPanel() },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .testTag("admin_panel_open_icon_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = appString(StringKey.ADMIN_PANEL_TITLE),
                                        modifier = Modifier.size(16.dp),
                                        tint = if (currentTier == SubscriptionTier.ADMIN) ProteinBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar")
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val labelText = appString(tab.stringKey)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = labelText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = labelText,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.DIARY -> DiaryScreen(viewModel = viewModel)
                    AppTab.WEIGHT -> WeightScreen(viewModel = viewModel)
                    AppTab.FOODS -> FoodDatabaseScreen(viewModel = viewModel)
                    AppTab.INSIGHTS -> InsightsScreen(viewModel = viewModel)
                    AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Subscription Paywall Dialog
    if (viewModel.showPaywallDialog) {
        SubscriptionPaywallDialog(
            currentTier = currentTier,
            appConfig = appConfig,
            highlightFeature = viewModel.paywallTriggerFeature,
            onSelectTier = { newTier ->
                viewModel.selectTierFromPaywall(newTier)
            },
            onProceedToPayment = { tier ->
                viewModel.openPaymentCheckout(tier)
            },
            promoActivationEnabled = viewModel.promoActivationEnabled,
            onOpenPromoDialog = {
                viewModel.openActivatePromoDialog()
            },
            onDismiss = { viewModel.closePaywall() }
        )
    }

    // Payment Checkout Dialog
    if (viewModel.showPaymentCheckoutDialog) {
        val tier = viewModel.checkoutTier
        val price = appConfig.tiers[tier]?.price ?: if (tier == SubscriptionTier.PREMIUM) "$50" else "$20"
        PaymentCheckoutDialog(
            tier = tier,
            tierPrice = price,
            paymentDetails = paymentDetails,
            onConfirmPayment = { method, note ->
                viewModel.closePaymentCheckout()
                viewModel.submitPayment(
                    tier = tier,
                    amount = price,
                    methodType = method,
                    payerNote = note
                )
            },
            onDismiss = { viewModel.closePaymentCheckout() }
        )
    }

    // Payment Receipt Dialog
    if (viewModel.showPaymentReceiptDialog && viewModel.activeReceiptForDisplay != null) {
        PaymentReceiptDialog(
            receipt = viewModel.activeReceiptForDisplay!!,
            supportContact = "${paymentDetails.supportTelegram} (${paymentDetails.supportEmail})",
            onDismiss = { viewModel.closePaymentReceipt() }
        )
    }

    // Admin Panel Dialog
    if (adminToolsEnabled && viewModel.showAdminPanelDialog) {
        AdminPanelDialog(
            currentTier = currentTier,
            appConfig = appConfig,
            savedInvites = savedInvites,
            paymentDetails = paymentDetails,
            onSelectTier = { newTier ->
                viewModel.setUserTierForDebug(newTier)
            },
            onImportConfig = { jsonText, fileName ->
                viewModel.importAdminConfigFile(jsonText, fileName)
            },
            onResetConfig = {
                viewModel.resetConfigToDefaults()
            },
            onCreateInvite = { tier, days, label, customCode ->
                viewModel.createGuestInvite(tier, days, label, customCode)
            },
            onDeleteInvite = { code ->
                viewModel.deleteGuestInvite(code)
            },
            onUpdatePaymentDetails = {
                viewModel.updatePaymentDetails(it)
            },
            onDismiss = { viewModel.closeAdminPanel() }
        )
    }

    // Activate Promo Code Dialog
    if (viewModel.promoActivationEnabled && viewModel.showActivatePromoDialog) {
        ActivatePromoCodeDialog(
            onActivate = { input ->
                viewModel.activatePromoCode(input)
            },
            onDismiss = { viewModel.closeActivatePromoDialog() }
        )
    }
}
