package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountSettings
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import com.example.data.repository.ChecklistRepository
import com.example.data.repository.TradeRepository
import com.example.util.DateTimeUtils
import com.example.util.ImageStorageHelper
import com.example.util.RiskCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max

data class DashboardStats(
  val totalTrades: Int = 0,
  val openTrades: Int = 0,
  val closedTrades: Int = 0,
  val winsCount: Int = 0,
  val lossesCount: Int = 0,
  val breakevenCount: Int = 0,
  val winRate: Double = 0.0,
  val netPnl: Double = 0.0,
  val netPnlPercent: Double = 0.0,
  val totalProfit: Double = 0.0,
  val totalLoss: Double = 0.0,
  val profitFactor: Double = 0.0,
  val averageRiskReward: Double = 0.0,
  val bestTrade: Double = 0.0,
  val worstTrade: Double = 0.0,
  val startingCapital: Double = 10000.0,
  val currentEquity: Double = 10000.0
)

data class EquityPoint(
  val timestamp: Long,
  val dateLabel: String,
  val symbol: String,
  val equity: Double,
  val pnl: Double,
  val drawdownPercent: Double
)

data class DayPnlSummary(
  val dateKey: String, // yyyy-MM-dd
  val netPnl: Double,
  val trades: List<TradeEntity>
)

enum class TradeFilter {
  ALL,
  OPEN,
  CLOSED,
  WINS,
  LOSSES
}

class TradeViewModel(application: Application) : AndroidViewModel(application) {

  private val tradeRepository: TradeRepository
  private val checklistRepository: ChecklistRepository

  init {
    val db = AppDatabase.getDatabase(application, viewModelScope)
    tradeRepository = TradeRepository(db.tradeDao())
    checklistRepository = ChecklistRepository(db.checklistDao())
  }

  val allTrades: StateFlow<List<TradeEntity>> = tradeRepository.allTrades
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val checklistItems: StateFlow<List<ChecklistItemEntity>> = checklistRepository.allChecklistItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _accountSettings = MutableStateFlow(AccountSettings())
  val accountSettings: StateFlow<AccountSettings> = _accountSettings.asStateFlow()

  private val _filter = MutableStateFlow(TradeFilter.ALL)
  val filter: StateFlow<TradeFilter> = _filter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
  val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()

  private val _selectedDayKey = MutableStateFlow<String?>(null)
  val selectedDayKey: StateFlow<String?> = _selectedDayKey.asStateFlow()

