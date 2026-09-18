package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.components.LcdNumberDisplay
import com.example.ui.components.OperatorSelector
import com.example.ui.components.TactileKeypad
import com.example.ui.components.TransferSuccessDialog
import com.example.ui.viewmodel.TransferViewModel

@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val totalOutgoing by viewModel.totalOutgoing.collectAsStateWithLifecycle()

    var showCustomAmountDialog by remember { mutableStateOf(false) }
    var tempAmountText by remember { mutableStateOf("") }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        viewModel.onContactPicked(context, contactUri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shop Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = settings.shopName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحويل رصيد سوري فوري",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Total outgoing today badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صادر: ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%,d ل.س".format(totalOutgoing),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Operator Selector (Syriatel / MTN / Custom 3rd Operator)
            OperatorSelector(
                selectedOperator = uiState.selectedOperator,
                customOperatorName = settings.customOperatorName,
                onSelectOperator = { viewModel.selectOperator(it) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // The LCD Monitor Display (Screen for number verification & 1-click execution)
            LcdNumberDisplay(
                phoneNumber = uiState.phoneNumber,
                customerName = uiState.customerName,
                amount = uiState.amountText.toLongOrNull() ?: 0L,
                operator = uiState.selectedOperator,
                customOperatorName = settings.customOperatorName,
                onOneClickTransfer = { viewModel.executeTransfer(asDebt = false) },
                onTransferAsDebt = { viewModel.executeTransfer(asDebt = true) },
                onClearCustomerName = { viewModel.onKeypadClear() },
                onChangeAmountClick = {
                    tempAmountText = uiState.amountText
                    showCustomAmountDialog = true
                }
            )
        }

        // Tactile Keypad Section
        TactileKeypad(
            currentAmount = uiState.amountText.toLongOrNull() ?: 0L,
            onDigitClick = { viewModel.onKeypadDigit(it) },
            onBackspaceClick = { viewModel.onKeypadBackspace() },
            onClearClick = { viewModel.onKeypadClear() },
            onQuickPrefixClick = { viewModel.setQuickPrefix(it) },
            onSelectAmount = { viewModel.setAmount(it) },
            onCustomAmountClick = {
                tempAmountText = uiState.amountText
                showCustomAmountDialog = true
            },
            onOpenContactsClick = {
                contactPickerLauncher.launch(null)
            },
            modifier = Modifier.padding(bottom = 6.dp),
            quickAmounts = settings.quickAmounts
        )
    }

    // Custom Amount Input Dialog
    if (showCustomAmountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAmountDialog = false },
            title = {
                Text(
                    text = "تحديد مبلغ التحويل (ل.س)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل قيمة الرصيد المطلوب تحويله بالليرة السورية:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 8) {
                                tempAmountText = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("المبلغ (ل.س)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_amount_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAmountText.isNotBlank()) {
                            viewModel.setCustomAmount(tempAmountText)
                        }
                        showCustomAmountDialog = false
                    },
                    modifier = Modifier.testTag("confirm_custom_amount_button")
                ) {
                    Text("تثبيت المبلغ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAmountDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Success Notification & Invoice Dialog
    if (uiState.showSuccessDialog && uiState.lastSuccessTransfer != null) {
        TransferSuccessDialog(
            record = uiState.lastSuccessTransfer!!,
            shopName = settings.shopName,
            onDismiss = { viewModel.dismissSuccessDialog() }
        )
    }
}
