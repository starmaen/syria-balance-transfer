package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_records")
data class TransferRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "OUTGOING" (تحويل للزبون) or "INCOMING" (تغذية جملة)
    val operatorType: String, // "SYRIATEL", "MTN", "CUSTOM"
    val operatorName: String,
    val customerPhone: String,
    val customerName: String = "",
    val amount: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val ussdCode: String = "",
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val isDebt: Boolean = false,
    val note: String = ""
)