  // Filtered trades for Trade Log tab
  val filteredTrades: StateFlow<List<TradeEntity>> = combine(
    allTrades,
    _filter,
    _searchQuery
  ) { trades, filter, query ->
    trades.filter { trade ->
      val matchesFilter = when (filter) {
        TradeFilter.ALL -> true
        TradeFilter.OPEN -> trade.status == TradeStatus.OPEN
        TradeFilter.CLOSED -> trade.status != TradeStatus.OPEN
        TradeFilter.WINS -> trade.status == TradeStatus.CLOSED_WIN
        TradeFilter.LOSSES -> trade.status == TradeStatus.CLOSED_LOSS
      }
      val matchesQuery = query.isBlank() ||
          trade.symbol.contains(query, ignoreCase = true) ||
          trade.strategyTag.contains(query, ignoreCase = true) ||
          trade.notes.contains(query, ignoreCase = true)

      matchesFilter && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dashboard Summary Metrics
  val dashboardStats: StateFlow<DashboardStats> = combine(
    allTrades,
    accountSettings
  ) { trades, settings ->
    computeDashboardStats(trades, settings.totalCapital)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

  // Interactive Equity Curve Series
  val equityCurve: StateFlow<List<EquityPoint>> = combine(
    allTrades,
    accountSettings
  ) { trades, settings ->
    computeEquityCurve(trades, settings.totalCapital)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Monthly/Daily PnL map (DateKey -> DayPnlSummary)
  val dailyPnlMap: StateFlow<Map<String, DayPnlSummary>> = allTrades.combine(_selectedMonth) { trades, _ ->
    val map = mutableMapOf<String, MutableList<TradeEntity>>()
    trades.forEach { trade ->
      val timestamp = trade.closeTimestamp ?: trade.openTimestamp
      val key = DateTimeUtils.getDayKey(timestamp)
      val list = map.getOrPut(key) { mutableListOf() }
      list.add(trade)
    }
    map.mapValues { (key, tradeList) ->
      val sumPnl = tradeList.sumOf { it.realizedPnl ?: 0.0 }
      DayPnlSummary(dateKey = key, netPnl = sumPnl, trades = tradeList)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  fun setFilter(newFilter: TradeFilter) {
    _filter.value = newFilter
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedMonth(calendar: Calendar) {
    _selectedMonth.value = calendar
  }

  fun nextMonth() {
    val cal = Calendar.getInstance().apply {
      timeInMillis = _selectedMonth.value.timeInMillis
      add(Calendar.MONTH, 1)
    }
    _selectedMonth.value = cal
  }

  fun previousMonth() {
    val cal = Calendar.getInstance().apply {
      timeInMillis = _selectedMonth.value.timeInMillis
      add(Calendar.MONTH, -1)
    }
    _selectedMonth.value = cal
  }

  fun selectDay(dateKey: String?) {
    _selectedDayKey.value = if (_selectedDayKey.value == dateKey) null else dateKey
  }

  fun updateCapital(newCapital: Double) {
    if (newCapital > 0) {
      _accountSettings.value = _accountSettings.value.copy(totalCapital = newCapital)
    }
  }

  fun saveTrade(trade: TradeEntity) {
    viewModelScope.launch {
      if (trade.id == 0L) {
        tradeRepository.insertTrade(trade)
      } else {
        tradeRepository.updateTrade(trade)
      }
    }
  }

  fun closeTrade(trade: TradeEntity, exitPrice: Double, notes: String = "") {
    viewModelScope.launch {
      val (pnlDollars, pnlPercent) = RiskCalculator.calculateRealizedPnl(
        tradeType = trade.tradeType,
        entryPrice = trade.entryPrice,
        exitPrice = exitPrice,
        margin = trade.marginAmount,
        leverage = trade.leverage
      )
      val status = when {
        abs(pnlDollars) < 0.01 -> TradeStatus.CLOSED_BE
        pnlDollars > 0 -> TradeStatus.CLOSED_WIN
        else -> TradeStatus.CLOSED_LOSS
      }
      val updatedTrade = trade.copy(
        status = status,
        exitPrice = exitPrice,
        realizedPnl = pnlDollars,
        realizedPnlPercent = pnlPercent,
        closeTimestamp = System.currentTimeMillis(),
        notes = if (notes.isNotBlank()) "${trade.notes}\n[Exit Note]: $notes".trim() else trade.notes
      )
      tradeRepository.updateTrade(updatedTrade)
    }
  }

  fun updateTrade(trade: TradeEntity) {
    viewModelScope.launch {
      tradeRepository.updateTrade(trade)
    }
  }

  fun deleteTrade(trade: TradeEntity) {
    viewModelScope.launch {
      tradeRepository.deleteTrade(trade)
    }
  }

  // Checklist management
  fun addChecklistItem(title: String, category: String = "Analysis") {
    if (title.isBlank()) return
    viewModelScope.launch {
      val items = checklistItems.value
      val nextOrder = (items.maxOfOrNull { it.orderIndex } ?: 0) + 1
      checklistRepository.insertChecklistItem(
        ChecklistItemEntity(
          title = title.trim(),
          category = category,
          orderIndex = nextOrder
        )
      )
    }
  }

  fun updateChecklistItem(item: ChecklistItemEntity) {
    viewModelScope.launch {
      checklistRepository.updateChecklistItem(item)
    }
  }

  fun deleteChecklistItem(item: ChecklistItemEntity) {
    viewModelScope.launch {
      checklistRepository.deleteChecklistItem(item)
    }
  }

  fun saveChartImage(uri: Uri): String? {
    return ImageStorageHelper.saveImageToInternalStorage(getApplication(), uri)
  }

  private fun computeDashboardStats(trades: List<TradeEntity>, startingCapital: Double): DashboardStats {
    val totalTrades = trades.size
    val openTrades = trades.count { it.status == TradeStatus.OPEN }
    val closedList = trades.filter { it.status != TradeStatus.OPEN }
    val closedTrades = closedList.size

    val wins = closedList.filter { it.status == TradeStatus.CLOSED_WIN || (it.realizedPnl ?: 0.0) > 0 }
    val losses = closedList.filter { it.status == TradeStatus.CLOSED_LOSS || (it.realizedPnl ?: 0.0) < 0 }
    val be = closedList.filter { it.status == TradeStatus.CLOSED_BE || ((it.realizedPnl ?: 0.0) == 0.0 && it.status != TradeStatus.OPEN) }

    val winsCount = wins.size
    val lossesCount = losses.size
    val breakevenCount = be.size

    val winRate = if (closedTrades > 0) (winsCount.toDouble() / closedTrades.toDouble()) * 100.0 else 0.0

    val totalProfit = wins.sumOf { max(0.0, it.realizedPnl ?: 0.0) }
    val totalLoss = losses.sumOf { abs(it.realizedPnl ?: 0.0) }
    val netPnl = closedList.sumOf { it.realizedPnl ?: 0.0 }
    val netPnlPercent = if (startingCapital > 0) (netPnl / startingCapital) * 100.0 else 0.0

    val profitFactor = if (totalLoss > 0) totalProfit / totalLoss else if (totalProfit > 0) 99.9 else 0.0

    val avgRR = if (closedList.isNotEmpty()) {
      val validRRList = closedList.map { it.riskRewardRatio }.filter { it > 0 }
      if (validRRList.isNotEmpty()) validRRList.average() else 0.0
    } else 0.0

    val bestTrade = closedList.maxOfOrNull { it.realizedPnl ?: 0.0 } ?: 0.0
    val worstTrade = closedList.minOfOrNull { it.realizedPnl ?: 0.0 } ?: 0.0

    return DashboardStats(
      totalTrades = totalTrades,
      openTrades = openTrades,
      closedTrades = closedTrades,
      winsCount = winsCount,
      lossesCount = lossesCount,
      breakevenCount = breakevenCount,
      winRate = winRate,
      netPnl = netPnl,
      netPnlPercent = netPnlPercent,
      totalProfit = totalProfit,
      totalLoss = totalLoss,
      profitFactor = profitFactor,
      averageRiskReward = avgRR,
      bestTrade = bestTrade,
      worstTrade = worstTrade,
      startingCapital = startingCapital,
      currentEquity = startingCapital + netPnl
    )
  }

  private fun computeEquityCurve(trades: List<TradeEntity>, startingCapital: Double): List<EquityPoint> {
    val closedTrades = trades
      .filter { it.status != TradeStatus.OPEN && it.closeTimestamp != null }
      .sortedBy { it.closeTimestamp ?: it.openTimestamp }

    val points = mutableListOf<EquityPoint>()
    // Starting point
    val firstTime = closedTrades.firstOrNull()?.openTimestamp ?: System.currentTimeMillis()
    var runningEquity = startingCapital
    var peakEquity = startingCapital

    points.add(
      EquityPoint(
        timestamp = firstTime - 3600000L,
        dateLabel = "Start",
        symbol = "Initial Balance",
        equity = startingCapital,
        pnl = 0.0,
        drawdownPercent = 0.0
      )
    )

    closedTrades.forEach { trade ->
      val pnl = trade.realizedPnl ?: 0.0
      runningEquity += pnl
      if (runningEquity > peakEquity) {
        peakEquity = runningEquity
      }
      val drawdownPercent = if (peakEquity > 0) ((peakEquity - runningEquity) / peakEquity) * 100.0 else 0.0
      val time = trade.closeTimestamp ?: trade.openTimestamp

      points.add(
        EquityPoint(
          timestamp = time,
          dateLabel = DateTimeUtils.formatDate(time),
          symbol = trade.symbol,
          equity = runningEquity,
          pnl = pnl,
          drawdownPercent = drawdownPercent
        )
      )
    }

    return points
  }
}
