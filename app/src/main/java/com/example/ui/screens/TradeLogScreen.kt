package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import com.example.ui.components.CloseTradeDialog
import com.example.ui.components.EditTradeDialog
import com.example.ui.components.TradeDetailBottomSheet
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoLightBg
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.LightBg
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
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
import com.example.viewmodel.TradeFilter
import com.example.viewmodel.TradeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeLogScreen(
  viewModel: TradeViewModel,
  onNavigateToNewTrade: () -> Unit,
  modifier: Modifier = Modifier
) {
  val filteredTrades by viewModel.filteredTrades.collectAsStateWithLifecycle()
  val activeFilter by viewModel.filter.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()

  var tradeToClose by remember { mutableStateOf<TradeEntity?>(null) }
  var selectedTradeForDetail by remember { mutableStateOf<TradeEntity?>(null) }
  var tradeToEdit by remember { mutableStateOf<TradeEntity?>(null) }
  var tradeToDelete by remember { mutableStateOf<TradeEntity?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val filters = listOf(
    TradeFilter.ALL to "All Trades",
    TradeFilter.OPEN to "Open",
    TradeFilter.CLOSED to "Closed",
    TradeFilter.WINS to "Wins",
    TradeFilter.LOSSES to "Losses"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LightBg)
      .testTag("trade_log_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Bento Tile
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .background(IndigoLightBg, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Trade History & Journal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "${filteredTrades.size} recorded trades in journal",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
              )
            }
          }

          IconButton(
            onClick = onNavigateToNewTrade,
            modifier = Modifier
              .size(38.dp)
              .background(EmeraldGreenBg, CircleShape)
              .testTag("log_screen_new_trade_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Trade", tint = EmeraldGreenDark, modifier = Modifier.size(22.dp))
          }
        }
      }
    }

    // 2. Search Box
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Search by symbol, strategy, or notes...", color = Slate400) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setSearchQuery("") }) {
              Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Slate400)
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("trade_search_input"),
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
      )
    }

    // 3. Filter Chips Row
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (filter, label) ->
          val isSelected = activeFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setFilter(filter) },
            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = IndigoPrimary,
              selectedLabelColor = SurfaceWhite
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // 4. Trade Cards List
    if (filteredTrades.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(26.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
          border = BorderStroke(1.dp, BorderSubtle)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "No trades match criteria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Adjust your filters or record a new position",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onNavigateToNewTrade,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
              ) {
                Text("Log New Trade", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    } else {
      items(filteredTrades, key = { it.id }) { trade ->
        TradeLogCard(
          trade = trade,
          onClick = { selectedTradeForDetail = trade },
          onCloseClick = { tradeToClose = trade }
        )
      }
    }
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

  // Trade Detail BottomSheet
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

  // Delete Trade Confirmation Dialog
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
          modifier = Modifier.testTag("confirm_delete_trade_button")
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
private fun TradeLogCard(
  trade: TradeEntity,
  onClick: () -> Unit,
  onCloseClick: () -> Unit
) {
  val isLong = trade.tradeType == TradeType.LONG
  val isOpen = trade.status == TradeStatus.OPEN
  val pnl = trade.realizedPnl ?: 0.0
  val isWin = pnl >= 0

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("trade_log_item_${trade.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
    border = BorderStroke(1.dp, BorderSubtle),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header: Symbol, Direction, Date & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(if (isLong) EmeraldGreenBg else CrimsonRedBg, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isLong) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
              contentDescription = null,
              tint = if (isLong) EmeraldGreen else CrimsonRed,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = trade.symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isLong) EmeraldGreenBg else CrimsonRedBg
              ) {
                Text(
                  text = "${trade.tradeType.name} ${trade.leverage}x",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isLong) EmeraldGreenDark else CrimsonRed,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontSize = 10.sp
                )
              }
            }
            Text(
              text = "${DateTimeUtils.formatDate(trade.openTimestamp)} • ${trade.strategyTag}",
              style = MaterialTheme.typography.bodySmall,
              color = Slate400,
              fontSize = 11.sp
            )
          }
        }

        // PnL or Action
        if (isOpen) {
          Button(
            onClick = onCloseClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoLightBg),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
          ) {
            Text(
              text = "Close",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = IndigoDark
            )
          }
        } else {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "${if (isWin) "+" else ""}${DateTimeUtils.formatCurrency(pnl)}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = if (isWin) EmeraldGreen else CrimsonRed
            )
            if (trade.realizedPnlPercent != null) {
              Text(
                text = DateTimeUtils.formatPercent(trade.realizedPnlPercent),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWin) EmeraldGreen else CrimsonRed,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = BorderSubtle)
      Spacer(modifier = Modifier.height(12.dp))

      // Trade Parameters 5-col summary
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("ENTRY", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
          Text("${trade.entryPrice}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate900)
        }
        Column {
          Text("STOP LOSS", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
          Text("${trade.stopLoss}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = CrimsonRed)
        }
        Column {
          Text("TAKE PROFIT", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
          Text("${trade.takeProfit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
        }
        Column {
          Text("R:R RATIO", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
          Text("1 : ${DateTimeUtils.formatNumber(trade.riskRewardRatio, 1)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = IndigoPrimary)
        }
        Column {
          Text("MARGIN", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
          Text(DateTimeUtils.formatCurrency(trade.marginAmount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate900)
        }
      }

      if (trade.imageUri != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Image, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Screenshot attached", style = MaterialTheme.typography.labelSmall, color = IndigoPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
      }
    }
  }
}
