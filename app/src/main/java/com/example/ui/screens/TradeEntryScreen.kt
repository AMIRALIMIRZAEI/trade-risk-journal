package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import com.example.ui.components.ChecklistDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBg
import com.example.ui.theme.CrimsonRedDark
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
import com.example.util.RiskCalculator
import com.example.viewmodel.CalculatorState
import com.example.viewmodel.TradeViewModel
import java.io.File
import kotlin.math.abs

@Composable
fun TradeEntryScreen(
  viewModel: TradeViewModel,
  prefilledCalculatorState: CalculatorState?,
  onTradeSaved: () -> Unit,
  onNavigateToChecklistManager: () -> Unit,
  modifier: Modifier = Modifier
) {
  val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()
  val accountSettings by viewModel.accountSettings.collectAsStateWithLifecycle()

  var symbol by remember { mutableStateOf("BTC/USDT") }
  var tradeType by remember { mutableStateOf(TradeType.LONG) }
  var entryPriceText by remember { mutableStateOf("65000") }
  var stopLossPriceText by remember { mutableStateOf("63700") }
  var takeProfitPriceText by remember { mutableStateOf("68900") }
  var marginAmountText by remember { mutableStateOf("1000") }
  var leverageText by remember { mutableStateOf("5") }
  var strategyTag by remember { mutableStateOf("Breakout") }
  var notesText by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<String?>(null) }
  var showAddRuleDialog by remember { mutableStateOf(false) }

  val checkedChecklistIds = remember { mutableStateListOf<Long>() }

  // Populate prefilled data from Calculator when supplied
  LaunchedEffect(prefilledCalculatorState) {
    prefilledCalculatorState?.let { calc ->
      symbol = calc.symbol
      tradeType = calc.tradeType
      entryPriceText = calc.entryPrice
      stopLossPriceText = calc.stopLossPrice
      takeProfitPriceText = calc.takeProfitPrice
      marginAmountText = DateTimeUtils.formatNumber(calc.positionSizeMargin, 2).replace(",", "")
      leverageText = calc.leverage
    }
  }

  // Image Picker Launcher
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val savedPath = viewModel.saveChartImage(it)
      if (savedPath != null) {
        selectedImageUri = savedPath
      }
    }
  }

  // Real-time calculations
  val entryPrice = entryPriceText.toDoubleOrNull() ?: 0.0
  val stopLoss = stopLossPriceText.toDoubleOrNull() ?: 0.0
  val takeProfit = takeProfitPriceText.toDoubleOrNull() ?: 0.0
  val margin = marginAmountText.toDoubleOrNull() ?: 0.0
  val leverage = leverageText.toDoubleOrNull() ?: 1.0

  val slPercent = if (entryPrice > 0 && stopLoss > 0) {
    (abs(entryPrice - stopLoss) / entryPrice) * 100.0
  } else 0.0

  val tpPercent = if (entryPrice > 0 && takeProfit > 0) {
    (abs(takeProfit - entryPrice) / entryPrice) * 100.0
  } else 0.0

  val rrRatio = if (slPercent > 0) tpPercent / slPercent else 0.0
  val notionalSize = margin * leverage
  val riskAmount = notionalSize * (slPercent / 100.0)
  val potentialProfit = notionalSize * (tpPercent / 100.0)

  val sampleStrategies = listOf("Breakout", "S/R Bounce", "Trend Continuation", "Pullback", "Liquidity Sweep", "Reversal")
  val sampleSymbols = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "EUR/USD", "XAU/USD", "NVDA", "AAPL")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LightBg)
      .testTag("trade_entry_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Title Bento Tile
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
                .background(EmeraldGreenBg, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PostAdd,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Log New Trade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "Pre-trade risk & discipline checklist",
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
              text = "JOURNAL",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = IndigoDark,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // 2. Instrument & Trade Direction Bento Card
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
            text = "DIRECTION & INSTRUMENT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            letterSpacing = 1.0.sp
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Direction Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val isLong = tradeType == TradeType.LONG
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { tradeType = TradeType.LONG }
                .testTag("entry_long_toggle"),
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
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { tradeType = TradeType.SHORT }
                .testTag("entry_short_toggle"),
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

          // Symbol
          Text(
            text = "Symbol / Ticker",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = symbol,
            onValueChange = { symbol = it },
            placeholder = { Text("e.g. BTC/USDT, EUR/USD, NVDA") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("entry_symbol_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sampleSymbols) { sym ->
              FilterChip(
                selected = symbol == sym,
                onClick = { symbol = sym },
                label = { Text(sym, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = IndigoLightBg,
                  selectedLabelColor = IndigoDark
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }
      }
    }

    // 3. Trade Numbers & Sizing Bento Card
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
            text = "PRICE LEVELS & SIZING",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            letterSpacing = 1.0.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = entryPriceText,
            onValueChange = { entryPriceText = it },
            label = { Text("Entry Price ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("entry_price_input"),
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
                value = stopLossPriceText,
                onValueChange = { stopLossPriceText = it },
                label = { Text("Stop Loss ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("entry_stop_loss_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "SL Dist: ${DateTimeUtils.formatNumber(slPercent, 2)}%",
                style = MaterialTheme.typography.bodySmall,
                color = CrimsonRed,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              OutlinedTextField(
                value = takeProfitPriceText,
                onValueChange = { takeProfitPriceText = it },
                label = { Text("Take Profit ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("entry_take_profit_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "TP Dist: ${DateTimeUtils.formatNumber(tpPercent, 2)}%",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Position Size (Margin & Leverage)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              OutlinedTextField(
                value = marginAmountText,
                onValueChange = { marginAmountText = it },
                label = { Text("Position Margin ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("entry_margin_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              OutlinedTextField(
                value = leverageText,
                onValueChange = { leverageText = it },
                label = { Text("Leverage (x)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("entry_leverage_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Summary Mini-Bar
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Slate50,
            border = BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "R:R RATIO",
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate500,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
                Text(
                  text = "1 : ${DateTimeUtils.formatNumber(rrRatio, 2)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (rrRatio >= 2.0) EmeraldGreen else if (rrRatio >= 1.0) IndigoPrimary else CrimsonRed
                )
              }

              Column {
                Text(
                  text = "MAX RISK",
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate500,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
                Text(
                  text = "-${DateTimeUtils.formatCurrency(riskAmount)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = CrimsonRed
                )
              }

              Column {
                Text(
                  text = "TARGET PROFIT",
                  style = MaterialTheme.typography.labelSmall,
                  color = Slate500,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
                Text(
                  text = "+${DateTimeUtils.formatCurrency(potentialProfit)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldGreen
                )
              }
            }
          }
        }
      }
    }

    // 4. Interactive & Customizable Pre-Trade Checklist Bento Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pre_trade_checklist_card"),
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
              Icon(
                imageVector = Icons.Default.FormatListBulleted,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "PRE-TRADE DISCIPLINE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 1.0.sp
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              val verifiedCount = checkedChecklistIds.size
              val totalCount = checklistItems.size
              val allMet = totalCount > 0 && verifiedCount == totalCount

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (allMet) EmeraldGreenBg else IndigoLightBg
              ) {
                Text(
                  text = "$verifiedCount/$totalCount Rules Met",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (allMet) EmeraldGreenDark else IndigoDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              IconButton(
                onClick = { showAddRuleDialog = true },
                modifier = Modifier
                  .size(30.dp)
                  .testTag("quick_add_rule_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Checklist items with interactive checkboxes
          checklistItems.forEach { item ->
            val isChecked = checkedChecklistIds.contains(item.id)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  if (isChecked) checkedChecklistIds.remove(item.id)
                  else checkedChecklistIds.add(item.id)
                }
                .padding(vertical = 5.dp, horizontal = 4.dp)
            ) {
              Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                  if (checked) checkedChecklistIds.add(item.id)
                  else checkedChecklistIds.remove(item.id)
                },
                colors = CheckboxDefaults.colors(
                  checkedColor = EmeraldGreen,
                  checkmarkColor = SurfaceWhite
                ),
                modifier = Modifier.size(28.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                  color = if (isChecked) Slate900 else Slate700
                )
                Text(
                  text = item.category,
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate400,
                  fontSize = 10.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = onNavigateToChecklistManager,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Manage Custom Rules & Checkpoints", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // 5. Strategy Tag & Rationale Notes Bento Card
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
            text = "STRATEGY & RATIONALE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            letterSpacing = 1.0.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sampleStrategies) { tag ->
              FilterChip(
                selected = strategyTag == tag,
                onClick = { strategyTag = tag },
                label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = IndigoLightBg,
                  selectedLabelColor = IndigoDark
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            label = { Text("Strategy Rationale / Entry Confluences") },
            placeholder = { Text("Key support level, order block, RSI divergence, news catalyst...") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("entry_notes_input"),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // 6. Screenshot / Chart Attachment Bento Card
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
              Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "CHART SCREENSHOT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 1.0.sp
              )
            }

            if (selectedImageUri != null) {
              IconButton(
                onClick = { selectedImageUri = null },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = CrimsonRed)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (selectedImageUri != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceSubtle)
            ) {
              val context = LocalContext.current
              val file = remember(selectedImageUri) { File(selectedImageUri!!) }
              AsyncImage(
                model = ImageRequest.Builder(context)
                  .data(if (file.exists()) file else selectedImageUri)
                  .crossfade(true)
                  .build(),
                contentDescription = "Chart Screenshot",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }
          } else {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Slate50,
              border = BorderStroke(1.dp, Slate200),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { imagePickerLauncher.launch("image/*") }
                .testTag("pick_chart_image_button")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = IndigoPrimary,
                  modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Attach Chart Screenshot (Gallery)",
                  style = MaterialTheme.typography.titleMedium,
                  color = Slate900,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "PNG, JPG up to 10MB",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate400
                )
              }
            }
          }
        }
      }
    }

    // 7. Save & Log Trade Button
    item {
      val canSave = symbol.isNotBlank() && entryPrice > 0 && stopLoss > 0 && takeProfit > 0 && margin > 0
      Button(
        onClick = {
          if (canSave) {
            val trade = TradeEntity(
              symbol = symbol.trim().uppercase(),
              tradeType = tradeType,
              entryPrice = entryPrice,
              takeProfit = takeProfit,
              stopLoss = stopLoss,
              marginAmount = margin,
              leverage = leverage,
              totalCapital = accountSettings.totalCapital,
              riskPercentage = if (accountSettings.totalCapital > 0) (riskAmount / accountSettings.totalCapital) * 100.0 else 1.0,
              status = TradeStatus.OPEN,
              notes = notesText.trim(),
              strategyTag = strategyTag,
              imageUri = selectedImageUri,
              completedChecklistIds = checkedChecklistIds.joinToString(","),
              openTimestamp = System.currentTimeMillis()
            )
            viewModel.saveTrade(trade)
            onTradeSaved()
          }
        },
        enabled = canSave,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_and_log_trade_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Save & Log Trade to Journal",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    }
  }

  // Quick Add Rule Dialog
  if (showAddRuleDialog) {
    ChecklistDialog(
      onDismiss = { showAddRuleDialog = false },
      onSave = { title, category ->
        viewModel.addChecklistItem(title, category)
        showAddRuleDialog = false
      }
    )
  }
}
