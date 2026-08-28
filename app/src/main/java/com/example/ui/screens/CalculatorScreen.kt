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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.TradeType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.CrimsonRedDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.Indigo100
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
import com.example.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
  viewModel: CalculatorViewModel,
  onApplyToTradeEntry: () -> Unit,
  modifier: Modifier = Modifier
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  val riskPresets = listOf(0.5, 1.0, 1.5, 2.0, 3.0)
  val leveragePresets = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0)
  val capitalPresets = listOf("1000", "5000", "10000", "25000", "50000")
  val sampleSymbols = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "EUR/USD", "XAU/USD", "NVDA", "AAPL")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LightBg)
      .testTag("calculator_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Title & Mathematical Formula Explanation Bento Tile
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                  .size(36.dp)
                  .background(IndigoLightBg, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Calculate,
                  contentDescription = null,
                  tint = IndigoPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Risk Management Calculator",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Slate900
                )
                Text(
                  text = "Position Sizing Engine",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate400
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = IndigoLightBg
            ) {
              Text(
                text = "EXACT FORMULA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IndigoDark,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Slate50,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Margin = (Total Capital × Risk%) / (Leverage × Stop Loss%)",
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = IndigoDark,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // 2. Primary Calculated Result Hero Bento Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("calculated_result_card"),
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "RECOMMENDED MARGIN",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Slate500,
              letterSpacing = 1.0.sp
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = IndigoLightBg
            ) {
              Text(
                text = "${state.leverage}x Leverage",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IndigoDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = DateTimeUtils.formatCurrency(state.positionSizeMargin),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = IndigoPrimary,
            fontSize = 36.sp
          )

          Text(
            text = "Allocates exact capital risking ${state.riskPercent}% on stop out",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(14.dp))

          // 4-item Metrics Breakdown Bento Mini Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            CalcMetricCell(
              label = "Max Risk ($)",
              value = "-${DateTimeUtils.formatCurrency(state.riskAmount)}",
              sub = "${state.riskPercent}% of Capital",
              valueColor = CrimsonRed
            )
            CalcMetricCell(
              label = "Notional Size",
              value = DateTimeUtils.formatCurrency(state.notionalSize),
              sub = "Margin × ${state.leverage}x",
              valueColor = Slate900
            )
            CalcMetricCell(
              label = "Target Profit",
              value = "+${DateTimeUtils.formatCurrency(state.potentialProfit)}",
              sub = "At TP target",
              valueColor = EmeraldGreen
            )
            CalcMetricCell(
              label = "Risk:Reward",
              value = "1 : ${DateTimeUtils.formatNumber(state.riskRewardRatio, 2)}",
              sub = "Ratio",
              valueColor = IndigoPrimary
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Action: Transfer To Trade Entry
          Button(
            onClick = {
              viewModel.prepareApplyToTrade()
              onApplyToTradeEntry()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("apply_to_trade_entry_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply to Trade Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // 3. Trade Setup & Parameters Inputs Bento Box
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Text(
            text = "SETUP PARAMETERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            letterSpacing = 1.0.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Trade Direction Toggle (Long vs Short)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val isLong = state.tradeType == TradeType.LONG
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.updateTradeType(TradeType.LONG) }
                .testTag("calc_long_button"),
              shape = RoundedCornerShape(12.dp),
              color = if (isLong) EmeraldGreen else Slate100
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.TrendingUp,
                  contentDescription = null,
                  tint = if (isLong) SurfaceWhite else Slate600,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "LONG / BUY",
                  fontWeight = FontWeight.Bold,
                  color = if (isLong) SurfaceWhite else Slate700,
                  fontSize = 13.sp
                )
              }
            }

            Surface(
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.updateTradeType(TradeType.SHORT) }
                .testTag("calc_short_button"),
              shape = RoundedCornerShape(12.dp),
              color = if (!isLong) CrimsonRed else Slate100
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.TrendingDown,
                  contentDescription = null,
                  tint = if (!isLong) SurfaceWhite else Slate600,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "SHORT / SELL",
                  fontWeight = FontWeight.Bold,
                  color = if (!isLong) SurfaceWhite else Slate700,
                  fontSize = 13.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Symbol Quick Chips
          Text(
            text = "Symbol / Instrument",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = state.symbol,
            onValueChange = { viewModel.updateSymbol(it) },
            placeholder = { Text("e.g. BTC/USDT, EUR/USD, NVDA") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("calc_symbol_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sampleSymbols) { sym ->
              FilterChip(
                selected = state.symbol == sym,
                onClick = { viewModel.updateSymbol(sym) },
                label = { Text(sym, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = IndigoLightBg,
                  selectedLabelColor = IndigoDark
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Total Capital Input & Presets
          Text(
            text = "Total Account Capital ($)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = state.totalCapital,
            onValueChange = { viewModel.updateCapital(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("calc_capital_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(capitalPresets) { cap ->
              FilterChip(
                selected = state.totalCapital == cap,
                onClick = { viewModel.updateCapital(cap) },
                label = { Text("$$cap", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = IndigoLightBg,
                  selectedLabelColor = IndigoDark
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Risk Percentage (%) and Leverage Inputs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Risk Per Trade (%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = state.riskPercent,
                onValueChange = { viewModel.updateRiskPercent(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("calc_risk_percent_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Leverage Multiplier",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = state.leverage,
                onValueChange = { viewModel.updateLeverage(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("calc_leverage_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Risk Presets Row
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(riskPresets) { pct ->
              FilterChip(
                selected = state.riskPercent == pct.toString(),
                onClick = { viewModel.setRiskPreset(pct) },
                label = { Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CrimsonRedBg,
                  selectedLabelColor = CrimsonRedDark
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Price Level Inputs (Entry, Stop Loss, Take Profit)
          Text(
            text = "Price Levels (Entry, SL & TP)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = state.entryPrice,
            onValueChange = { viewModel.updateEntryPrice(it) },
            label = { Text("Entry Price ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("calc_entry_price_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              OutlinedTextField(
                value = state.stopLossPrice,
                onValueChange = { viewModel.updateStopLossPrice(it) },
                label = { Text("Stop Loss ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("calc_stop_loss_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "SL Dist: ${DateTimeUtils.formatNumber(state.stopLossPercent, 2)}%",
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonRed,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              OutlinedTextField(
                value = state.takeProfitPrice,
                onValueChange = { viewModel.updateTakeProfitPrice(it) },
                label = { Text("Take Profit ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("calc_take_profit_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "TP Dist: ${DateTimeUtils.formatNumber(state.takeProfitPercent, 2)}%",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CalcMetricCell(
  label: String,
  value: String,
  sub: String,
  valueColor: Color = Slate900
) {
  Column {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = Slate500,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.ExtraBold,
      color = valueColor,
      fontSize = 14.sp
    )
    Text(
      text = sub,
      style = MaterialTheme.typography.bodySmall,
      color = Slate400,
      fontSize = 10.sp
    )
  }
}
