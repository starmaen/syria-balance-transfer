package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OperatorType

@Composable
fun LcdNumberDisplay(
    phoneNumber: String,
    customerName: String,
    amount: Long,
    operator: OperatorType,
    customOperatorName: String,
    onOneClickTransfer: () -> Unit,
    onTransferAsDebt: () -> Unit,
    onClearCustomerName: () -> Unit,
    onChangeAmountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isValid = phoneNumber.length == 10 && phoneNumber.startsWith("09")
    val isPrefixValid = phoneNumber.isEmpty() || phoneNumber.startsWith("0") && (phoneNumber.length < 2 || phoneNumber.startsWith("09"))
    
    // Format display: 09XX XXX XXX
    val formattedPhone = when {
        phoneNumber.isEmpty() -> "09•• ••• •••"
        phoneNumber.length <= 4 -> phoneNumber
        phoneNumber.length <= 7 -> "${phoneNumber.substring(0, 4)} ${phoneNumber.substring(4)}"
        else -> "${phoneNumber.substring(0, 4)} ${phoneNumber.substring(4, 7)} ${phoneNumber.substring(7)}"
    }

    val operatorColor = when (operator) {
        OperatorType.SYRIATEL -> Color(0xFFE52E25)
        OperatorType.MTN -> Color(0xFFFFB800)
        OperatorType.CUSTOM -> Color(0xFF00897B)
    }

    val operatorDisplayName = when (operator) {
        OperatorType.SYRIATEL -> "سيريتل Syriatel"
        OperatorType.MTN -> "إم تي إن MTN"
        OperatorType.CUSTOM -> customOperatorName
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = if (isValid) listOf(Color(0xFF10B981), Color(0xFF059669))
                    else listOf(Color(0xFF334155), Color(0xFF1E293B))
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF0A0F1D) // High-contrast LCD terminal background
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar inside LCD Display: Operator badge & Validation status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Operator Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(operatorColor.copy(alpha = 0.2f))
                        .border(1.dp, operatorColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(operatorColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = operatorDisplayName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Validation Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isValid -> Color(0xFF064E3B)
                                !isPrefixValid -> Color(0xFF7F1D1D)
                                phoneNumber.isNotEmpty() -> Color(0xFF78350F)
                                else -> Color(0xFF1E293B)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isValid -> Icons.Default.CheckCircle
                            !isPrefixValid -> Icons.Default.ErrorOutline
                            phoneNumber.isNotEmpty() -> Icons.Default.HourglassEmpty
                            else -> Icons.Default.HourglassEmpty
                        },
                        contentDescription = null,
                        tint = when {
                            isValid -> Color(0xFF34D399)
                            !isPrefixValid -> Color(0xFFF87171)
                            phoneNumber.isNotEmpty() -> Color(0xFFFBBF24)
                            else -> Color(0xFF94A3B8)
                        },
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isValid -> "رقم صحيح (10 أرقام)"
                            !isPrefixValid -> "يجب البدء بـ 09"
                            phoneNumber.isNotEmpty() -> "متبقي ${10 - phoneNumber.length} أرقام"
                            else -> "أدخل رقم الزبون"
                        },
                        color = when {
                            isValid -> Color(0xFF34D399)
                            !isPrefixValid -> Color(0xFFF87171)
                            phoneNumber.isNotEmpty() -> Color(0xFFFBBF24)
                            else -> Color(0xFF94A3B8)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name Tag (if present)
            AnimatedVisibility(
                visible = customerName.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = customerName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف الاسم",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onClearCustomerName() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Phone Number Big Display
            Text(
                text = formattedPhone,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                color = when {
                    isValid -> Color(0xFF10B981) // Crisp glowing emerald
                    phoneNumber.isEmpty() -> Color(0xFF475569) // Placeholder gray
                    !isPrefixValid -> Color(0xFFEF4444)
                    else -> Color(0xFFF1F5F9)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lcd_phone_display")
            )

            // Amount Indicator Pill (Clickable to change amount)
            Surface(
                onClick = onChangeAmountClick,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3D56)),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 10.dp)
                    .testTag("lcd_amount_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "المبلغ المطلوب: ",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%,d ل.س".format(amount),
                        color = Color(0xFFFBBF24), // Gold accent
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✏️",
                        fontSize = 10.sp
                    )
                }
            }

            // Quick 1-Click Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary One-Click Instant Transfer Button
                Button(
                    onClick = onOneClickTransfer,
                    enabled = isValid && amount > 0,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("one_click_transfer_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isValid) Color(0xFF10B981) else Color(0xFF334155),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF1E293B),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تحويل فوري بنقرة واحدة",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Debt Transfer Button ("تسجيل كدين")
                OutlinedButton(
                    onClick = onTransferAsDebt,
                    enabled = isValid && amount > 0,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("transfer_as_debt_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF59E0B),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isValid && amount > 0) Color(0xFFF59E0B) else Color(0xFF334155)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تحويل بالدين",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
