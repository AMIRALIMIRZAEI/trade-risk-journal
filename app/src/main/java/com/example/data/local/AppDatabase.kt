package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [TradeEntity::class, ChecklistItemEntity::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun tradeDao(): TradeDao
  abstract fun checklistDao(): ChecklistDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "trade_journal_database"
        )
          .addCallback(DatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch {
            populateInitialData(database.checklistDao(), database.tradeDao())
          }
        }
      }

      suspend fun populateInitialData(checklistDao: ChecklistDao, tradeDao: TradeDao) {
        // Prepopulate standard high-probability Pre-Trade Checklist checkpoints
        val defaultChecklist = listOf(
          ChecklistItemEntity(title = "Higher Timeframe (4H/Daily) Trend Aligned", category = "Analysis", orderIndex = 0),
          ChecklistItemEntity(title = "Clear Support / Resistance / Key Level identified", category = "Analysis", orderIndex = 1),
          ChecklistItemEntity(title = "Position Risk is strictly <= 2% of Total Capital", category = "Risk", orderIndex = 2),
          ChecklistItemEntity(title = "Defined Invalidation / Stop Loss price before entry", category = "Risk", orderIndex = 3),
          ChecklistItemEntity(title = "Risk-to-Reward Ratio is at least 1:2.0+", category = "Risk", orderIndex = 4),
          ChecklistItemEntity(title = "No high-impact economic news within 30 minutes", category = "Execution", orderIndex = 5),
          ChecklistItemEntity(title = "Calm mindset: No revenge or FOMO impulse", category = "Psychology", orderIndex = 6)
        )
        checklistDao.insertAll(defaultChecklist)

        // Prepopulate starter sample trades to showcase the interactive Equity Curve, Calendar, and Analytics
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L

        val starterTrades = listOf(
          TradeEntity(
            symbol = "BTC/USDT",
            tradeType = TradeType.LONG,
            entryPrice = 64200.0,
            takeProfit = 68500.0,
            stopLoss = 62500.0,
            marginAmount = 450.0,
            leverage = 5.0,
            totalCapital = 10000.0,
            riskPercentage = 1.5,
            status = TradeStatus.CLOSED_WIN,
            exitPrice = 68450.0,
            realizedPnl = 150.85,
            realizedPnlPercent = 33.52,
            notes = "Breakout above 4H resistance zone with high volume confirmation.",
            strategyTag = "Breakout",
            openTimestamp = now - (dayMillis * 6),
            closeTimestamp = now - (dayMillis * 5)
          ),
          TradeEntity(
            symbol = "ETH/USDT",
            tradeType = TradeType.SHORT,
            entryPrice = 3450.0,
            takeProfit = 3280.0,
            stopLoss = 3520.0,
            marginAmount = 350.0,
            leverage = 3.0,
            totalCapital = 10150.85,
            riskPercentage = 1.0,
            status = TradeStatus.CLOSED_WIN,
            exitPrice = 3290.0,
            realizedPnl = 48.70,
            realizedPnlPercent = 13.91,
            notes = "Bearish divergence on 1H RSI with rejection at supply block.",
            strategyTag = "Reversal",
            openTimestamp = now - (dayMillis * 4),
            closeTimestamp = now - (dayMillis * 4) + 14400000L
          ),
          TradeEntity(
            symbol = "SOL/USDT",
            tradeType = TradeType.LONG,
            entryPrice = 142.50,
            takeProfit = 158.00,
            stopLoss = 138.00,
            marginAmount = 300.0,
            leverage = 2.0,
            totalCapital = 10199.55,
            riskPercentage = 1.0,
            status = TradeStatus.CLOSED_LOSS,
            exitPrice = 137.90,
            realizedPnl = -19.37,
            realizedPnlPercent = -6.46,
            notes = "Tested support but fakeout wick triggered stop loss.",
            strategyTag = "S/R Bounce",
            openTimestamp = now - (dayMillis * 3),
            closeTimestamp = now - (dayMillis * 3) + 7200000L
          ),
          TradeEntity(
            symbol = "EUR/USD",
            tradeType = TradeType.LONG,
            entryPrice = 1.0850,
            takeProfit = 1.0960,
            stopLoss = 1.0810,
            marginAmount = 500.0,
            leverage = 10.0,
            totalCapital = 10180.18,
            riskPercentage = 2.0,
            status = TradeStatus.CLOSED_WIN,
            exitPrice = 1.0945,
            realizedPnl = 437.78,
            realizedPnlPercent = 87.56,
            notes = "Post-ECB dovish press conference trend continuation.",
            strategyTag = "Trend Continuation",
            openTimestamp = now - (dayMillis * 2),
            closeTimestamp = now - (dayMillis * 1)
          ),
          TradeEntity(
            symbol = "NVDA",
            tradeType = TradeType.LONG,
            entryPrice = 124.0,
            takeProfit = 138.0,
            stopLoss = 119.5,
            marginAmount = 400.0,
            leverage = 1.0,
            totalCapital = 10617.96,
            riskPercentage = 1.5,
            status = TradeStatus.OPEN,
            notes = "Consolidation base breakout on earnings anticipation.",
            strategyTag = "Breakout",
            openTimestamp = now - (dayMillis * 1)
          )
        )
        tradeDao.insertTrades(starterTrades)
      }
    }
  }
}
