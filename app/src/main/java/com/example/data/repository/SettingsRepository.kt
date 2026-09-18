package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
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

    // المبالغ السريعة المعروضة في لوحة المفاتيح، قابلة للإضافة والحذف من الإعدادات
    val quickAmounts: List<Long> = listOf(1000L, 2000L, 3000L, 5000L, 10000L, 25000L, 50000L)
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("telecom_settings", Context.MODE_PRIVATE)

    // تخزين مشفّر مخصص لأرقام PIN فقط، دون أي تعديل على بقية الإعدادات أو طريقة استدعائها
    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "telecom_settings_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // في حال فشل نظام التشفير على جهاز معين (نادر)، نعود مؤقتًا للتخزين العادي
            // بدل تعطيل التطبيق بالكامل.
            prefs
        }
    }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<OperatorSettings> = _settings.asStateFlow()

    init {
        migratePlainPinsIfNeeded()
    }

    /** ينقل أرقام PIN المخزّنة سابقًا بشكل عادي (قبل هذا التحديث) إلى التخزين المشفّر، مرة واحدة فقط. */
    private fun migratePlainPinsIfNeeded() {
        if (prefs.contains("syriatel_pin") || prefs.contains("mtn_pin") || prefs.contains("custom_pin")) {
            val editor = securePrefs.edit()
            prefs.getString("syriatel_pin", null)?.let { editor.putString("syriatel_pin", it) }
            prefs.getString("mtn_pin", null)?.let { editor.putString("mtn_pin", it) }
            prefs.getString("custom_pin", null)?.let { editor.putString("custom_pin", it) }
            editor.apply()

            prefs.edit()
                .remove("syriatel_pin")
                .remove("mtn_pin")
                .remove("custom_pin")
                .apply()

            _settings.value = loadSettings()
        }
    }

    private fun loadQuickAmounts(): List<Long> {
        val raw = prefs.getString("quick_amounts", null) ?: return OperatorSettings().quickAmounts
        val parsed = raw.split(",").mapNotNull { it.trim().toLongOrNull() }.filter { it > 0 }
        return if (parsed.isNotEmpty()) parsed else OperatorSettings().quickAmounts
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
        securePrefs.edit().apply {
            putString("syriatel_pin", newSettings.syriatelPin)
            putString("mtn_pin", newSettings.mtnPin)
            putString("custom_pin", newSettings.customOperatorPin)
            apply()
        }
        _settings.value = newSettings
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
