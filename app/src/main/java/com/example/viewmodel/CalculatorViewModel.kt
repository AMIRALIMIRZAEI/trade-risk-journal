package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.TradeType
import com.example.util.RiskCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max

data class CalculatorState(
  val totalCapital: String = "10000",
  val riskPercent: String = "1.0", // 1.0%
  val leverage: String = "5", // 5x
  val tradeType: TradeType = TradeType.LONG,
  val symbol: String = "BTC/USDT",
  val entryPrice: String = "65000",
  val stopLossPrice: String = "63700",
  val takeProfitPrice: String = "68900",
  // Computed values
  val stopLossPercent: Double = 2.0, // 2%
  val takeProfitPercent: Double = 6.0, // 6%
  val positionSizeMargin: Double = 1000.0, // Calculated exact Margin
  val notionalSize: Double = 5000.0, // Margin * Leverage
  val riskAmount: Double = 100.0, // Total Capital * Risk%
  val potentialProfit: Double = 300.0, // Notional * TP% or RiskAmount * RR
  val riskRewardRatio: Double = 3.0 // 1:3.0
)

class CalculatorViewModel : ViewModel() {

  private val _state = MutableStateFlow(CalculatorState())
  val state: StateFlow<CalculatorState> = _state.asStateFlow()

  // Flag/data to pass when user clicks "Send to Trade Entry"
  private val _prefilledTradeData = MutableStateFlow<CalculatorState?>(null)
  val prefilledTradeData: StateFlow<CalculatorState?> = _prefilledTradeData.asStateFlow()
  val transferToTradeState: CalculatorState? get() = _prefilledTradeData.value

  init {
    recalculate()
  }

  fun updateCapital(value: String) {
    _state.value = _state.value.copy(totalCapital = value)
    recalculate()
  }

  fun updateRiskPercent(value: String) {
    _state.value = _state.value.copy(riskPercent = value)
    recalculate()
  }

  fun updateLeverage(value: String) {
    _state.value = _state.value.copy(leverage = value)
    recalculate()
  }

  fun updateTradeType(type: TradeType) {
    _state.value = _state.value.copy(tradeType = type)
    recalculate()
  }

  fun updateSymbol(symbol: String) {
    _state.value = _state.value.copy(symbol = symbol)
  }

  fun updateEntryPrice(value: String) {
    _state.value = _state.value.copy(entryPrice = value)
    recalculate()
  }

  fun updateStopLossPrice(value: String) {
    _state.value = _state.value.copy(stopLossPrice = value)
    recalculate()
  }

  fun updateTakeProfitPrice(value: String) {
    _state.value = _state.value.copy(takeProfitPrice = value)
    recalculate()
  }

  fun setRiskPreset(percent: Double) {
    _state.value = _state.value.copy(riskPercent = percent.toString())
    recalculate()
  }

  fun setLeveragePreset(lev: Double) {
    _state.value = _state.value.copy(leverage = if (lev == lev.toLong().toDouble()) lev.toLong().toString() else lev.toString())
    recalculate()
  }

  fun prepareApplyToTrade() {
    _prefilledTradeData.value = _state.value
  }

  fun clearPrefilledData() {
    _prefilledTradeData.value = null
  }

  fun clearTransferState() {
    clearPrefilledData()
  }

  private fun recalculate() {
    val current = _state.value
    val capital = current.totalCapital.toDoubleOrNull() ?: 0.0
    val riskPct = current.riskPercent.toDoubleOrNull() ?: 0.0
    val lev = max(1.0, current.leverage.toDoubleOrNull() ?: 1.0)
    val entry = current.entryPrice.toDoubleOrNull() ?: 0.0
    val sl = current.stopLossPrice.toDoubleOrNull() ?: 0.0
    val tp = current.takeProfitPrice.toDoubleOrNull() ?: 0.0

    // Stop Loss % = |Entry - SL| / Entry * 100
    val slPercent = if (entry > 0 && sl > 0) {
      (abs(entry - sl) / entry) * 100.0
    } else {
      2.0 // fallback
    }

    // Take Profit % = |TP - Entry| / Entry * 100
    val tpPercent = if (entry > 0 && tp > 0) {
      (abs(tp - entry) / entry) * 100.0
    } else {
      4.0 // fallback
    }

    // Exact requested single-line formula:
    // Position Size (Margin) = (Total Capital * Risk%) / (Leverage * Stop Loss%)
    // with percentages processed as decimals
    val riskDecimal = riskPct / 100.0
    val slDecimal = slPercent / 100.0
    val margin = RiskCalculator.calculateMarginPositionSize(
      totalCapital = capital,
      riskPercentDecimal = riskDecimal,
      leverage = lev,
      stopLossPercentDecimal = slDecimal
    )

    val notional = margin * lev
    val riskAmount = capital * riskDecimal
    val rr = if (slPercent > 0) tpPercent / slPercent else 0.0
    val potentialProfit = riskAmount * rr

    _state.value = current.copy(
      stopLossPercent = slPercent,
      takeProfitPercent = tpPercent,
      positionSizeMargin = margin,
      notionalSize = notional,
      riskAmount = riskAmount,
      potentialProfit = potentialProfit,
      riskRewardRatio = rr
    )
  }
}
