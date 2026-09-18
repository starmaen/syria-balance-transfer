package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SecurityState(
    val isActivated: Boolean = false,
    val licensedCustomerName: String = "",
    val deviceId: String = "",
    val activationKey: String = "",
    val activationDate: Long = 0L,
    val isAppLocked: Boolean = true,
    val investorUsername: String = "المستثمر",
    val autoLockEnabled: Boolean = true
)

class SecurityRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("telecom_security_prefs", Context.MODE_PRIVATE)

    val currentDeviceId: String = DeviceActivationManager.getDeviceId(context)

    private val _securityState = MutableStateFlow(loadInitialState())
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()

    private fun loadInitialState(): SecurityState {
        val activated = prefs.getBoolean(KEY_IS_ACTIVATED, false)
        val customerName = prefs.getString(KEY_CUSTOMER_NAME, "") ?: ""
        val activationKey = prefs.getString(KEY_ACTIVATION_KEY, "") ?: ""
        val activationDate = prefs.getLong(KEY_ACTIVATION_DATE, 0L)
        val investorUser = prefs.getString(KEY_INVESTOR_USERNAME, "المستثمر") ?: "المستثمر"
        val autoLock = prefs.getBoolean(KEY_AUTO_LOCK, true)

        // Verify stored license against current device ID for tampering protection
        val isValidLicense = if (activated) {
            DeviceActivationManager.verifyLicenseKey(currentDeviceId, customerName, activationKey)
        } else {
            false
        }

        return SecurityState(
            isActivated = isValidLicense,
            licensedCustomerName = if (isValidLicense) customerName else "",
            deviceId = currentDeviceId,
            activationKey = if (isValidLicense) activationKey else "",
            activationDate = activationDate,
            isAppLocked = isValidLicense && autoLock, // If activated and auto-lock is on, start locked
            investorUsername = investorUser,
            autoLockEnabled = autoLock
        )
    }

    /**
     * Activates the app using a license key tied to this device and customer name.
     */
    fun activate(customerName: String, key: String): Boolean {
        val trimmedName = customerName.trim()
        val trimmedKey = key.trim()

        if (DeviceActivationManager.verifyLicenseKey(currentDeviceId, trimmedName, trimmedKey)) {
            val now = System.currentTimeMillis()
            prefs.edit().apply {
                putBoolean(KEY_IS_ACTIVATED, true)
                putString(KEY_CUSTOMER_NAME, trimmedName)
                putString(KEY_ACTIVATION_KEY, trimmedKey)
                putLong(KEY_ACTIVATION_DATE, now)
                apply()
            }
            _securityState.value = _securityState.value.copy(
                isActivated = true,
                licensedCustomerName = trimmedName,
                activationKey = trimmedKey,
                activationDate = now,
                isAppLocked = false
            )
            return true
        }
        return false
    }

    /**
     * Deactivates the current device license (requires owner PIN).
     */
    fun deactivate(investorPin: String): Boolean {
        if (!verifyInvestorPin(investorPin)) return false

        prefs.edit().apply {
            putBoolean(KEY_IS_ACTIVATED, false)
            remove(KEY_CUSTOMER_NAME)
            remove(KEY_ACTIVATION_KEY)
            remove(KEY_ACTIVATION_DATE)
            apply()
        }
        _securityState.value = _securityState.value.copy(
            isActivated = false,
            licensedCustomerName = "",
            activationKey = "",
            activationDate = 0L,
            isAppLocked = true
        )
        return true
    }

    /**
     * Verifies the investor PIN (default: "123456"), or the owner's secret master
     * override (see DeviceActivationManager.OWNER_MASTER_UNLOCK_SHA256 — compared as a
     * hash, never stored as plain text in the source).
     */
    fun verifyInvestorPin(pin: String): Boolean {
        val storedPin = prefs.getString(KEY_INVESTOR_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        return pin == storedPin || DeviceActivationManager.isOwnerMasterUnlock(pin)
    }

    /**
     * Verifies investor username & PIN login.
     */
    fun verifyInvestorLogin(username: String, pin: String): Boolean {
        val storedUser = _securityState.value.investorUsername
        val userMatches = username.trim().equals(storedUser.trim(), ignoreCase = true) ||
                username.trim() == "admin" ||
                username.trim() == "المستثمر"
        return userMatches && verifyInvestorPin(pin)
    }

    /**
     * Unlocks the app after successful login.
     */
    fun unlockApp() {
        _securityState.value = _securityState.value.copy(isAppLocked = false)
    }

    /**
     * Locks the app to prevent unauthorized balance transfer or tampering.
     */
    fun lockApp() {
        _securityState.value = _securityState.value.copy(isAppLocked = true)
    }

    /**
     * Updates investor credentials.
     */
    fun updateInvestorCredentials(newUsername: String, newPin: String, currentPin: String): Boolean {
        if (!verifyInvestorPin(currentPin)) return false
        if (newPin.length < 4) return false

        val trimmedUser = newUsername.trim().ifBlank { "المستثمر" }
        prefs.edit().apply {
            putString(KEY_INVESTOR_USERNAME, trimmedUser)
            putString(KEY_INVESTOR_PIN, newPin)
            apply()
        }
        _securityState.value = _securityState.value.copy(
            investorUsername = trimmedUser
        )
        return true
    }

    /**
     * Enables or disables auto-lock on app startup.
     */
    fun setAutoLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCK, enabled).apply()
        _securityState.value = _securityState.value.copy(autoLockEnabled = enabled)
    }

    /**
     * Investor tool: Generates a license code for any customer device.
     */
    fun generateKeyForClient(clientDeviceId: String, clientName: String): String {
        return DeviceActivationManager.generateLicenseKey(clientDeviceId, clientName)
    }

    companion object {
        private const val KEY_IS_ACTIVATED = "key_is_activated"
        private const val KEY_CUSTOMER_NAME = "key_customer_name"
        private const val KEY_ACTIVATION_KEY = "key_activation_key"
        private const val KEY_ACTIVATION_DATE = "key_activation_date"
        private const val KEY_INVESTOR_USERNAME = "key_investor_username"
        private const val KEY_INVESTOR_PIN = "key_investor_pin"
        private const val KEY_AUTO_LOCK = "key_auto_lock"

        const val DEFAULT_PIN = "123456"
    }
}
