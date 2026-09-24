package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debt_transactions")
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerPhone: String,
    val customerName: String,
    val amount: Long,
    val type: String, // "DEBT_ADD" (تحويل رصيد - زيادة دين), "PAYMENT" (تسديد دفعة نقدية)
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
