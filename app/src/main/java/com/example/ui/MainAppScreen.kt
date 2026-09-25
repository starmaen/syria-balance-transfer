package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import com.example.ui.components.OwnerPortalDialog
import com.example.ui.screens.ActivationScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.SyriatelRed
import com.example.ui.viewmodel.TransferViewModel

@Composable
fun MainAppScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val securityState by viewModel.securityState.collectAsStateWithLifecycle()
    var showOwnerPortalDialog by remember { mutableStateOf(false) }

    // 1. Device activation gate
    if (!securityState.isActivated) {
        ActivationScreen(
            viewModel = viewModel,
            modifier = modifier
        )
        return
    }

    // 2. Investor / Operator lock gate
    if (securityState.isAppLocked) {
        LoginScreen(
            viewModel = viewModel,
            modifier = modifier
        )
        return
    }

    // 3. Main Application Dashboard (Authenticated & Activated)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand and App Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SyriatelRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "محول الرصيد السوري",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "مرخص: ${securityState.licensedCustomerName.ifBlank { "نسخة معتمدة" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.lockApp() },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("btn_quick_lock")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "قفل التطبيق الآن لمنع التلاعب",
                                            tint = SyriatelRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "قفل",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp
                ) {
                    // Tab 0: Transfer / Terminal
                    NavigationBarItem(
                        selected = uiState.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 0) Icons.Default.PhoneAndroid else Icons.Outlined.PhoneAndroid,
                                contentDescription = "تحويل الرصيد",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "تحويل الرصيد",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.activeTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_transfer")
                    )

                    // Tab 1: History
                    NavigationBarItem(
                        selected = uiState.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 1) Icons.Default.History else Icons.Outlined.History,
                                contentDescription = "سجل العمليات",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "سجل التحويل",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.activeTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_history")
                    )

                    // Tab 2: Debts & Credits
                    NavigationBarItem(
                        selected = uiState.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 2) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                contentDescription = "دفتر الديون",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "دفتر الديون",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.activeTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_debts")
                    )

                    // Tab 3: Settings
                    NavigationBarItem(
                        selected = uiState.activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "الإعدادات",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "الإعدادات",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.activeTab == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        ) { paddingValues ->
            when (uiState.activeTab) {
                0 -> TransferScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
                1 -> HistoryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
                2 -> DebtsScreen(
                    viewModel = viewModel,
                    onTransferToCustomer = { phone, name ->
                        viewModel.onKeypadClear()
                        // Set phone and name in viewModel
                        phone.forEach { ch ->
                            if (ch.isDigit()) viewModel.onKeypadDigit(ch.toString())
                        }
                        viewModel.setActiveTab(0)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
                3 -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Owner Portal Dialog
        if (showOwnerPortalDialog) {
            OwnerPortalDialog(
                viewModel = viewModel,
                onDismiss = { showOwnerPortalDialog = false }
            )
        }
    }
}
