package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CustomerDebt
import com.example.data.model.DebtTransaction
import com.example.data.model.TransferRecord
import com.example.data.model.UssdShortcut

@Database(
    entities = [
        TransferRecord::class,
        CustomerDebt::class,
        DebtTransaction::class,
        UssdShortcut::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transferDao(): TransferDao
    abstract fun debtDao(): DebtDao
    abstract fun ussdShortcutDao(): UssdShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * ترحيل حقيقي من الإصدار 1 إلى 2: يضيف عمود الكود المحلي وجدول الأكواد الإضافية
         * دون حذف أي بيانات ديون أو تحويلات موجودة مسبقًا على أجهزة الزبائن المفعّلين حاليًا.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customer_debts ADD COLUMN localCode TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ussd_shortcuts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`label` TEXT NOT NULL, " +
                        "`code` TEXT NOT NULL)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "syria_balance_transfer.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // يبقى كحل احتياطي أخير فقط إن تعذّر تطبيق الترحيل لأي سبب نادر،
                    // وليس السلوك الافتراضي بعد الآن.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
