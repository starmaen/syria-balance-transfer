package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OperatorType

@Composable
fun OperatorSelector(
    selectedOperator: OperatorType,
    customOperatorName: String,
    onSelectOperator: (OperatorType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OperatorItem(
            name = "سيريتل",
            subtext = "093 - 098 - 099",
            brandColor = Color(0xFFD32F2F),
            icon = Icons.Default.SimCard,
            isSelected = selectedOperator == OperatorType.SYRIATEL,
            modifier = Modifier.weight(1f),
            onClick = { onSelectOperator(OperatorType.SYRIATEL) }
        )

        OperatorItem(
            name = "MTN",
            subtext = "094 - 095 - 096",
            brandColor = Color(0xFFFFB800),
            textColor = Color(0xFF1E1E1E),
            icon = Icons.Default.CellTower,
            isSelected = selectedOperator == OperatorType.MTN,
            modifier = Modifier.weight(1f),
            onClick = { onSelectOperator(OperatorType.MTN) }
        )

        OperatorItem(
            name = customOperatorName.take(10),
            subtext = "المشغل 3",
            brandColor = Color(0xFF00897B),
            icon = Icons.Default.SignalCellularAlt,
            isSelected = selectedOperator == OperatorType.CUSTOM,
            modifier = Modifier.weight(1f),
            onClick = { onSelectOperator(OperatorType.CUSTOM) }
        )
    }
}

@Composable
private fun OperatorItem(
    name: String,
    subtext: String,
    brandColor: Color,
    textColor: Color = Color.White,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) brandColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "border"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("operator_select_${name}"),
        color = if (isSelected) brandColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(brandColor),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
