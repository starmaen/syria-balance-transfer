package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CustomerDebt
import com.example.data.model.DebtTransaction
import com.example.data.model.OperatorType
import com.example.data.model.TransferRecord
import com.example.data.repository.DebtRepository
import com.example.data.repository.OperatorSettings
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TransferRepository
import com.example.data.repository.UssdShortcutRepository
import com.example.data.security.SecurityRepository
import com.example.data.security.SecurityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter {
    ALL,
    OUTGOING,
    INCOMING,
    SYRIATEL,
    MTN,
    CUSTOM
}

data class TransferUiState(
    val selectedOperator: OperatorType = OperatorType.SYRIATEL,
    val phoneNumber: String = "",
    val customerName: String = "",
    val amountText: String = "5000",
    val customAmountDialogVisible: Boolean = false,
    val isTransferring: Boolean = false,
    val isDebtTransfer: Boolean = false,
    val lastSuccessTransfer: TransferRecord? = null,
    val showSuccessDialog: Boolean = false,
    val activeTab: Int = 0 // 0: Dial/Transfer, 1: History, 2: Debts, 3: Settings
)

class TransferViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val transferRepository = TransferRepository(database.transferDao())
    val debtRepository = DebtRepository(database.debtDao())
    val settingsRepository = SettingsRepository(application)
    val securityRepository = SecurityRepository(application)
    val ussdShortcutRepository = UssdShortcutRepository(database.ussdShortcutDao())

    val ussdShortcuts = ussdShortcutRepository.all.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val settings: StateFlow<OperatorSettings> = settingsRepository.settings
    val securityState: StateFlow<SecurityState> = securityRepository.securityState

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    // History state
    val allTransfers = transferRepository.allTransfers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val totalOutgoing = transferRepository.totalOutgoing.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0L
    )
    val totalIncoming = transferRepository.totalIncoming.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0L
    )

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val _selectedTransferIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTransferIds: StateFlow<Set<Long>> = _selectedTransferIds.asStateFlow()

    val filteredTransfers = combine(allTransfers, _historyFilter) { list, filter ->
        when (filter) {
            HistoryFilter.ALL -> list
            HistoryFilter.OUTGOING -> list.filter { it.type == "OUTGOING" }
            HistoryFilter.INCOMING -> list.filter { it.type == "INCOMING" }
            HistoryFilter.SYRIATEL -> list.filter { it.operatorType == "SYRIATEL" }
            HistoryFilter.MTN -> list.filter { it.operatorType == "MTN" }
            HistoryFilter.CUSTOM -> list.filter { it.operatorType == "CUSTOM" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Debts state
    private val _debtSearchQuery = MutableStateFlow("")
    val debtSearchQuery: StateFlow<String> = _debtSearchQuery.asStateFlow()

    val allDebts = debtRepository.allDebts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val totalReceivableDebts = debtRepository.totalReceivableDebts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0L
    )

    val filteredDebts = combine(allDebts, _debtSearchQuery) { debts, query ->
        if (query.isBlank()) debts
        else debts.filter { it.customerName.contains(query, ignoreCase = true) || it.customerPhone.contains(query) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerHapticFeedback() {
        if (settings.value.hapticFeedbackEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        }
    }

    fun setActiveTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun selectOperator(operator: OperatorType) {
        triggerHapticFeedback()
        _uiState.value = _uiState.value.copy(selectedOperator = operator)
    }

    fun onKeypadDigit(digit: String) {
        triggerHapticFeedback()
        val current = _uiState.value.phoneNumber
        if (current.length < 10) {
            val updated = current + digit
            updatePhoneAndAutoDetect(updated)
        }
    }

    fun onKeypadBackspace() {
        triggerHapticFeedback()
        val current = _uiState.value.phoneNumber
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            updatePhoneAndAutoDetect(updated)
        }
    }

    fun onKeypadClear() {
        triggerHapticFeedback()
        _uiState.value = _uiState.value.copy(phoneNumber = "", customerName = "")
    }

    fun setQuickPrefix(prefix: String) {
        triggerHapticFeedback()
        updatePhoneAndAutoDetect(prefix)
    }

    fun setAmount(amount: Long) {
        triggerHapticFeedback()
        _uiState.value = _uiState.value.copy(amountText = amount.toString())
    }

    fun setCustomAmount(amountString: String) {
        val cleaned = amountString.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(amountText = cleaned)
    }

    private fun updatePhoneAndAutoDetect(phone: String) {
        val autoOperator = OperatorType.detectFromPhone(phone, settings.value.customOperatorPrefix)
        val newOperator = autoOperator ?: _uiState.value.selectedOperator
        _uiState.value = _uiState.value.copy(
            phoneNumber = phone,
            selectedOperator = newOperator
        )
        // Check if phone matches any customer in debts to auto-fill name
        viewModelScope.launch {
            if (phone.length >= 7) {
                val customer = debtRepository.getCustomerDebt(phone)
                if (customer != null && _uiState.value.customerName.isBlank()) {
                    _uiState.value = _uiState.value.copy(customerName = customer.customerName)
                }
            }
        }
    }

    fun onContactPicked(context: Context, contactUri: Uri?) {
        if (contactUri == null) return
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    if (numberIndex != -1) {
                        var rawNumber = cursor.getString(numberIndex) ?: ""
                        rawNumber = rawNumber.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
                        // Convert international +963 to 0
                        if (rawNumber.startsWith("+963")) {
                            rawNumber = "0" + rawNumber.removePrefix("+963")
                        } else if (rawNumber.startsWith("00963")) {
                            rawNumber = "0" + rawNumber.removePrefix("00963")
                        }
                        if (rawNumber.length > 10) rawNumber = rawNumber.take(10)
                        
                        val name = if (nameIndex != -1) cursor.getString(nameIndex) ?: "" else ""
                        _uiState.value = _uiState.value.copy(customerName = name)
                        updatePhoneAndAutoDetect(rawNumber)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun executeTransfer(asDebt: Boolean) {
        triggerHapticFeedback()
        val phone = _uiState.value.phoneNumber
        val amount = _uiState.value.amountText.toLongOrNull() ?: 0L
        if (phone.length != 10 || amount <= 0) return

        val operator = _uiState.value.selectedOperator
        val operatorName = when (operator) {
            OperatorType.SYRIATEL -> "سيريتل Syriatel"
            OperatorType.MTN -> "إم تي إن MTN"
            OperatorType.CUSTOM -> settings.value.customOperatorName
        }
        val ussdCode = settingsRepository.buildUssdCode(operator, phone, amount)

        val record = TransferRecord(
            type = "OUTGOING",
            operatorType = operator.name,
            operatorName = operatorName,
            customerPhone = phone,
            customerName = _uiState.value.customerName.ifBlank { "زبون نقدي" },
            amount = amount,
            timestamp = System.currentTimeMillis(),
            ussdCode = ussdCode,
            status = "SUCCESS",
            isDebt = asDebt,
            note = if (asDebt) "تحويل رصيد بالدين" else "تحويل رصيد نقدي"
        )

        viewModelScope.launch {
            val id = transferRepository.addTransfer(record)
            if (asDebt) {
                debtRepository.recordTransferAsDebt(
                    customerPhone = phone,
                    customerName = _uiState.value.customerName,
                    amount = amount,
                    note = "تحويل رصيد $operatorName بمبلغ $amount ل.س"
                )
            }
            _uiState.value = _uiState.value.copy(
                lastSuccessTransfer = record.copy(id = id),
                showSuccessDialog = true
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }

    fun addIncomingBalance(operator: OperatorType, amount: Long, note: String) {
        val operatorName = when (operator) {
            OperatorType.SYRIATEL -> "سيريتل Syriatel"
            OperatorType.MTN -> "إم تي إن MTN"
            OperatorType.CUSTOM -> settings.value.customOperatorName
        }
        val record = TransferRecord(
            type = "INCOMING",
            operatorType = operator.name,
            operatorName = operatorName,
            customerPhone = "الموزع المعتمد",
            customerName = "تغذية رصيد جملة",
            amount = amount,
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS",
            note = note.ifBlank { "شحن رصيد جملة من الموزع" }
        )
        viewModelScope.launch {
            transferRepository.addTransfer(record)
        }
    }

    // History Actions
    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun toggleSelectTransfer(id: Long) {
        val current = _selectedTransferIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedTransferIds.value = current
    }

    fun selectAllFilteredTransfers() {
        val allIds = filteredTransfers.value.map { it.id }.toSet()
        _selectedTransferIds.value = allIds
    }

    fun clearSelectedTransfers() {
        _selectedTransferIds.value = emptySet()
    }

    fun deleteSelectedTransfers() {
        viewModelScope.launch {
            val ids = _selectedTransferIds.value.toList()
            if (ids.isNotEmpty()) {
                transferRepository.deleteTransfers(ids)
                _selectedTransferIds.value = emptySet()
            }
        }
    }

    fun deleteSingleTransfer(id: Long) {
        viewModelScope.launch {
            transferRepository.deleteTransfer(id)
            val current = _selectedTransferIds.value.toMutableSet()
            current.remove(id)
            _selectedTransferIds.value = current
        }
    }

    fun clearAllTransfers() {
        viewModelScope.launch {
            transferRepository.clearAll()
            _selectedTransferIds.value = emptySet()
        }
    }

    // Debt Actions
    fun setDebtSearchQuery(query: String) {
        _debtSearchQuery.value = query
    }

    fun recordPayment(customerPhone: String, customerName: String, amount: Long, note: String) {
        viewModelScope.launch {
            debtRepository.recordPayment(customerPhone, customerName, amount, note)
        }
    }

    fun addCustomerDebt(name: String, phone: String, initialDebt: Long, note: String) {
        viewModelScope.launch {
            debtRepository.recordTransferAsDebt(phone, name, initialDebt, note.ifBlank { "افتتاح حساب ذمة" })
        }
    }

    fun deleteCustomer(customerDebt: CustomerDebt) {
        viewModelScope.launch {
            debtRepository.deleteCustomer(customerDebt)
        }
    }

    fun getCustomerTransactions(phone: String) =
        debtRepository.getCustomerTransactions(phone)

    fun setCustomerLocalCode(customerDebt: CustomerDebt, newCode: String) {
        viewModelScope.launch {
            debtRepository.setLocalCode(customerDebt, newCode)
        }
    }

    fun transferBetweenCustomers(
        fromPhone: String,
        fromName: String,
        toLocalCode: String,
        amount: Long,
        note: String = "",
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            val result = debtRepository.transferBetweenCustomers(fromPhone, fromName, toLocalCode, amount, note)
            result.fold(
                onSuccess = { onResult(true, "تم التحويل إلى ${it.customerName} بنجاح") },
                onFailure = { onResult(false, it.message ?: "تعذّر إتمام التحويل") }
            )
        }
    }

    // أكواد USSD إضافية (بدون أرقام ثابتة بالكود، تُدار بالكامل من المستخدم)
    fun addUssdShortcut(label: String, code: String) {
        if (label.isBlank() || code.isBlank()) return
        viewModelScope.launch {
            ussdShortcutRepository.add(label, code)
        }
    }

    fun deleteUssdShortcut(id: Long) {
        viewModelScope.launch {
            ussdShortcutRepository.delete(id)
        }
    }

    // Settings
    fun updateSettings(newSettings: OperatorSettings) {
        settingsRepository.updateSettings(newSettings)
    }

    // Security & Activation
    fun activateLicense(customerName: String, key: String): Boolean {
        return securityRepository.activate(customerName, key)
    }

    fun verifyInvestorLogin(username: String, pin: String): Boolean {
        val success = securityRepository.verifyInvestorLogin(username, pin)
        if (success) {
            securityRepository.unlockApp()
        }
        return success
    }

    fun verifyInvestorPin(pin: String): Boolean {
        return securityRepository.verifyInvestorPin(pin)
    }

    fun unlockAppWithPin(pin: String): Boolean {
        val success = securityRepository.verifyInvestorPin(pin)
        if (success) {
            securityRepository.unlockApp()
        }
        return success
    }

    fun lockApp() {
        securityRepository.lockApp()
    }

    fun unlockApp() {
        securityRepository.unlockApp()
    }

    fun updateInvestorCredentials(newUsername: String, newPin: String, currentPin: String): Boolean {
        return securityRepository.updateInvestorCredentials(newUsername, newPin, currentPin)
    }

    fun setAutoLock(enabled: Boolean) {
        securityRepository.setAutoLock(enabled)
    }

    fun deactivateLicense(investorPin: String): Boolean {
        return securityRepository.deactivate(investorPin)
    }

    fun generateClientLicenseKey(clientDeviceId: String, clientName: String): String {
        return securityRepository.generateKeyForClient(clientDeviceId, clientName)
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransferViewModel(application) as T
                }
            }
    }
}
