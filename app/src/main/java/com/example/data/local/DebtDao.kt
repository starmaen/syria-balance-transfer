package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerDebt
import com.example.data.model.DebtTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM customer_debts ORDER BY (totalDebt - totalCredit) DESC, lastUpdated DESC")
    fun getAllCustomerDebts(): Flow<List<CustomerDebt>>

    @Query("SELECT * FROM customer_debts WHERE customerPhone = :phone LIMIT 1")
    suspend fun getCustomerDebtByPhone(phone: String): CustomerDebt?

    @Query("SELECT * FROM customer_debts WHERE localCode = :code LIMIT 1")
    suspend fun getCustomerDebtByLocalCode(code: String): CustomerDebt?

    @Query("SELECT * FROM customer_debts WHERE customerName LIKE '%' || :query || '%' OR customerPhone LIKE '%' || :query || '%'")
    fun searchCustomerDebts(query: String): Flow<List<CustomerDebt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerDebt(customerDebt: CustomerDebt): Long

    @Update
    suspend fun updateCustomerDebt(customerDebt: CustomerDebt)

    @Query("DELETE FROM customer_debts WHERE id = :id")
    suspend fun deleteCustomerDebtById(id: Long)

    @Query("SELECT SUM(totalDebt - totalCredit) FROM customer_debts WHERE (totalDebt - totalCredit) > 0")
    fun getTotalReceivableDebts(): Flow<Long?>

    // Debt Transactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DebtTransaction): Long

    @Query("SELECT * FROM debt_transactions WHERE customerPhone = :phone ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(phone: String): Flow<List<DebtTransaction>>

    @Query("DELETE FROM debt_transactions WHERE customerPhone = :phone")
    suspend fun deleteTransactionsForCustomer(phone: String)
}
