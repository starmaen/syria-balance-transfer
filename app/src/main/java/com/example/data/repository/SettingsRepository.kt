package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.OperatorType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OperatorSettings(
    val syriatelPin: String = "1234",
    val syriatelUssdTemplate: String = "*150*1*{phone}*{amount}*{pin}#",
    
    val mtnPin: String = "1234",
    val mtnUssdTemplate: String = "*150*{phone}*{amount}*{pin}#",
    
    val customOperatorEnabled: Boolean = true,
    val customOperatorName: String = "وفا تل (Wafa)",
    val customOperatorPrefix: String = "097",
    val customOperatorPin: String = "1234",
    val customOperatorUssdTemplate: String = "*150*{phone}*{amount}*{pin}#",
    
    val shopName: String = "مركز النجمة لخدمات الرصيد",
    val shopPhone: String = "0933000000",
    val hapticFeedbackEnabled: Boolean = true,
    val sendSmsReceiptPrompt: Boolean = true,
    val quickAmounts: List<Long> = listOf(1000L, 2000L, 3000L, 5000L, 10000L, 25000L, 50000L)
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("telecom_settings", Context.MODE_PRIVATE)

    // Secure encrypted storage dedicated for PIN codes (Section 3.1)
    private val securePrefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "telecom_secure_pins",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("SettingsRepository", "EncryptedSharedPreferences init failed, falling back to standard prefs", e)
        context.getSharedPreferences("telecom_secure_pins_fallback", Context.MODE_PRIVATE)
    }

    init {
        migrateExistingPinsIfAny()
    }

    private fun migrateExistingPinsIfAny() {
        if (prefs.contains("syriatel_pin")) {
            val oldSyriatelPin = prefs.getString("syriatel_pin", "1234") ?: "1234"
            securePrefs.edit().putString("syriatel_pin", oldSyriatelPin).apply()
            prefs.edit().remove("syriatel_pin").apply()
        }
        if (prefs.contains("mtn_pin")) {
            val oldMtnPin = prefs.getString("mtn_pin", "1234") ?: "1234"
            securePrefs.edit().putString("mtn_pin", oldMtnPin).apply()
            prefs.edit().remove("mtn_pin").apply()
        }
        if (prefs.contains("custom_pin")) {
            val oldCustomPin = prefs.getString("custom_pin", "1234") ?: "1234"
            securePrefs.edit().putString("custom_pin", oldCustomPin).apply()
            prefs.edit().remove("custom_pin").apply()
        }
    }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<OperatorSettings> = _settings.asStateFlow()

    private fun loadQuickAmounts(): List<Long> {
        val defaultList = listOf(1000L, 2000L, 3000L, 5000L, 10000L, 25000L, 50000L)
        val raw = prefs.getString("quick_amounts", null) ?: return defaultList
        val parsed = raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { it > 0 }
            .distinct()
            .sorted()
        return if (parsed.isNotEmpty()) parsed else defaultList
    }

    private fun loadSettings(): OperatorSettings {
        return OperatorSettings(
            syriatelPin = securePrefs.getString("syriatel_pin", "1234") ?: "1234",
            syriatelUssdTemplate = prefs.getString("syriatel_ussd", "*150*1*{phone}*{amount}*{pin}#") ?: "*150*1*{phone}*{amount}*{pin}#",
            
            mtnPin = securePrefs.getString("mtn_pin", "1234") ?: "1234",
            mtnUssdTemplate = prefs.getString("mtn_ussd", "*150*{phone}*{amount}*{pin}#") ?: "*150*{phone}*{amount}*{pin}#",
            
            customOperatorEnabled = prefs.getBoolean("custom_enabled", true),
            customOperatorName = prefs.getString("custom_name", "وفا تل (Wafa)") ?: "وفا تل (Wafa)",
            customOperatorPrefix = prefs.getString("custom_prefix", "097") ?: "097",
            customOperatorPin = securePrefs.getString("custom_pin", "1234") ?: "1234",
            customOperatorUssdTemplate = prefs.getString("custom_ussd", "*150*{phone}*{amount}*{pin}#") ?: "*150*{phone}*{amount}*{pin}#",
            
            shopName = prefs.getString("shop_name", "مركز النجمة لخدمات الرصيد") ?: "مركز النجمة لخدمات الرصيد",
            shopPhone = prefs.getString("shop_phone", "0933000000") ?: "0933000000",
            hapticFeedbackEnabled = prefs.getBoolean("haptic_feedback", true),
            sendSmsReceiptPrompt = prefs.getBoolean("send_sms_prompt", true),
            quickAmounts = loadQuickAmounts()
        )
    }

    fun updateSettings(newSettings: OperatorSettings) {
        // Store sensitive PINs strictly in EncryptedSharedPreferences
        securePrefs.edit().apply {
            putString("syriatel_pin", newSettings.syriatelPin)
            putString("mtn_pin", newSettings.mtnPin)
            putString("custom_pin", newSettings.customOperatorPin)
            apply()
        }

        // Store non-sensitive configuration in standard preferences
        prefs.edit().apply {
            putString("syriatel_ussd", newSettings.syriatelUssdTemplate)
            putString("mtn_ussd", newSettings.mtnUssdTemplate)
            putBoolean("custom_enabled", newSettings.customOperatorEnabled)
            putString("custom_name", newSettings.customOperatorName)
            putString("custom_prefix", newSettings.customOperatorPrefix)
            putString("custom_ussd", newSettings.customOperatorUssdTemplate)
            putString("shop_name", newSettings.shopName)
            putString("shop_phone", newSettings.shopPhone)
            putBoolean("haptic_feedback", newSettings.hapticFeedbackEnabled)
            putBoolean("send_sms_prompt", newSettings.sendSmsReceiptPrompt)
            putString("quick_amounts", newSettings.quickAmounts.joinToString(","))
            apply()
        }
        _settings.value = newSettings
    }

    fun addQuickAmount(amount: Long) {
        if (amount <= 0) return
        val current = _settings.value.quickAmounts.toMutableList()
        if (!current.contains(amount)) {
            current.add(amount)
            current.sort()
            updateSettings(_settings.value.copy(quickAmounts = current))
        }
    }

    fun removeQuickAmount(amount: Long) {
        val current = _settings.value.quickAmounts.toMutableList()
        if (current.size > 1 && current.remove(amount)) {
            updateSettings(_settings.value.copy(quickAmounts = current))
        }
    }

    fun resetQuickAmounts() {
        val defaultList = listOf(1000L, 2000L, 3000L, 5000L, 10000L, 25000L, 50000L)
        updateSettings(_settings.value.copy(quickAmounts = defaultList))
    }

    /**
     * Builds the final executable USSD code replacing {phone}, {amount}, and {pin}.
     */
    fun buildUssdCode(operatorType: OperatorType, phone: String, amount: Long): String {
        val s = _settings.value
        val (template, pin) = when (operatorType) {
            OperatorType.SYRIATEL -> s.syriatelUssdTemplate to s.syriatelPin
            OperatorType.MTN -> s.mtnUssdTemplate to s.mtnPin
            OperatorType.CUSTOM -> s.customOperatorUssdTemplate to s.customOperatorPin
        }
        val cleanPhone = phone.replace(" ", "").replace("-", "")
        return template
            .replace("{phone}", cleanPhone)
            .replace("{amount}", amount.toString())
            .replace("{pin}", pin)
    }
}
