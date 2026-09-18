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

    suspend fun getCustomerByLocalCode(code: String): CustomerDebt? =
        debtDao.getCustomerDebtByLocalCode(code.trim())

    /** يحدّد/يعدّل الكود المحلي لزبون موجود مسبقًا. */
    suspend fun setLocalCode(customerDebt: CustomerDebt, newCode: String) {
        debtDao.updateCustomerDebt(customerDebt.copy(localCode = newCode.trim()))
    }

    /**
     * تحويل بين زبونين/صديقين باستخدام الكود المحلي للطرف المُستقبِل فقط،
     * دون أي رصيد ثابت أو رقم مبرمج مسبقًا. يُسجَّل كحركة مدين على المُرسِل
     * ودائن على المُستقبِل ضمن دفتر الديون الحالي، ليبقى كل شيء قابلاً للمراجعة.
     */
    suspend fun transferBetweenCustomers(
        fromPhone: String,
        fromName: String,
        toLocalCode: String,
        amount: Long,
        note: String = ""
    ): Result<CustomerDebt> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("المبلغ يجب أن يكون أكبر من صفر"))
        }
        val receiver = debtDao.getCustomerDebtByLocalCode(toLocalCode.trim())
            ?: return Result.failure(NoSuchElementException("لا يوجد زبون مسجَّل بهذا الكود المحلي"))

        if (receiver.customerPhone == fromPhone.trim()) {
            return Result.failure(IllegalStateException("لا يمكن التحويل لنفس الزبون"))
        }

        val sender = debtDao.getCustomerDebtByPhone(fromPhone)
        val senderName = if (fromName.isNotBlank()) fromName else sender?.customerName ?: "زبون $fromPhone"

        if (sender != null) {
            debtDao.updateCustomerDebt(
                sender.copy(
                    customerName = senderName,
                    totalDebt = sender.totalDebt + amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } else {
            debtDao.insertCustomerDebt(
                CustomerDebt(
                    customerName = senderName,
                    customerPhone = fromPhone.trim(),
                    totalDebt = amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
        debtDao.insertTransaction(
            DebtTransaction(
                customerPhone = fromPhone.trim(),
                customerName = senderName,
                amount = amount,
                type = "PEER_OUT",
                note = "تحويل إلى ${receiver.customerName} (${receiver.localCode})${if (note.isNotBlank()) " - $note" else ""}",
                timestamp = System.currentTimeMillis()
            )
        )

        val updatedReceiver = receiver.copy(
            totalCredit = receiver.totalCredit + amount,
            lastUpdated = System.currentTimeMillis()
        )
        debtDao.updateCustomerDebt(updatedReceiver)
        debtDao.insertTransaction(
            DebtTransaction(
                customerPhone = receiver.customerPhone,
                customerName = receiver.customerName,
                amount = amount,
                type = "PEER_IN",
                note = "تحويل من $senderName${if (note.isNotBlank()) " - $note" else ""}",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(updatedReceiver)
    }

    suspend fun deleteCustomer(customerDebt: CustomerDebt) {
        debtDao.deleteCustomerDebtById(customerDebt.id)
        debtDao.deleteTransactionsForCustomer(customerDebt.customerPhone)
    }

    fun getCustomerTransactions(phone: String): Flow<List<DebtTransaction>> =
        debtDao.getTransactionsForCustomer(phone)
}
