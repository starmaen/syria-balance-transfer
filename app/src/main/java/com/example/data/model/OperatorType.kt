package com.example.data.model

enum class OperatorType(
    val id: String,
    val defaultName: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val defaultPrefixes: List<String>
) {
    SYRIATEL(
        id = "SYRIATEL",
        defaultName = "سيريتل Syriatel",
        primaryColorHex = 0xFFD32F2F, // Red
        accentColorHex = 0xFFB71C1C,
        defaultPrefixes = listOf("093", "098", "099")
    ),
    MTN(
        id = "MTN",
        defaultName = "إم تي إن MTN",
        primaryColorHex = 0xFFFFB800, // MTN Yellow/Amber
        accentColorHex = 0xFFE6A800,
        defaultPrefixes = listOf("094", "095", "096")
    ),
    CUSTOM(
        id = "CUSTOM",
        defaultName = "المشغل الثالث (وفا تل)",
        primaryColorHex = 0xFF00897B, // Teal
        accentColorHex = 0xFF004D40,
        defaultPrefixes = listOf("097")
    );

    companion object {
        fun detectFromPhone(phone: String, customPrefix: String = "097"): OperatorType? {
            val clean = phone.replace(" ", "").replace("-", "")
            return when {
                SYRIATEL.defaultPrefixes.any { clean.startsWith(it) } -> SYRIATEL
                MTN.defaultPrefixes.any { clean.startsWith(it) } -> MTN
                clean.startsWith(customPrefix) -> CUSTOM
                else -> null
            }
        }
    }
}
