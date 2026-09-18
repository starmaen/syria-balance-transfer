package com.example.data.security

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/**
 * Manages device-specific hardware identification and deterministic license generation/validation.
 * Ensures an activation code is strictly tied to a single physical device and customer name.
 */
object DeviceActivationManager {

    private const val MASTER_SALT = "SYRIA_TELECOM_INVESTOR_BINDING_SALT_2026_@#987"
    private const val PREFS_NAME = "device_identity_prefs"
    private const val KEY_CACHED_DEVICE_ID = "cached_device_unique_id"

    /**
     * تجاوز خاص بالمالك (المستثمر) فقط: يعمل على أي جهاز عند حدوث مشكلة، دون المرور
     * بخطوة توليد كود مرتبط بجهاز الزبون. الفرق عن النظام القديم أن السر الحقيقي (رقمك
     * السري) لا يوجد إطلاقًا كنص واضح داخل الكود أو داخل ملف الـ APK — المخزَّن هنا هو
     * فقط بصمة (هاش SHA-256) له، ولا يمكن استخراج الرقم الأصلي من الهاش.
     *
     * لتفعيل هذه الميزة:
     * 1. اختر رقمًا/رمزًا سريًا طويلاً ومعقدًا (يفضّل 8 خانات أو أكثر، وليس تاريخ ميلاد
     *    أو رقم متسلسل بسيط) لا يعرفه غيرك.
     * 2. احسب هاش SHA-256 له محليًا وبدون إنترنت عبر الأداة المرفقة في
     *    license_generator.html (قسم "حساب بصمة الرقم السري للمالك").
     * 3. الصق الناتج هنا بدل القيمة الفارغة أدناه، ولا تكتب رقمك السري نفسه هنا مطلقًا.
     *
     * طالما هذا الحقل فارغًا، ميزة التجاوز معطّلة تلقائيًا ولا يعمل أي كود عام على أي جهاز.
     */
    private const val OWNER_MASTER_UNLOCK_SHA256 = ""

    /**
     * Retrieves or generates a consistent, formatted hardware ID for this device.
     * Format: e.g. "SYR-A83F-92C1"
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cached = prefs.getString(KEY_CACHED_DEVICE_ID, null)
        if (!cached.isNullOrBlank()) {
            return cached
        }

        val rawAndroidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            null
        }

        val seed = if (!rawAndroidId.isNullOrBlank() && rawAndroidId != "9774d56d682e549c") {
            rawAndroidId
        } else {
            // Fallback to random installation UUID if Android ID is unavailable or emulator generic
            UUID.randomUUID().toString()
        }

        val formattedId = formatDeviceIdFromSeed(seed)
        prefs.edit().putString(KEY_CACHED_DEVICE_ID, formattedId).apply()
        return formattedId
    }

    /**
     * Formats raw seed into a clean readable token: SYR-XXXX-YYYY
     */
    private fun formatDeviceIdFromSeed(seed: String): String {
        val hash = sha256Hex(seed + MASTER_SALT)
        val part1 = hash.substring(0, 4).uppercase(Locale.ROOT)
        val part2 = hash.substring(4, 8).uppercase(Locale.ROOT)
        return "SYR-$part1-$part2"
    }

    /**
     * Generates a deterministic license activation key for a given device ID and customer name.
     * The investor uses this algorithm to give the activation code to the customer.
     * Format: e.g. "ACT-8429-1053"
     */
    fun generateLicenseKey(deviceId: String, customerName: String): String {
        val normalizedDevice = deviceId.trim().uppercase(Locale.ROOT)
        val normalizedName = customerName.trim().replace("\\s+".toRegex(), " ").lowercase(Locale.ROOT)
        val combined = "$normalizedDevice|$normalizedName|$MASTER_SALT"

        val hash = sha256Hex(combined)
        
        // Take 8 hexadecimal digits and convert to a clean alphanumeric code
        val chunk1 = hash.substring(0, 4).uppercase(Locale.ROOT)
        val chunk2 = hash.substring(4, 8).uppercase(Locale.ROOT)
        return "ACT-$chunk1-$chunk2"
    }

    /**
     * Validates whether a provided activation key matches the device and customer name,
     * أو يطابق تجاوز المالك السري (انظر OWNER_MASTER_UNLOCK_SHA256 أعلاه).
     */
    fun verifyLicenseKey(deviceId: String, customerName: String, inputKey: String): Boolean {
        if (customerName.isBlank() || inputKey.isBlank()) return false
        val cleanInput = inputKey.trim().uppercase(Locale.ROOT)
        val expectedKey = generateLicenseKey(deviceId, customerName)

        // Match exact or without hyphens
        val cleanExpected = expectedKey.replace("-", "")
        val cleanUser = cleanInput.replace("-", "")

        if (cleanUser == cleanExpected) return true

        return isOwnerMasterUnlock(inputKey)
    }

    /**
     * يقارن هاش الرقم المُدخل بالبصمة المخزّنة فقط — لا يحتفظ التطبيق بالرقم السري
     * نفسه في أي مكان. إن لم يضبط المالك OWNER_MASTER_UNLOCK_SHA256 بعد، هذا يعيد false دائمًا.
     */
    fun isOwnerMasterUnlock(inputKey: String): Boolean {
        if (OWNER_MASTER_UNLOCK_SHA256.isBlank()) return false
        return sha256Hex(inputKey.trim()).equals(OWNER_MASTER_UNLOCK_SHA256.trim(), ignoreCase = true)
    }

    fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
