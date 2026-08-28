package com.example.util

import com.example.data.model.TradeType
import kotlin.math.abs
import kotlin.math.max

object RiskCalculator {

  /**
   * Exact requested single-line formula:
   * Position Size (Margin) = (Total Capital * Risk%) / (Leverage * Stop Loss%)
   * Note: Percentages processed as decimals (e.g. 1% = 0.01, 2.5% = 0.025).
   *
   * @param totalCapital e.g. 10000.0 ($)
   * @param riskPercentDecimal e.g. 0.01 for 1%
   * @param leverage e.g. 10.0 (10x) or 1.0 (spot)
   * @param stopLossPercentDecimal e.g. 0.02 for 2%
   * @return Position Size in Margin currency ($)
   */
  fun calculateMarginPositionSize(
    totalCapital: Double,
    riskPercentDecimal: Double,
    leverage: Double,
    stopLossPercentDecimal: Double
  ): Double {
    val lev = max(1.0, leverage)
    val denominator = lev * stopLossPercentDecimal
    if (denominator <= 0.0 || totalCapital <= 0.0 || riskPercentDecimal <= 0.0) {
      return 0.0
    }
    return (totalCapital * riskPercentDecimal) / denominator
  }

  /**
   * Helper accepting percentage numbers (e.g., 1 for 1%, 2.5 for 2.5%)
   */
  fun calculateMarginFromPercents(
    totalCapital: Double,
    riskPercentage: Double,
    leverage: Double,
    stopLossPercentage: Double
  ): Double {
    val riskDecimal = riskPercentage / 100.0
    val slDecimal = stopLossPercentage / 100.0
    return calculateMarginPositionSize(totalCapital, riskDecimal, leverage, slDecimal)
  }

  /**
   * Calculates Stop Loss percentage from Entry & Stop Loss price
   */
  fun calculateStopLossPercent(entryPrice: Double, stopLossPrice: Double): Double {
    if (entryPrice <= 0.0) return 0.0
    return (abs(entryPrice - stopLossPrice) / entryPrice) * 100.0
  }

  /**
   * Calculates Take Profit percentage from Entry & Take Profit price
   */
  fun calculateTakeProfitPercent(entryPrice: Double, takeProfitPrice: Double): Double {
    if (entryPrice <= 0.0) return 0.0
    return (abs(takeProfitPrice - entryPrice) / entryPrice) * 100.0
  }

  /**
   * Calculates Risk to Reward ratio
   */
  fun calculateRiskReward(
    tradeType: TradeType,
    entryPrice: Double,
    stopLossPrice: Double,
    takeProfitPrice: Double
  ): Double {
    if (entryPrice <= 0.0) return 0.0
    val slDistance = abs(entryPrice - stopLossPrice)
    val tpDistance = abs(takeProfitPrice - entryPrice)
    if (slDistance <= 0.0) return 0.0
    return tpDistance / slDistance
  }

  /**
   * Calculates Realized PnL ($ and %) for a closed trade
   */
  fun calculateRealizedPnl(
    tradeType: TradeType,
    entryPrice: Double,
    exitPrice: Double,
    margin: Double,
    leverage: Double
  ): Pair<Double, Double> {
    if (entryPrice <= 0.0 || margin <= 0.0) return Pair(0.0, 0.0)
    val notional = margin * max(1.0, leverage)
    val priceChangePercent = when (tradeType) {
      TradeType.LONG -> ((exitPrice - entryPrice) / entryPrice)
      TradeType.SHORT -> ((entryPrice - exitPrice) / entryPrice)
    }
    val pnlDollars = notional * priceChangePercent
    val pnlPercentOnMargin = priceChangePercent * max(1.0, leverage) * 100.0
    return Pair(pnlDollars, pnlPercentOnMargin)
  }
}
