package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerDebt
import com.example.ui.viewmodel.TransferViewModel

@Composable
fun PeerTransferScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val debts by viewModel.allDebts.collectAsStateWithLifecycle()
    val shortcuts by viewModel.ussdShortcuts.collectAsStateWithLifecycle()

    var senderPhone by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var receiverCode by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    var shortcutLabel by remember { mutableStateOf("") }
    var shortcutCode by remember { mutableStateOf("") }

    var editingCustomer by remember { mutableStateOf<CustomerDebt?>(null) }
    var editingCodeText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ================= قسم: التحويل بين الزبائن =================
            item {
                Text(
                    "تحويل رصيد بين زبائن ومحل",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            item {
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("الزبون المُرسِل", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = senderPhone,
                            onValueChange = { senderPhone = it },
                            label = { Text("رقم هاتف المُرسِل") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("اسم المُرسِل (اختياري)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider()
                        Text("الزبون المُستقبِل", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = receiverCode,
                            onValueChange = { receiverCode = it },
                            label = { Text("الكود المحلي للمستقبِل") },
                            placeholder = { Text("مثال: 4521") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input -> if (input.all { it.isDigit() }) amountText = input },
                            label = { Text("المبلغ (ل.س)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val amount = amountText.toLongOrNull() ?: 0L
                                if (senderPhone.isBlank() || receiverCode.isBlank() || amount <= 0) {
                                    Toast.makeText(context, "يرجى تعبئة كل الحقول بمبلغ صحيح", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.transferBetweenCustomers(
                                    fromPhone = senderPhone.trim(),
                                    fromName = senderName.trim(),
                                    toLocalCode = receiverCode.trim(),
                                    amount = amount
                                ) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        senderPhone = ""; senderName = ""; receiverCode = ""; amountText = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("تنفيذ التحويل")
                        }
                    }
                }
            }

            // ================= قسم: الأكواد المحلية للزبائن =================
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "الأكواد المحلية للزبائن",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "كل زبون يدخل كوده الخاص مرة واحدة، وهو قابل للتعديل لاحقًا في أي وقت.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(debts, key = { it.id }) { customer ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(customer.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(customer.customerPhone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customer.localCode.ifBlank { "بدون كود" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (customer.localCode.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = {
                                editingCustomer = customer
                                editingCodeText = customer.localCode
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل الكود المحلي")
                            }
                        }
                    }
                }
            }

            // ================= قسم: أكواد إضافية (بدون أرقام ثابتة) =================
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "أكواد USSD إضافية",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "أضف أي كود تحتاجه (تفقد رصيد، باقات، إلخ) واحذفه وقتما تشاء — لا توجد أكواد ثابتة في التطبيق.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = shortcutLabel,
                                onValueChange = { shortcutLabel = it },
                                label = { Text("اسم الكود") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = shortcutCode,
                                onValueChange = { shortcutCode = it },
                                label = { Text("الكود") },
                                placeholder = { Text("*121#") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (shortcutLabel.isBlank() || shortcutCode.isBlank()) {
                                    Toast.makeText(context, "أدخل اسم الكود ونصّه", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                viewModel.addUssdShortcut(shortcutLabel, shortcutCode)
                                shortcutLabel = ""; shortcutCode = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إضافة الكود")
                        }
                    }
                }
            }
            items(shortcuts, key = { it.id }) { shortcut ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(shortcut.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(shortcut.code, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            IconButton(onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(shortcut.code)}"))
                                context.startActivity(dialIntent)
                            }) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "استخدام الكود")
                            }
                            IconButton(onClick = { viewModel.deleteUssdShortcut(shortcut.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog لتعديل الكود المحلي لزبون
    val customerBeingEdited = editingCustomer
    if (customerBeingEdited != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editingCustomer = null },
            title = { Text("الكود المحلي لـ ${customerBeingEdited.customerName}") },
            text = {
                OutlinedTextField(
                    value = editingCodeText,
                    onValueChange = { editingCodeText = it },
                    label = { Text("الكود المحلي") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setCustomerLocalCode(customerBeingEdited, editingCodeText)
                    editingCustomer = null
                }) { Text("حفظ") }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingCustomer = null }) { Text("إلغاء") }
            }
        )
    }
}
