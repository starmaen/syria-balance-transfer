package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_debts")
data class CustomerDebt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val totalDebt: Long = 0, // المبلغ المطلوب منه (مدين)
    val totalCredit: Long = 0, // المبلغ المدفوع زيادة مقدماً (دائن)
    val lastUpdated: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    // Net balance: positive means customer owes money (مدين), negative means customer has credit (دائن)
    val netBalance: Long get() = totalDebt - totalCredit
}
