package com.example.data.repository

import com.example.data.local.DebtDao
import com.example.data.model.CustomerDebt
import com.example.data.model.DebtTransaction
import kotlinx.coroutines.flow.Flow

class DebtRepository(private val debtDao: DebtDao) {
    val allDebts: Flow<List<CustomerDebt>> = debtDao.getAllCustomerDebts()
    val totalReceivableDebts: Flow<Long?> = debtDao.getTotalReceivableDebts()

    fun searchDebts(query: String): Flow<List<CustomerDebt>> =
        debtDao.searchCustomerDebts(query)

    suspend fun getCustomerDebt(phone: String): CustomerDebt? =
        debtDao.getCustomerDebtByPhone(phone)

    suspend fun recordTransferAsDebt(customerPhone: String, customerName: String, amount: Long, note: String = "تحويل رصيد") {
        val existing = debtDao.getCustomerDebtByPhone(customerPhone)
        val name = if (customerName.isNotBlank()) customerName else existing?.customerName ?: "زبون $customerPhone"
        
        if (existing != null) {
            debtDao.updateCustomerDebt(
                existing.copy(
                    customerName = name,
                    totalDebt = existing.totalDebt + amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } else {
            debtDao.insertCustomerDebt(
                CustomerDebt(
                    customerName = name,
                    customerPhone = customerPhone,
                    totalDebt = amount,
                    totalCredit = 0,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        debtDao.insertTransaction(
            DebtTransaction(
                customerPhone = customerPhone,
                customerName = name,
                amount = amount,
                type = "DEBT_ADD",
                note = note,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordPayment(customerPhone: String, customerName: String, amount: Long, note: String = "تسديد دفعة نقدية") {
        val existing = debtDao.getCustomerDebtByPhone(customerPhone)
        val name = if (customerName.isNotBlank()) customerName else existing?.customerName ?: "زبون $customerPhone"

        if (existing != null) {
            debtDao.updateCustomerDebt(
                existing.copy(
                    customerName = name,
                    totalCredit = existing.totalCredit + amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } else {
            debtDao.insertCustomerDebt(
                CustomerDebt(
                    customerName = name,
                    customerPhone = customerPhone,
                    totalDebt = 0,
                    totalCredit = amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        debtDao.insertTransaction(
            DebtTransaction(
                customerPhone = customerPhone,
                customerName = name,
                amount = amount,
                type = "PAYMENT",
                note = note,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCustomer(customerDebt: CustomerDebt) {
        debtDao.deleteCustomerDebtById(customerDebt.id)
        debtDao.deleteTransactionsForCustomer(customerDebt.customerPhone)
    }

    fun getCustomerTransactions(phone: String): Flow<List<DebtTransaction>> =
        debtDao.getTransactionsForCustomer(phone)
}
