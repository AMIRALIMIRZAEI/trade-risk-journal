package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import com.example.ui.components.BentoTileStyle
import com.example.ui.components.CloseTradeDialog
import com.example.ui.components.EditTradeDialog
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.MetricCard
import com.example.ui.components.PnlCalendarView
import com.example.ui.components.TradeDetailBottomSheet
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo400
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.LightBg
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DateTimeUtils
import com.example.viewmodel.TradeViewModel
import kotlin.math.abs
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: TradeViewModel,
  onNavigateToNewTrade: () -> Unit,
  onNavigateToCalculator: () -> Unit,
  onNavigateToTradeLog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
  val equityPoints by viewModel.equityCurve.collectAsStateWithLifecycle()
  val dailyPnlMap by viewModel.dailyPnlMap.collectAsStateWithLifecycle()
  val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
  val selectedDayKey by viewModel.selectedDayKey.collectAsStateWithLifecycle()
  val allTrades by viewModel.allTrades.collectAsStateWithLifecycle()
  val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()
  val accountSettings by viewModel.accountSettings.collectAsStateWithLifecycle()

  var showCapitalEditDialog by remember { mutableStateOf(false) }
  var tradeToClose by remember { mutableStateOf<TradeEntity?>(null) }
  var selectedTradeForDetail by remember { mutableStateOf<TradeEntity?>(null) }
  var tradeToEdit by remember { mutableStateOf<TradeEntity?>(null) }
  var tradeToDelete by remember { mutableStateOf<TradeEntity?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val openTrades = remember(allTrades) { allTrades.filter { it.status == TradeStatus.OPEN } }
  val recentTrades = remember(allTrades) { allTrades.take(5) }
  val closedTradesList = remember(allTrades) { allTrades.filter { it.status != TradeStatus.OPEN } }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LightBg)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Bento Hero Card - Net Profit & Loss
    item {
      val isPositive = stats.netPnl >= 0
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("account_equity_banner"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          // Header row: Label + Percentage badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Net Profit & Loss",
              style = MaterialTheme.typography.bodyMedium,
              color = Slate500,
              fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isPositive) EmeraldGreenBg else CrimsonRedBg
              ) {
                Text(
                  text = "${if (isPositive) "+" else ""}${DateTimeUtils.formatPercent(stats.netPnlPercent)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isPositive) EmeraldGreenDark else CrimsonRedDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              IconButton(
                onClick = { showCapitalEditDialog = true },
                modifier = Modifier
                  .size(32.dp)
                  .background(Slate100, CircleShape)
                  .testTag("edit_capital_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Capital",
                  tint = Slate600,
                  modifier = Modifier.size(15.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Big Hero Value
          Text(
            text = "${if (isPositive && stats.netPnl > 0) "+" else ""}${DateTimeUtils.formatCurrency(stats.netPnl)}",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Slate900,
            fontSize = 34.sp
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "Current Balance: ${DateTimeUtils.formatCurrency(stats.currentEquity)}  •  Base: ${DateTimeUtils.formatCurrency(stats.startingCapital)}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Mini PnL Distribution Bars
          val displayTrades = remember(closedTradesList) {
            if (closedTradesList.isEmpty()) {
              listOf(0.3, 0.45, -0.2, 0.6, 0.85, 0.75, 1.0)
            } else {
              val sample = closedTradesList.take(7).reversed()
              val maxAbs = sample.map { abs(it.realizedPnl ?: 0.0) }.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
              sample.map { (it.realizedPnl ?: 0.0) / maxAbs }
            }
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
          ) {
            displayTrades.forEach { ratio ->
              val isWin = ratio >= 0
              val heightPercent = max(0.2f, minOf(1.0f, abs(ratio).toFloat()))
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight(fraction = heightPercent)
                  .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .background(if (isWin) EmeraldGreen else CrimsonRed)
              )
            }
          }
        }
      }
    }

    // 2. Bento Asymmetric Metric Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Tile 1: Soft Indigo Bento Tile for Win Rate
          MetricCard(
            title = "Win Rate",
            value = "${DateTimeUtils.formatNumber(stats.winRate, 1)}%",
            subtitle = "${stats.winsCount}W • ${stats.lossesCount}L • ${stats.breakevenCount}BE",
            tileStyle = BentoTileStyle.INDIGO_SOFT,
            badgeText = "${stats.closedTrades} Closed",
            badgeColor = Indigo100,
            badgeTextColor = IndigoDark,
            modifier = Modifier.weight(1f)
          )

          // Tile 2: Dark Slate Bento Tile for Avg R:R
          MetricCard(
            title = "Avg R:R",
            value = "1 : ${DateTimeUtils.formatNumber(stats.averageRiskReward, 2)}",
            subtitle = "Benchmark target",
            tileStyle = BentoTileStyle.DARK_SLATE,
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Tile 3: Profit Factor White Tile
          MetricCard(
            title = "Profit Factor",
            value = if (stats.profitFactor >= 99) "MAX" else DateTimeUtils.formatNumber(stats.profitFactor, 2),
            subtitle = "+${DateTimeUtils.formatCurrency(stats.totalProfit)} / -${DateTimeUtils.formatCurrency(stats.totalLoss)}",
            tileStyle = BentoTileStyle.WHITE,
            valueColor = if (stats.profitFactor >= 1.5) EmeraldGreenDark else IndigoPrimary,
            badgeText = if (stats.profitFactor >= 1.0) "Profitable" else "Drawdown",
            badgeColor = if (stats.profitFactor >= 1.0) EmeraldGreenBg else CrimsonRedBg,
            badgeTextColor = if (stats.profitFactor >= 1.0) EmeraldGreenDark else CrimsonRedDark,
            modifier = Modifier.weight(1f)
          )

          // Tile 4: Total Trades / Active Positions
          MetricCard(
            title = "Total Trades",
            value = stats.totalTrades.toString(),
            subtitle = "${stats.openTrades} Active • ${stats.closedTrades} Closed",
            tileStyle = BentoTileStyle.WHITE,
            badgeText = "Journal",
            badgeColor = Slate100,
            badgeTextColor = Slate700,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 3. Bento Tile: Risk Calculator Feature Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToCalculator() }
          .testTag("dashboard_calculator_bento_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(IndigoLightBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Calculate,
                  contentDescription = null,
                  tint = IndigoPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Risk Calculator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate800
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Slate100
            ) {
              Text(
                text = "Cap × Risk% / Lev × SL%",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Slate500,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 2 preview pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "RISK %",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Slate50, RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(
                  text = "1.0%",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = Slate900
                )
              }
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "STOP LOSS %",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Slate50, RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(
                  text = "2.5%",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = Slate900
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Recommended Margin banner
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(IndigoPrimary, RoundedCornerShape(16.dp))
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Recommended Margin",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Indigo100
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              val sampleMargin = (stats.startingCapital * 0.01) / (1.0 * 0.025)
              Text(
                text = DateTimeUtils.formatCurrency(sampleMargin),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = SurfaceWhite,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    // 4. Quick Action Buttons
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onNavigateToNewTrade,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_new_trade_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Log New Trade", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onNavigateToTradeLog,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("quick_calculator_button"),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Slate200)
        ) {
          Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Full Journal", fontWeight = FontWeight.Bold, color = Slate800)
        }
      }
    }

    // 5. Interactive Equity Curve Chart Bento Card
    item {
      EquityCurveChart(
        equityPoints = equityPoints,
        startingCapital = stats.startingCapital
      )
    }

    // 6. Monthly & Daily PnL Calendar View Bento Card
    item {
      PnlCalendarView(
        currentMonth = selectedMonth,
        dailyPnlMap = dailyPnlMap,
        selectedDayKey = selectedDayKey,
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() },
        onSelectDay = { key -> viewModel.selectDay(key) }
      )
    }

    // 7. Active / Open Trades
    if (openTrades.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(AmberWarning, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ACTIVE OPEN POSITIONS (${openTrades.size})",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Slate900,
              letterSpacing = 0.8.sp
            )
          }
        }
      }

      items(openTrades) { trade ->
        BentoTradeItem(
          trade = trade,
          onClick = { selectedTradeForDetail = trade },
          onCloseClick = { tradeToClose = trade }
        )
      }
    }

    // 8. Recent Trades Header & List
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RECENT TRADES",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Slate500,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "View All (${allTrades.size})",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = IndigoPrimary,
          modifier = Modifier.clickable { onNavigateToTradeLog() }
        )
      }
    }

    if (recentTrades.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
          border = BorderStroke(1.dp, BorderSubtle)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No trades logged yet",
                style = MaterialTheme.typography.titleMedium,
                color = Slate900,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Tap 'Log New Trade' or calculate position sizing to begin",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
          }
        }
      }
    } else {
      items(recentTrades) { trade ->
        BentoTradeItem(
          trade = trade,
          onClick = { selectedTradeForDetail = trade },
          onCloseClick = { tradeToClose = trade }
        )
      }
    }
  }

  // Edit Capital Dialog
  if (showCapitalEditDialog) {
    var capitalInput by remember { mutableStateOf(accountSettings.totalCapital.toString()) }
    AlertDialog(
      onDismissRequest = { showCapitalEditDialog = false },
      title = {
        Text(
          text = "Update Base Account Capital",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Slate900
        )
      },
      text = {
        Column {
          Text(
            text = "Used for position size calculations and initial equity baseline:",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = capitalInput,
            onValueChange = { capitalInput = it },
            label = { Text("Total Capital ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("capital_input_dialog")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = capitalInput.toDoubleOrNull()
            if (amount != null && amount > 0) {
              viewModel.updateCapital(amount)
              showCapitalEditDialog = false
            }
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
          Text("Update")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showCapitalEditDialog = false },
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Cancel")
        }
      },
      containerColor = SurfaceWhite,
      shape = RoundedCornerShape(24.dp)
    )
  }

  // Close Trade Dialog
  tradeToClose?.let { trade ->
    CloseTradeDialog(
      trade = trade,
      onDismiss = { tradeToClose = null },
      onConfirmClose = { exitPrice, notes ->
        viewModel.closeTrade(trade, exitPrice, notes)
        tradeToClose = null
      }
    )
  }

  // Trade Detail Bottom Sheet
  selectedTradeForDetail?.let { trade ->
    TradeDetailBottomSheet(
      trade = trade,
      sheetState = sheetState,
      checklistItems = checklistItems,
      onDismiss = { selectedTradeForDetail = null },
      onCloseTradeClick = {
        selectedTradeForDetail = null
        tradeToClose = trade
      },
      onEditTradeClick = {
        tradeToEdit = trade
        selectedTradeForDetail = null
      },
      onDeleteTradeClick = {
        tradeToDelete = trade
        selectedTradeForDetail = null
      }
    )
  }

  // Edit Trade Dialog
  tradeToEdit?.let { trade ->
    EditTradeDialog(
      trade = trade,
      onDismiss = { tradeToEdit = null },
      onConfirmUpdate = { updatedTrade ->
        viewModel.updateTrade(updatedTrade)
        tradeToEdit = null
      }
    )
  }

  // Delete Confirmation Dialog
  tradeToDelete?.let { trade ->
    AlertDialog(
      onDismissRequest = { tradeToDelete = null },
      icon = {
        Icon(
          imageVector = Icons.Default.WarningAmber,
          contentDescription = null,
          tint = CrimsonRed,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = "Delete Trade #${trade.id}?",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Slate900
        )
      },
      text = {
        Text(
          text = "Are you sure you want to permanently delete this ${trade.tradeType.name} position on ${trade.symbol}? This will remove it from all logs, calendar entries, and recalculate your dashboard metrics (Win Rate, Net PnL, Equity Curve).",
          style = MaterialTheme.typography.bodyMedium,
          color = Slate500
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteTrade(trade)
            tradeToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("confirm_delete_trade_dashboard_button")
        ) {
          Text("Delete Trade", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { tradeToDelete = null },
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Cancel")
        }
      },
      containerColor = SurfaceWhite,
      shape = RoundedCornerShape(20.dp)
    )
  }
}

@Composable
fun BentoTradeItem(
  trade: TradeEntity,
  onClick: () -> Unit,
  onCloseClick: () -> Unit
) {
  val isLong = trade.tradeType == TradeType.LONG
  val isOpen = trade.status == TradeStatus.OPEN
  val pnl = trade.realizedPnl ?: 0.0
  val isWin = pnl >= 0

  // Extract short ticker symbol (e.g. BTC from BTC/USDT)
  val tickerBadge = remember(trade.symbol) {
    trade.symbol.split("/").firstOrNull()?.take(4)?.uppercase() ?: trade.symbol.take(4).uppercase()
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("trade_card_${trade.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    border = BorderStroke(1.dp, BorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f, fill = false)
      ) {
        // Ticker badge (e.g. h-9 w-9 rounded-xl bg-rose-50 text-rose-600)
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(
              color = if (isLong) EmeraldGreenBg else CrimsonRedBg,
              shape = RoundedCornerShape(12.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = tickerBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (isLong) EmeraldGreenDark else CrimsonRedDark,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${if (isLong) "Long" else "Short"} ${trade.symbol}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Slate100
            ) {
              Text(
                text = "${trade.leverage}x",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate600,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                fontSize = 9.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${trade.strategyTag} • ${DateTimeUtils.formatDate(trade.openTimestamp)}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(horizontalAlignment = Alignment.End) {
        if (isOpen) {
          Button(
            onClick = onCloseClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoLightBg),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier
              .height(32.dp)
              .testTag("close_trade_btn_${trade.id}")
          ) {
            Text(
              text = "Close",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = IndigoDark
            )
          }
        } else {
          Text(
            text = "${if (isWin) "+" else ""}${DateTimeUtils.formatCurrency(pnl)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isWin) EmeraldGreen else CrimsonRed
          )
          Spacer(modifier = Modifier.height(3.dp))
          // Checklist compliance indicator dots (Bento visual polish)
          Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.size(5.dp).background(Slate300, CircleShape))
            Box(modifier = Modifier.size(5.dp).background(Slate300, CircleShape))
            Box(modifier = Modifier.size(5.dp).background(if (isWin) EmeraldGreenLight else Slate300, CircleShape))
          }
        }
      }
    }
  }
}
