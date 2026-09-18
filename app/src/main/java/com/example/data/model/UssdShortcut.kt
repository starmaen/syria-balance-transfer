package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كود USSD إضافي يضيفه المستخدم بنفسه (مثل تفقد الرصيد، تفعيل باقة، إلخ).
 * لا توجد أي أكواد ثابتة مبرمجة مسبقًا في الكود المصدري؛ القائمة بأكملها
 * يديرها المستخدم من شاشة الإعدادات (إضافة/حذف بحرية).
 */
@Entity(tableName = "ussd_shortcuts")
data class UssdShortcut(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String, // اسم وصفي يظهر للمستخدم، مثل "تفقد الرصيد - سيريتل"
    val code: String   // الكود الفعلي، مثل *121#
)
