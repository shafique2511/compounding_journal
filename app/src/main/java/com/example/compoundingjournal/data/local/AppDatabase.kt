package com.example.compoundingjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.compoundingjournal.data.dao.FilterPresetDao
import com.example.compoundingjournal.data.dao.SettingsDao
import com.example.compoundingjournal.data.dao.StrategyDao
import com.example.compoundingjournal.data.dao.TradeDao
import com.example.compoundingjournal.data.entity.FilterPresetEntity
import com.example.compoundingjournal.data.entity.SettingsEntity
import com.example.compoundingjournal.data.entity.StrategyEntity
import com.example.compoundingjournal.data.entity.TradeEntity

@Database(
    entities = [
        TradeEntity::class,
        SettingsEntity::class,
        StrategyEntity::class,
        FilterPresetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun settingsDao(): SettingsDao
    abstract fun strategyDao(): StrategyDao
    abstract fun filterPresetDao(): FilterPresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Update trades table
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistTrendConfirmed INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistKeyLevelConfirmed INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistEntryReasonConfirmed INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistStopLossPlanned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistTakeProfitPlanned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistRiskAccepted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistNoRevengeTrade INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistNoOverlot INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistNewsChecked INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistEmotionStable INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistScore REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE trades ADD COLUMN checklistStatus TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE trades ADD COLUMN mistakeTags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE trades ADD COLUMN ruleFollowed TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE trades ADD COLUMN ruleBrokenNotes TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE trades ADD COLUMN tradeQualityScore REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE trades ADD COLUMN tradeQualityGrade TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE trades ADD COLUMN reviewCompleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE trades ADD COLUMN reviewDate TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE trades ADD COLUMN reviewNotes TEXT NOT NULL DEFAULT ''")

                // Update settings table
                database.execSQL("ALTER TABLE settings ADD COLUMN autoTradeNumber INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE settings ADD COLUMN maxRiskPerTradePercent REAL NOT NULL DEFAULT 2.0")
                database.execSQL("ALTER TABLE settings ADD COLUMN maxDailyLossPercent REAL NOT NULL DEFAULT 5.0")
                database.execSQL("ALTER TABLE settings ADD COLUMN maxWeeklyLossPercent REAL NOT NULL DEFAULT 10.0")
                database.execSQL("ALTER TABLE settings ADD COLUMN maxTradesPerDay INTEGER NOT NULL DEFAULT 5")
                database.execSQL("ALTER TABLE settings ADD COLUMN maxLosingStreakWarning INTEGER NOT NULL DEFAULT 3")
                database.execSQL("ALTER TABLE settings ADD COLUMN minimumRiskRewardRatio REAL NOT NULL DEFAULT 1.5")
                database.execSQL("ALTER TABLE settings ADD COLUMN enableRiskWarning INTEGER NOT NULL DEFAULT 1")

                // Create new tables
                database.execSQL("CREATE TABLE IF NOT EXISTS `strategies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `strategyName` TEXT NOT NULL, `marketType` TEXT NOT NULL, `timeframe` TEXT NOT NULL, `entryRules` TEXT NOT NULL, `exitRules` TEXT NOT NULL, `stopLossRules` TEXT NOT NULL, `takeProfitRules` TEXT NOT NULL, `riskRules` TEXT NOT NULL, `exampleScreenshotPath` TEXT, `notes` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
                
                database.execSQL("CREATE TABLE IF NOT EXISTS `filter_presets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `presetName` TEXT NOT NULL, `dateFilter` TEXT NOT NULL, `symbolFilter` TEXT NOT NULL, `timeframeFilter` TEXT NOT NULL, `strategyFilter` TEXT NOT NULL, `statusFilter` TEXT NOT NULL, `qualityGradeFilter` TEXT NOT NULL, `ruleFollowedFilter` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "compounding_journal_db"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
