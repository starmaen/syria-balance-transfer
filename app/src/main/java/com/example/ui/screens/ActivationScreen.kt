package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.SyriatelRed
import com.example.ui.viewmodel.TransferViewModel

@Composable
fun ActivationScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val securityState by viewModel.securityState.collectAsStateWithLifecycle()

    var customerNameInput by remember { mutableStateOf("") }
    var activationKeyInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showInvestorQuickDialog by remember { mutableStateOf(false) }
    var investorPinInput by remember { mutableStateOf("") }
    var investorPinError by remember { mutableStateOf(false) }

    var logoTapCount by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Shield / Logo Badge (Tapping 5 times opens Secret Master/Investor Unlock)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SyriatelRed, Color(0xFFE65100))
                            )
                        )
                        .border(3.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                        .clickable {
                            logoTapCount++
                            if (logoTapCount >= 5) {
                                logoTapCount = 0
                                showInvestorQuickDialog = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Title & Description
                Text(
                    text = "محول الرصيد السوري",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "نظام الحماية والترخيص المرتبط بالجهاز",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SyriatelRed,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "هذا التطبيق مخصص للاستخدام على جهاز واحد محدد. لتفعيل نسختك ومنع العبث بالرصيد، يرجى تزويد مالك البرنامج بمعرف جهازك أدناه للحصول على كود التفعيل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Device Fingerprint Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_device_id"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(SyriatelRed.copy(alpha = 0.5f), Color(0xFFFFB300).copy(alpha = 0.5f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "معرّف هذا الجهاز (خاص بنسختك):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Monospace Device ID Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = securityState.deviceId,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 2.sp
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(securityState.deviceId))
                                            Toast.makeText(context, "تم نسخ معرّف الجهاز بنجاح", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val shareText = "طلب كود تفعيل محول الرصيد السوري:\nمعرّف جهازي: ${securityState.deviceId}\nاسم العميل: $customerNameInput"
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة معرّف الجهاز مع المستثمر"))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "مشاركة",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "اضغط على زر النسخ أو المشاركة لإرسال المعرّف لمالك التطبيق",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Activation Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "إدخال بيانات التفعيل الرسمية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Customer/Shop Name Field
                        OutlinedTextField(
                            value = customerNameInput,
                            onValueChange = {
                                customerNameInput = it
                                errorMessage = null
                            },
                            label = { Text("اسم العميل أو اسم المحل التجاري") },
                            placeholder = { Text("مثال: مركز الأمل لخدمات الرصيد") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_customer_name"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Activation Key Field
                        OutlinedTextField(
                            value = activationKeyInput,
                            onValueChange = {
                                activationKeyInput = it
                                errorMessage = null
                            },
                            label = { Text("كود التفعيل (من مالك البرنامج)") },
                            placeholder = { Text("مثال: ACT-XXXX-YYYY") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_activation_key"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SyriatelRed,
                                focusedLabelColor = SyriatelRed
                            )
                        )

                        // Error Banner
                        AnimatedVisibility(visible = errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Activate Button
                        Button(
                            onClick = {
                                if (customerNameInput.isBlank()) {
                                    errorMessage = "يرجى كتابة اسم العميل أو اسم المتجر أولاً"
                                    return@Button
                                }
                                if (activationKeyInput.isBlank()) {
                                    errorMessage = "يرجى كتابة كود التفعيل المستلم من المالك"
                                    return@Button
                                }
                                val success = viewModel.activateLicense(customerNameInput, activationKeyInput)
                                if (success) {
                                    Toast.makeText(context, "تم تفعيل النسخة بنجاح! أهلاً بك.", Toast.LENGTH_LONG).show()
                                } else {
                                    errorMessage = "كود التفعيل غير صحيح أو غير مخصص لهذا الجهاز وهذا الاسم."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_activate_license"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SyriatelRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تفعيل التطبيق والبدء بالعمل",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Investor Quick Unlock Dialog
            if (showInvestorQuickDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showInvestorQuickDialog = false
                        investorPinInput = ""
                        investorPinError = false
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SyriatelRed,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "تسجيل دخول المستثمر / المالك",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "أدخل الرقم السري الخاص بالمستثمر لتفعيل هذه النسخة مباشرة لهذا الجهاز:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = investorPinInput,
                                onValueChange = {
                                    investorPinInput = it
                                    investorPinError = false
                                },
                                label = { Text("الرقم السري للمستثمر") },
                                placeholder = { Text("الافتراضي: 123456") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = investorPinError,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (investorPinError) {
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
                                val isValid = viewModel.unlockAppWithPin(investorPinInput)
                                if (isValid) {
                                    val name = customerNameInput.ifBlank { "عميل معتمد (${securityState.deviceId})" }
                                    val generatedKey = viewModel.generateClientLicenseKey(securityState.deviceId, name)
                                    viewModel.activateLicense(name, generatedKey)
                                    showInvestorQuickDialog = false
                                    Toast.makeText(context, "تم تفعيل التطبيق بنجاح بواسطة المستثمر", Toast.LENGTH_SHORT).show()
                                } else {
                                    investorPinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SyriatelRed)
                        ) {
                            Text("تفعيل فوري")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showInvestorQuickDialog = false
                            investorPinInput = ""
                            investorPinError = false
                        }) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }
    }
}
