package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OperatorType
import com.example.data.model.TransferRecord
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.TransferViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier
) {
    val transfers by viewModel.filteredTransfers.collectAsStateWithLifecycle()
    val totalOutgoing by viewModel.totalOutgoing.collectAsStateWithLifecycle()
    val totalIncoming by viewModel.totalIncoming.collectAsStateWithLifecycle()
    val currentFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedTransferIds.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var isSelectionMode by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var itemToDeleteSingle by remember { mutableStateOf<TransferRecord?>(null) }
    var showAddIncomingDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Stats Summary Card (Incoming & Outgoing totals)
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
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Outgoing Total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إجمالي الصادر للزبائن",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "%,d ل.س".format(totalOutgoing),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Incoming Total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إجمالي الوارد (شحن جملة)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "%,d ل.س".format(totalIncoming),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            // Selection Mode Action Bar OR Filter Chips
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isSelectionMode = false
                                    viewModel.clearSelectedTransfers()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إلغاء التحديد",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "تم تحديد (${selectedIds.size}) عملية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { viewModel.selectAllFilteredTransfers() }) {
                                Text("تحديد الكل", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showDeleteSelectedDialog = true },
                                enabled = selectedIds.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("delete_selected_transfers_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مسح المحدد", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Standard Toolbar with Filters & Selection Toggle
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Filter Chips
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterItem(
                            label = "الكل",
                            isSelected = currentFilter == HistoryFilter.ALL,
                            onClick = { viewModel.setHistoryFilter(HistoryFilter.ALL) }
                        )
                        FilterItem(
                            label = "صادر للزبائن",
                            isSelected = currentFilter == HistoryFilter.OUTGOING,
                            onClick = { viewModel.setHistoryFilter(HistoryFilter.OUTGOING) }
                        )
                        FilterItem(
                            label = "وارد (شحن)",
                            isSelected = currentFilter == HistoryFilter.INCOMING,
                            onClick = { viewModel.setHistoryFilter(HistoryFilter.INCOMING) }
                        )
                        FilterItem(
                            label = "سيريتل",
                            isSelected = currentFilter == HistoryFilter.SYRIATEL,
                            onClick = { viewModel.setHistoryFilter(HistoryFilter.SYRIATEL) }
                        )
                        FilterItem(
                            label = "MTN",
                            isSelected = currentFilter == HistoryFilter.MTN,
                            onClick = { viewModel.setHistoryFilter(HistoryFilter.MTN) }
                        )
                    }

                    // Selection Mode trigger button
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { isSelectionMode = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "تحديد مجموعة",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showClearAllDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "مسح كل السجل",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Transfer Records List
            if (transfers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📱", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد عمليات تحويل مسجلة في هذا القسم",
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
                    items(transfers, key = { it.id }) { record ->
                        TransferItemCard(
                            record = record,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(record.id),
                            onToggleSelect = { viewModel.toggleSelectTransfer(record.id) },
                            onDeleteSingle = { itemToDeleteSingle = record }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to add Incoming balance recharge record
        FloatingActionButton(
            onClick = { showAddIncomingDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_incoming_balance_fab"),
            containerColor = Color(0xFF10B981),
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "تسجيل رصيد وارد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Delete Single Item Dialog
    if (itemToDeleteSingle != null) {
        val target = itemToDeleteSingle!!
        AlertDialog(
            onDismissRequest = { itemToDeleteSingle = null },
            title = { Text("مسح عملية التحويل") },
            text = {
                Text("هل تريد بالتأكيد مسح عملية التحويل للرقم ${target.customerPhone} بمبلغ ${target.amount} ل.س من السجل؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSingleTransfer(target.id)
                        itemToDeleteSingle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح العملية")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeleteSingle = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Selected Items Dialog (Batch Delete)
    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            title = { Text("مسح العمليات المحددة") },
            text = {
                Text("هل تريد بالتأكيد مسح (${selectedIds.size}) عملية محددة من السجل نهائياً؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedTransfers()
                        showDeleteSelectedDialog = false
                        isSelectionMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_selected_button")
                ) {
                    Text("مسح المحدد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Clear All Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("مسح السجل بالكامل") },
            text = {
                Text("تحذير: سيتم مسح جميع سجلات التحويل الواردة والصادرة. هل ترغب في المتابعة؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTransfers()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Incoming Recharge Record Dialog
    if (showAddIncomingDialog) {
        var incomingAmount by remember { mutableStateOf("") }
        var incomingOperator by remember { mutableStateOf(OperatorType.SYRIATEL) }
        var incomingNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddIncomingDialog = false },
            title = { Text("تسجيل شحن رصيد وارد (جملة)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "اختر الشركة والمبلغ المستلم لتغذية رصيد المحل:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Operator buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { incomingOperator = OperatorType.SYRIATEL },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (incomingOperator == OperatorType.SYRIATEL) Color(0xFFD32F2F) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("سيريتل", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { incomingOperator = OperatorType.MTN },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (incomingOperator == OperatorType.MTN) Color(0xFFFFB800) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = if (incomingOperator == OperatorType.MTN) Color.Black else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("MTN", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { incomingOperator = OperatorType.CUSTOM },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (incomingOperator == OperatorType.CUSTOM) Color(0xFF00897B) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(settings.customOperatorName.take(8), fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = incomingAmount,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) incomingAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("المبلغ المشحون (ل.س)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = incomingNote,
                        onValueChange = { incomingNote = it },
                        label = { Text("ملاحظة (اسم الموزع / إيصال)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = incomingAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            viewModel.addIncomingBalance(incomingOperator, amount, incomingNote)
                            showAddIncomingDialog = false
                        }
                    }
                ) {
                    Text("حفظ بالسجل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddIncomingDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun FilterItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TransferItemCard(
    record: TransferRecord,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDeleteSingle: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(record.timestamp))
    val isIncoming = record.type == "INCOMING"

    val brandColor = when (record.operatorType) {
        "SYRIATEL" -> Color(0xFFD32F2F)
        "MTN" -> Color(0xFFFFB800)
        else -> Color(0xFF00897B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isSelectionMode, onClick = onToggleSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox in selection mode
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Direction / Operator Indicator Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncoming) Color(0xFF10B981) else brandColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isIncoming) "تغذية رصيد: ${record.operatorName}" else record.customerPhone,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = if (!isIncoming) FontFamily.Monospace else FontFamily.Default,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (record.isDebt) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دين",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (record.customerName.isNotBlank() && record.customerName != "زبون نقدي") {
                    Text(
                        text = "الزبون: ${record.customerName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Amount and Delete
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncoming) "+" else "-"}%,d ل.س".format(record.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isIncoming) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                )

                if (!isSelectionMode) {
                    IconButton(
                        onClick = onDeleteSingle,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "مسح العملية",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
