package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.OperatorSettings
import com.example.ui.theme.SyriatelRed
import com.example.ui.viewmodel.TransferViewModel

@Composable
fun SettingsScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()
    val securityState by viewModel.securityState.collectAsStateWithLifecycle()

    var syriatelPin by remember(currentSettings) { mutableStateOf(currentSettings.syriatelPin) }
    var syriatelUssd by remember(currentSettings) { mutableStateOf(currentSettings.syriatelUssdTemplate) }

    var mtnPin by remember(currentSettings) { mutableStateOf(currentSettings.mtnPin) }
    var mtnUssd by remember(currentSettings) { mutableStateOf(currentSettings.mtnUssdTemplate) }

    var customEnabled by remember(currentSettings) { mutableStateOf(currentSettings.customOperatorEnabled) }
    var customName by remember(currentSettings) { mutableStateOf(currentSettings.customOperatorName) }
    var customPrefix by remember(currentSettings) { mutableStateOf(currentSettings.customOperatorPrefix) }
    var customPin by remember(currentSettings) { mutableStateOf(currentSettings.customOperatorPin) }
    var customUssd by remember(currentSettings) { mutableStateOf(currentSettings.customOperatorUssdTemplate) }

    var shopName by remember(currentSettings) { mutableStateOf(currentSettings.shopName) }
    var shopPhone by remember(currentSettings) { mutableStateOf(currentSettings.shopPhone) }
    var hapticFeedback by remember(currentSettings) { mutableStateOf(currentSettings.hapticFeedbackEnabled) }
    var sendSmsPrompt by remember(currentSettings) { mutableStateOf(currentSettings.sendSmsReceiptPrompt) }
    var quickAmounts by remember(currentSettings) { mutableStateOf(currentSettings.quickAmounts) }
    var newQuickAmountInput by remember { mutableStateOf("") }

    // Security & Licensing states
    var showChangeCredentialsDialog by remember { mutableStateOf(false) }
    var newUsernameInput by remember { mutableStateOf(securityState.investorUsername) }
    var newPinInput by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    var credentialError by remember { mutableStateOf<String?>(null) }

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var deactivatePinInput by remember { mutableStateOf("") }
    var deactivateError by remember { mutableStateOf(false) }

    // Investor License Generator states
    var clientDeviceIdInput by remember { mutableStateOf("") }
    var clientNameInput by remember { mutableStateOf("") }
    var generatedClientKey by remember { mutableStateOf<String?>(null) }
    var isInvestorToolsUnlocked by remember { mutableStateOf(false) }
    var showUnlockInvestorToolsDialog by remember { mutableStateOf(false) }
    var developerMasterPinInput by remember { mutableStateOf("") }
    var developerMasterPinError by remember { mutableStateOf(false) }
    var licenseCardTapCount by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card with Explanatory info about USSD templates
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "إعدادات أكواد التحويل للشركات السورية",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "يمكنك تعديل صيغ الأكواد والرمز السري. المتغيرات المتاحة للاستبدال الآلي: {phone} لرقم الهاتف، {amount} للمبلغ، {pin} للرمز السري.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: Syriatel Settings
        OperatorSettingCard(
            title = "شركة سيريتل (Syriatel)",
            brandColor = Color(0xFFD32F2F),
            icon = Icons.Default.SimCard
        ) {
            OutlinedTextField(
                value = syriatelPin,
                onValueChange = { syriatelPin = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text("رمز التحويل السري (PIN)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("syriatel_pin_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = syriatelUssd,
                onValueChange = { syriatelUssd = it },
                label = { Text("صيغة كود USSD") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("syriatel_ussd_input"),
                singleLine = true
            )

            // Live preview
            val syriatelSample = syriatelUssd
                .replace("{phone}", "0933123456")
                .replace("{amount}", "5000")
                .replace("{pin}", syriatelPin)
            LiveCodePreview(sampleCode = syriatelSample)
        }

        // Section 2: MTN Settings
        OperatorSettingCard(
            title = "شركة إم تي إن (MTN)",
            brandColor = Color(0xFFFFB800),
            icon = Icons.Default.CellTower
        ) {
            OutlinedTextField(
                value = mtnPin,
                onValueChange = { mtnPin = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text("رمز التحويل السري (PIN)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mtn_pin_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = mtnUssd,
                onValueChange = { mtnUssd = it },
                label = { Text("صيغة كود USSD") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mtn_ussd_input"),
                singleLine = true
            )

            val mtnSample = mtnUssd
                .replace("{phone}", "0944123456")
                .replace("{amount}", "5000")
                .replace("{pin}", mtnPin)
            LiveCodePreview(sampleCode = mtnSample)
        }

        // Section 3: 3rd / New Operator Placeholder (Wafa Telecom / Custom)
        OperatorSettingCard(
            title = "المشغل الثالث الجديد (مكان فارغ لشركة جديدة)",
            brandColor = Color(0xFF00897B),
            icon = Icons.Default.SignalCellularAlt
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفعيل المشغل الجديد في الواجهة:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = customEnabled,
                    onCheckedChange = { customEnabled = it }
                )
            }

            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                label = { Text("اسم الشركة / المشغل (مثلاً: وفا تل Wafa)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_operator_name_input"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customPrefix,
                    onValueChange = { if (it.length <= 4) customPrefix = it },
                    label = { Text("بادئة الأرقام (Prefix)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customPin,
                    onValueChange = { customPin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    label = { Text("رمز التحويل (PIN)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = customUssd,
                onValueChange = { customUssd = it },
                label = { Text("صيغة كود USSD للمشغل الجديد") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            val customSample = customUssd
                .replace("{phone}", "${customPrefix}123456")
                .replace("{amount}", "5000")
                .replace("{pin}", customPin)
            LiveCodePreview(sampleCode = customSample)
        }

        // Section 4: General Shop Settings
        OperatorSettingCard(
            title = "بيانات المحل وتخصيص التطبيق",
            brandColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Storefront
        ) {
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("اسم المحل / المركز التجاري") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = shopPhone,
                onValueChange = { shopPhone = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                label = { Text("هاتف / واتساب المحل للوصولات") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "اهتزاز لمسي عند الضغط على المفاتيح")
                }
                Switch(checked = hapticFeedback, onCheckedChange = { hapticFeedback = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "اقتراح إرسال وصل SMS للزبون بعد التحويل")
                Switch(checked = sendSmsPrompt, onCheckedChange = { sendSmsPrompt = it })
            }
        }

        // Section 4.5: Customizable quick amounts (add/remove without editing code)
        OperatorSettingCard(
            title = "المبالغ السريعة في لوحة المفاتيح",
            brandColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Storefront
        ) {
            Text(
                text = "أضف أو احذف أي مبلغ تريده أن يظهر كزر سريع أثناء التحويل، دون الحاجة لتعديل الكود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { amount ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "%,d".format(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { quickAmounts = quickAmounts.filter { it != amount } },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newQuickAmountInput,
                    onValueChange = { input -> if (input.all { it.isDigit() }) newQuickAmountInput = input },
                    label = { Text("مبلغ جديد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = {
                    val value = newQuickAmountInput.toLongOrNull()
                    if (value != null && value > 0 && value !in quickAmounts) {
                        quickAmounts = (quickAmounts + value).sorted()
                    }
                    newQuickAmountInput = ""
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }

        // Save and Reset Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val updated = OperatorSettings(
                        syriatelPin = syriatelPin,
                        syriatelUssdTemplate = syriatelUssd,
                        mtnPin = mtnPin,
                        mtnUssdTemplate = mtnUssd,
                        customOperatorEnabled = customEnabled,
                        customOperatorName = customName,
                        customOperatorPrefix = customPrefix,
                        customOperatorPin = customPin,
                        customOperatorUssdTemplate = customUssd,
                        shopName = shopName,
                        shopPhone = shopPhone,
                        hapticFeedbackEnabled = hapticFeedback,
                        sendSmsReceiptPrompt = sendSmsPrompt,
                        quickAmounts = quickAmounts
                    )
                    viewModel.updateSettings(updated)
                    Toast.makeText(context, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ كل الإعدادات", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val defaults = OperatorSettings()
                    syriatelPin = defaults.syriatelPin
                    syriatelUssd = defaults.syriatelUssdTemplate
                    mtnPin = defaults.mtnPin
                    mtnUssd = defaults.mtnUssdTemplate
                    customEnabled = defaults.customOperatorEnabled
                    customName = defaults.customOperatorName
                    customPrefix = defaults.customOperatorPrefix
                    customPin = defaults.customOperatorPin
                    customUssd = defaults.customOperatorUssdTemplate
                    shopName = defaults.shopName
                    shopPhone = defaults.shopPhone
                    hapticFeedback = defaults.hapticFeedbackEnabled
                    sendSmsPrompt = defaults.sendSmsReceiptPrompt
                    quickAmounts = defaults.quickAmounts
                    viewModel.updateSettings(defaults)
                    Toast.makeText(context, "تمت استعادة الإعدادات الافتراضية", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("استعادة", fontSize = 12.sp)
            }
        }

        // Section 5: Security, Device License & Anti-Tampering
        OperatorSettingCard(
            title = "الأمان والترخيص وحماية الرصيد",
            brandColor = SyriatelRed,
            icon = Icons.Default.Security
        ) {
            // License Details (Tapping 5 times allows unlocking investor tools)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    licenseCardTapCount++
                    if (licenseCardTapCount >= 5) {
                        licenseCardTapCount = 0
                        showUnlockInvestorToolsDialog = true
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "حالة الترخيص: مفعل ومرتبط بهذا الجهاز",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Text(
                        text = "الزبون / المتجر المرخص: ${securityState.licensedCustomerName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "معرّف الجهاز: ${securityState.deviceId}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(securityState.deviceId))
                                Toast.makeText(context, "تم نسخ معرّف الجهاز", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ المعرف",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Auto-lock Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "القفل التلقائي عند فتح التطبيق",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "يطلب رقم سري المستثمر عند كل فتح للتطبيق لمنع التلاعب",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = securityState.autoLockEnabled,
                    onCheckedChange = { viewModel.setAutoLock(it) }
                )
            }

            // Quick Lock & Change Credentials Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.lockApp() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SyriatelRed)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("قفل التطبيق الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        newUsernameInput = securityState.investorUsername
                        newPinInput = ""
                        currentPinInput = ""
                        credentialError = null
                        showChangeCredentialsDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تغيير كلمة المرور", fontSize = 12.sp)
                }
            }

            // Deactivate device
            TextButton(
                onClick = {
                    deactivatePinInput = ""
                    deactivateError = false
                    showDeactivateDialog = true
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "إلغاء تفعيل هذا الجهاز (للمستثمر فقط)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Discreet button to unlock investor tools
            if (!isInvestorToolsUnlocked) {
                TextButton(
                    onClick = { showUnlockInvestorToolsDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("أدوات المستثمر والمطور (مقفلة)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        isInvestorToolsUnlocked = false
                        Toast.makeText(context, "تم إخفاء وقفل أدوات المستثمر بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("قفل وإخفاء أدوات المستثمر والمولد", fontSize = 12.sp)
                }
            }
        }

        // Section 6: Investor Tool: License Key Generator for Other Clients (ONLY VISIBLE IF UNLOCKED BY INVESTOR)
        if (isInvestorToolsUnlocked) {
            OperatorSettingCard(
                title = "أداة المستثمر: مولّد مفاتيح الترخيص للزبائن (مفعل)",
                brandColor = Color(0xFF1976D2),
                icon = Icons.Default.AdminPanelSettings
            ) {
                Text(
                    text = "بصفتك المالك والمستثمر للبرنامج، يمكنك توليد أكواد تفعيل جديدة لزبائنك لأجهزتهم الخاصة لمنع تشغيل التطبيق على أجهزة غير مرخصة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = clientDeviceIdInput,
                    onValueChange = { clientDeviceIdInput = it },
                    label = { Text("معرّف جهاز الزبون الآخر") },
                    placeholder = { Text("مثال: SYR-7A9B-4C21") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clientNameInput,
                    onValueChange = { clientNameInput = it },
                    label = { Text("اسم الزبون أو المحل المرخص له") },
                    placeholder = { Text("مثال: تسجيلات السلام - دمشق") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (clientDeviceIdInput.isBlank() || clientNameInput.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة معرّف الجهاز واسم الزبون", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val generated = viewModel.generateClientLicenseKey(clientDeviceIdInput, clientNameInput)
                        generatedClientKey = generated
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("توليد كود التفعيل المرتبط بهذا الجهاز", fontWeight = FontWeight.Bold)
                }

                if (generatedClientKey != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "كود التفعيل المرخص (خاص بهذا الجهاز فقط):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = generatedClientKey ?: "",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = 2.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(generatedClientKey ?: ""))
                                        Toast.makeText(context, "تم نسخ كود التفعيل", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ الكود", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareMsg = "أهلاً بك، كود تفعيل تطبيق محول الرصيد السوري المخصص لجهازك ($clientDeviceIdInput) باسم ($clientNameInput):\n${generatedClientKey}\nيرجى إدخاله في شاشة تفعيل التطبيق."
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareMsg)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "إرسال كود التفعيل للزبون"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إرسال للزبون", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Dialog: Change Investor Credentials
    if (showChangeCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showChangeCredentialsDialog = false },
            title = {
                Text(
                    text = "تغيير بيانات دخول المستثمر",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يمكنك تعديل اسم المستثمر والرقم السري المستخدم لقفل التطبيق:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = newUsernameInput,
                        onValueChange = { newUsernameInput = it },
                        label = { Text("اسم المستثمر الجديد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = { currentPinInput = it },
                        label = { Text("الرقم السري الحالي للتأكيد") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { newPinInput = it },
                        label = { Text("الرقم السري الجديد (4-6 أرقام)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (credentialError != null) {
                        Text(
                            text = credentialError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.updateInvestorCredentials(newUsernameInput, newPinInput, currentPinInput)
                        if (success) {
                            showChangeCredentialsDialog = false
                            Toast.makeText(context, "تم تحديث بيانات المستثمر بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            credentialError = "الرقم السري الحالي غير صحيح أو الرقم الجديد أقل من 4 أرقام"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SyriatelRed)
                ) {
                    Text("حفظ التغيير")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeCredentialsDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog: Deactivate License
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = {
                Text("تأكيد إلغاء تفعيل هذا الجهاز", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تحذير: سيتم قفل التطبيق وإعادة هذا الجهاز إلى حالة غير مفعلة، وسيتطلب كود تفعيل جديد من المستثمر للعمل مجدداً.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = deactivatePinInput,
                        onValueChange = {
                            deactivatePinInput = it
                            deactivateError = false
                        },
                        label = { Text("أدخل الرقم السري للمستثمر للتأكيد") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = deactivateError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (deactivateError) {
                        Text(
                            text = "الرقم السري غير صحيح!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.deactivateLicense(deactivatePinInput)
                        if (success) {
                            showDeactivateDialog = false
                            Toast.makeText(context, "تم إلغاء تفعيل الجهاز بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            deactivateError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد الإلغاء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }

    // Dialog: Unlock Investor Developer Tools
    if (showUnlockInvestorToolsDialog) {
        AlertDialog(
            onDismissRequest = {
                showUnlockInvestorToolsDialog = false
                developerMasterPinInput = ""
                developerMasterPinError = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFF1976D2)
                )
            },
            title = {
                Text("فتح أدوات المستثمر والمطور", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل الرقم السري الخاص بالمستثمر لإظهار مولّد مفاتيح الترخيص للزبائن:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = developerMasterPinInput,
                        onValueChange = {
                            developerMasterPinInput = it
                            developerMasterPinError = false
                        },
                        label = { Text("الرقم السري للمستثمر") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = developerMasterPinError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (developerMasterPinError) {
                        Text(
                            text = "الرقم السري غير صحيح!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyInvestorPin(developerMasterPinInput)) {
                            isInvestorToolsUnlocked = true
                            showUnlockInvestorToolsDialog = false
                            developerMasterPinInput = ""
                            Toast.makeText(context, "تم فتح أدوات المستثمر ومولّد المفاتيح بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            developerMasterPinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("فتح الأدوات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockInvestorToolsDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun OperatorSettingCard(
    title: String,
    brandColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(brandColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = brandColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            content()
        }
    }
}

@Composable
private fun LiveCodePreview(sampleCode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(10.dp)
    ) {
        Text(
            text = "معاينة الكود الناتج (تجريبي):",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sampleCode,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
