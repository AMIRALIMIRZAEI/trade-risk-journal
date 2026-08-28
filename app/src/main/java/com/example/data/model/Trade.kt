package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TradeType {
  LONG,
  SHORT
}

enum class TradeStatus {
  OPEN,
  CLOSED_WIN,
  CLOSED_LOSS,
  CLOSED_BE // Break-even
}

@Entity(tableName = "trades")
data class TradeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val symbol: String, // e.g. BTC/USDT, EUR/USD, NVDA
  val tradeType: TradeType, // LONG / SHORT
  val entryPrice: Double,
  val takeProfit: Double,
  val stopLoss: Double,
  val marginAmount: Double, // Position size (Margin allocation)
  val leverage: Double = 1.0,
  val totalCapital: Double = 10000.0,
  val riskPercentage: Double = 1.0, // e.g. 1.0 for 1%
  val status: TradeStatus = TradeStatus.OPEN,
  val exitPrice: Double? = null,
  val realizedPnl: Double? = null, // in $
  val realizedPnlPercent: Double? = null, // in %
  val notes: String = "",
  val strategyTag: String = "Breakout", // Strategy rationale / setup
  val imageUri: String? = null, // Internal path or URI for attached chart screenshot
  val completedChecklistIds: String = "", // Comma-separated IDs of checklist rules met
  val openTimestamp: Long = System.currentTimeMillis(),
  val closeTimestamp: Long? = null
) {
  // Notional Position Size ($) = Margin * Leverage
  val notionalSize: Double
    get() = marginAmount * leverage

  // Position Quantity (Units of base asset) = Notional Size / Entry Price
  val positionQuantity: Double
    get() = if (entryPrice > 0) notionalSize / entryPrice else 0.0

  // Stop Loss Distance (%) = |Entry - SL| / Entry * 100
  val stopLossPercent: Double
    get() = if (entryPrice > 0) (kotlin.math.abs(entryPrice - stopLoss) / entryPrice) * 100.0 else 0.0

  // Take Profit Distance (%) = |TP - Entry| / Entry * 100
  val takeProfitPercent: Double
    get() = if (entryPrice > 0) (kotlin.math.abs(takeProfit - entryPrice) / entryPrice) * 100.0 else 0.0

  // Risk Amount ($) = Total Capital * (Risk% / 100) or Margin * (SL% * Leverage / 100)
  val riskAmount: Double
    get() = totalCapital * (riskPercentage / 100.0)

  // Risk to Reward Ratio
  val riskRewardRatio: Double
    get() = if (stopLossPercent > 0) takeProfitPercent / stopLossPercent else 0.0

  // Potential Profit ($) = Risk Amount * R:R
  val potentialProfit: Double
    get() = riskAmount * riskRewardRatio
}
