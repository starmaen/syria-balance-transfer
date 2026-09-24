package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.SyriatelRed
import com.example.ui.viewmodel.TransferViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerPortalDialog(
    viewModel: TransferViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val securityState by viewModel.securityState.collectAsStateWithLifecycle()

    var isOwnerAuthenticated by remember {
        mutableStateOf(securityState.isOwnerDevice || securityState.licensedCustomerName.contains("مالك"))
    }
    var ownerPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Generator inputs
    var customerDeviceIdInput by remember { mutableStateOf("") }
    var customerNameInput by remember { mutableStateOf("") }
    var generatedKeyResult by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.90f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFE65100)))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFB300), Color(0xFFE65100))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "بوابة المالك وتوليد التراخيص 👑",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isOwnerAuthenticated) "أهلاً بك يا مالك التطبيق • صلاحيات كاملة" else "يرجى تأكيد هوية المالك",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOwnerAuthenticated) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_owner_portal")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Gate: Check if Owner is authenticated
                    if (!isOwnerAuthenticated) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF8E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "منطقة خاصة بمالك التطبيق",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "هذا القسم مخصص للمالك والمستثمر فقط لتوليد وتصدير أكواد التفعيل للزبائن والمحلات. يرجى إدخال رمز المالك للمتابعة:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = ownerPinInput,
                                onValueChange = {
                                    ownerPinInput = it
                                    pinError = null
                                },
                                label = { Text("رمز أو كود المالك السري") },
                                placeholder = { Text("أدخل كلمة مرور المالك") },
                                leadingIcon = {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFB300))
                                },
                                singleLine = true,
                                isError = pinError != null,
                                supportingText = {
                                    if (pinError != null) {
                                        Text(pinError ?: "", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .testTag("input_owner_portal_pin")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val input = ownerPinInput.trim()
                                    if (viewModel.verifyOwnerSecret(input) || viewModel.activateAsOwner(input)) {
                                        isOwnerAuthenticated = true
                                        Toast.makeText(context, "تم التحقق من هوية المالك بنجاح! 👑", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pinError = "كلمة مرور المالك غير صحيحة"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(50.dp)
                                    .testTag("btn_verify_owner_pin"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تأكيد هوية المالك والدخول 👑", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        // OWNER DASHBOARD CONTENT
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("توليد كود لزبون", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(
                                            "سجل التراخيص (${securityState.generatedLicenses.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // TAB 0: GENERATE KEY
                        if (selectedTab == 0) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        ),
                                        border = CardDefaults.outlinedCardBorder()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "بيانات جهاز الزبون أو المحل:",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            // Device ID Input + Paste
                                            OutlinedTextField(
                                                value = customerDeviceIdInput,
                                                onValueChange = { customerDeviceIdInput = it.uppercase() },
                                                label = { Text("معرّف جهاز الزبون (Device ID)") },
                                                placeholder = { Text("مثال: SYR-B757-AD3F") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                                                },
                                                trailingIcon = {
                                                    IconButton(
                                                        onClick = {
                                                            clipboardManager.getText()?.let { clip ->
                                                                val text = clip.text.trim().uppercase()
                                                                if (text.isNotBlank()) {
                                                                    customerDeviceIdInput = text
                                                                    Toast.makeText(context, "تم لصق المعرف من الحافظة", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.ContentPaste, contentDescription = "لصق من الحافظة")
                                                    }
                                                },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            // Customer Name Input
                                            OutlinedTextField(
                                                value = customerNameInput,
                                                onValueChange = { customerNameInput = it },
                                                label = { Text("اسم الزبون أو المحل التجاري") },
                                                placeholder = { Text("مثال: مركز الأمل للاتصالات") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Storefront, contentDescription = null)
                                                },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Button(
                                                onClick = {
                                                    if (customerDeviceIdInput.isBlank() || customerNameInput.isBlank()) {
                                                        Toast.makeText(context, "يرجى إدخال معرف الجهاز واسم الزبون", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    val key = viewModel.generateCustomerLicense(
                                                        customerDeviceIdInput,
                                                        customerNameInput
                                                    )
                                                    generatedKeyResult = key
                                                    Toast.makeText(context, "تم توليد وحفظ كود التفعيل بنجاح! 🔑", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("btn_generate_license_key"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SyriatelRed)
                                            ) {
                                                Icon(Icons.Default.Key, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("توليد كود التفعيل فوراً 🔑", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Generated Key Display
                                if (generatedKeyResult != null) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                            border = BorderStroke(
                                                1.5.dp,
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF2E7D32), Color(0xFFFFB300))
                                                )
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "كود التفعيل جاهز للزبون:",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }

                                                Text(
                                                    text = generatedKeyResult ?: "",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 24.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    letterSpacing = 2.sp
                                                )

                                                Text(
                                                    text = "مخصص لـ: $customerNameInput ($customerDeviceIdInput)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(generatedKeyResult ?: ""))
                                                            Toast.makeText(context, "تم نسخ كود التفعيل للحافظة", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("نسخ الكود")
                                                    }

                                                    Button(
                                                        onClick = {
                                                            val shareText = """
                                                                مرحباً بك،
                                                                كود تفعيل تطبيق محول الرصيد السوري الخاص بجهازك:
                                                                - اسم العميل / المحل: $customerNameInput
                                                                - معرّف الجهاز: $customerDeviceIdInput
                                                                - كود التفعيل: ${generatedKeyResult ?: ""}
                                                                
                                                                انسخ كود التفعيل والصقه في خانة التفعيل بالتطبيق ليعمل فوراً. شكراً لتعاملك معنا.
                                                            """.trimIndent()
                                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                type = "text/plain"
                                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                            }
                                                            context.startActivity(Intent.createChooser(shareIntent, "إرسال كود التفعيل للزبون"))
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                                    ) {
                                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("إرسال للزبون")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TAB 1: ARCHIVE OF ISSUED LICENSES
                        if (selectedTab == 1) {
                            val licenses = securityState.generatedLicenses
                            if (licenses.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "لا توجد تراخيص محفوظة بعد",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "كل كود تقوم بتوليده سيتم حفظه هنا تلقائياً لترجع له في أي وقت.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(licenses, key = { it.licenseKey }) { item ->
                                        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                                            ),
                                            border = CardDefaults.outlinedCardBorder()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = SyriatelRed, modifier = Modifier.size(18.dp))
                                                        Text(item.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteArchivedLicense(item.licenseKey)
                                                            Toast.makeText(context, "تم حذف الترخيص من السجل", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "الجهاز: ${item.deviceId}",
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = dateStr,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = item.licenseKey,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )

                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            IconButton(
                                                                onClick = {
                                                                    clipboardManager.setText(AnnotatedString(item.licenseKey))
                                                                    Toast.makeText(context, "تم نسخ كود: ${item.customerName}", Toast.LENGTH_SHORT).show()
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    val shareText = """
                                                                        كود تفعيل تطبيق محول الرصيد السوري المخصص لك:
                                                                        - المحل: ${item.customerName}
                                                                        - الجهاز: ${item.deviceId}
                                                                        - الكود: ${item.licenseKey}
                                                                    """.trimIndent()
                                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                        type = "text/plain"
                                                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                                                    }
                                                                    context.startActivity(Intent.createChooser(shareIntent, "إرسال كود التفعيل"))
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(16.dp))
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
                }
            }
        }
    }
}
