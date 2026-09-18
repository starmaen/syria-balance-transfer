package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerDebt
import com.example.data.model.DebtTransaction
import com.example.ui.viewmodel.TransferViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtsScreen(
    viewModel: TransferViewModel,
    onTransferToCustomer: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val debts by viewModel.filteredDebts.collectAsStateWithLifecycle()
    val totalReceivableDebts by viewModel.totalReceivableDebts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.debtSearchQuery.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerForPayment by remember { mutableStateOf<CustomerDebt?>(null) }
    var customerForDetails by remember { mutableStateOf<CustomerDebt?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerDebt?>(null) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        viewModel.onContactPicked(context, contactUri)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Debt Summary Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "دفتر ديون وذمم الزبائن (مدين ودائن)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "إجمالي المبالغ المستحقة من الزبائن:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "%,d ل.س".format(totalReceivableDebts),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setDebtSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("debt_search_input"),
                placeholder = { Text("ابحث باسم الزبون أو رقم الهاتف...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setDebtSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "مسح البحث")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Customers List
            if (debts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "لا يوجد زبائن مسجلين في دفتر الديون حالياً" else "لا توجد نتائج مطابقة للبحث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(debts, key = { it.id }) { customer ->
                        CustomerDebtCard(
                            customer = customer,
                            onSettlePayment = { customerForPayment = customer },
                            onViewDetails = { customerForDetails = customer },
                            onDirectTransfer = { onTransferToCustomer(customer.customerPhone, customer.customerName) },
                            onDelete = { customerToDelete = customer },
                            onSendReminder = {
                                sendDebtReminder(context, customer, settings.shopName)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add New Customer Debt
        FloatingActionButton(
            onClick = { showAddCustomerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_customer_debt_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "زبون جديد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var initialAmount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("إضافة زبون في دفتر الديون") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الزبون") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        label = { Text("رقم الهاتف (09XXXXXXXX)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = initialAmount,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) initialAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("مبلغ الدين الابتدائي (اختياري - ل.س)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("ملاحظة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            val amount = initialAmount.toLongOrNull() ?: 0L
                            viewModel.addCustomerDebt(name, phone, amount, note)
                            showAddCustomerDialog = false
                        }
                    },
                    enabled = name.isNotBlank() && phone.length >= 7
                ) {
                    Text("إضافة الحساب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Settle Payment Dialog (تسديد دفعة نقدية)
    if (customerForPayment != null) {
        val target = customerForPayment!!
        var paymentAmount by remember { mutableStateOf(target.netBalance.coerceAtLeast(0L).toString()) }
        var paymentNote by remember { mutableStateOf("قبض دفعة نقدية") }

        AlertDialog(
            onDismissRequest = { customerForPayment = null },
            title = { Text("تسديد دفعة نقدية من الزبون") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "الزبون: ${target.customerName} (${target.customerPhone})",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "المبلغ المطلوب حالياً: %,d ل.س".format(target.netBalance),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) paymentAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("المبلغ المقبوض نقداً (ل.س)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_amount_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("ملاحظة أو بيان") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            viewModel.recordPayment(target.customerPhone, target.customerName, amount, paymentNote)
                            customerForPayment = null
                        }
                    },
                    modifier = Modifier.testTag("confirm_payment_btn")
                ) {
                    Text("تأكيد القبض والتسديد")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerForPayment = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Customer Account Statement / Details Dialog
    if (customerForDetails != null) {
        val target = customerForDetails!!
        val transactions by viewModel.getCustomerTransactions(target.customerPhone).collectAsStateWithLifecycle(emptyList())

        AlertDialog(
            onDismissRequest = { customerForDetails = null },
            title = {
                Column {
                    Text(text = "كشف حساب: ${target.customerName}")
                    Text(
                        text = "رقم الهاتف: ${target.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Current Balance Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الرصيد الصافي:", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (target.netBalance > 0) "مدين: %,d ل.س".format(target.netBalance)
                            else if (target.netBalance < 0) "دائن: %,d ل.س".format(-target.netBalance)
                            else "خالص (0 ل.س)",
                            color = if (target.netBalance > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "سجل العمليات والتحويلات:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد حركات مسجلة لهذا الزبون", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(transactions, key = { it.id }) { tx ->
                                TransactionRowItem(tx)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { customerForDetails = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Delete Customer Dialog
    if (customerToDelete != null) {
        val target = customerToDelete!!
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("حذف الزبون من الدفتر") },
            text = {
                Text("هل أنت متأكد من حذف حساب الزبون ${target.customerName} وجميع حركات ديونه من السجل؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(target)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun CustomerDebtCard(
    customer: CustomerDebt,
    onSettlePayment: () -> Unit,
    onViewDetails: () -> Unit,
    onDirectTransfer: () -> Unit,
    onDelete: () -> Unit,
    onSendReminder: () -> Unit
) {
    val isDebtor = customer.netBalance > 0 // مدين (مطلوب منه)
    val isCreditor = customer.netBalance < 0 // دائن (له رصيد)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDebtor) Color(0xFFEF4444).copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Name & Balance Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDebtor) Color(0xFFEF4444).copy(alpha = 0.15f)
                                else Color(0xFF10B981).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isDebtor) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = customer.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = customer.customerPhone,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Balance Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isDebtor -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        isCreditor -> Color(0xFF10B981).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = when {
                                isDebtor -> "مدين (مطلوب منه):"
                                isCreditor -> "دائن (له رصيد):"
                                else -> "خالص الذمة"
                            },
                            fontSize = 10.sp,
                            color = when {
                                isDebtor -> Color(0xFFEF4444)
                                isCreditor -> Color(0xFF10B981)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "%,d ل.س".format(kotlin.math.abs(customer.netBalance)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                isDebtor -> Color(0xFFEF4444)
                                isCreditor -> Color(0xFF10B981)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Quick Actions: Settle Payment, Direct Transfer, Details, Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settle Payment Button
                Button(
                    onClick = onSettlePayment,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settle_debt_btn_${customer.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("قبض دفعة", fontSize = 11.sp)
                }

                // Transfer to customer button
                OutlinedButton(
                    onClick = onDirectTransfer,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تحويل رصيد", fontSize = 11.sp)
                }

                // Details Statement Button
                IconButton(onClick = onViewDetails, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "كشف الحساب",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Send WhatsApp/SMS Reminder
                if (isDebtor) {
                    IconButton(onClick = onSendReminder, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "إرسال تذكير",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Delete customer
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(tx: DebtTransaction) {
    val isPayment = tx.type == "PAYMENT"
    val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.timestamp))

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPayment) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isPayment) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (isPayment) "تسديد دفعة نقدية" else tx.note.ifBlank { "تحويل رصيد" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = dateStr, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Text(
                text = "${if (isPayment) "-" else "+"}%,d ل.س".format(tx.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isPayment) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

private fun sendDebtReminder(context: Context, customer: CustomerDebt, shopName: String) {
    val message = """
        مرحباً أخي ${customer.customerName}،
        نحيطكم علماً بأن الرصيد المتبقي بذمتكم لـ ($shopName) هو: %,d ل.س.
        يرجى تسديد المبلغ عند أقرب فرصة، وشكراً لتعاملكم معنا.
    """.trimIndent().format(customer.netBalance)

    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "إرسال تذكير بالدين"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
