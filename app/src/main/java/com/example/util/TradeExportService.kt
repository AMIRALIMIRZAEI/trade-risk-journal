package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.TradeEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object TradeExportService {

  private const val FILE_NAME = "trading_journal_export.csv"

  /**
   * Generates a standard formatted CSV string from a list of trades.
   */
  fun generateCsv(trades: List<TradeEntity>): String {
    val sb = StringBuilder()

    // Header row
    val headers = listOf(
      "Trade ID",
      "Symbol",
      "Direction",
      "Status",
      "Entry Price ($)",
      "Exit Price ($)",
      "Stop Loss ($)",
      "Take Profit ($)",
      "Margin ($)",
      "Leverage",
      "Notional Size ($)",
      "Risk Amount ($)",
      "Risk (%)",
      "Realized PnL ($)",
      "Realized PnL (%)",
      "R:R Ratio",
      "Strategy",
      "Emotion",
      "Lessons Learned",
      "Notes",
      "Open Date",
      "Close Date"
    )
    sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

    // Data rows
    trades.forEach { trade ->
      val row = listOf(
        trade.id.toString(),
        trade.symbol,
        trade.tradeType.name,
        trade.status.name,
        trade.entryPrice.toString(),
        trade.exitPrice?.toString() ?: "",
        trade.stopLoss.toString(),
        trade.takeProfit.toString(),
        DateTimeUtils.formatNumber(trade.marginAmount, 2),
        "${trade.leverage}x",
        DateTimeUtils.formatNumber(trade.notionalSize, 2),
        DateTimeUtils.formatNumber(trade.riskAmount, 2),
        DateTimeUtils.formatNumber(trade.riskPercentage, 2),
        trade.realizedPnl?.let { DateTimeUtils.formatNumber(it, 2) } ?: "",
        trade.realizedPnlPercent?.let { DateTimeUtils.formatNumber(it, 2) } ?: "",
        DateTimeUtils.formatNumber(trade.riskRewardRatio, 2),
        trade.strategyTag,
        trade.emotion.ifBlank { "Calm" },
        trade.lessonsLearned,
        trade.notes,
        DateTimeUtils.formatDateTime(trade.openTimestamp),
        trade.closeTimestamp?.let { DateTimeUtils.formatDateTime(it) } ?: ""
      )
      sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
    }

    return sb.toString()
  }

  /**
   * Writes the CSV content to a cache file for sharing.
   */
  fun writeCsvFile(context: Context, trades: List<TradeEntity>): File {
    val file = File(context.cacheDir, FILE_NAME)
    file.outputStream().use { out ->
      OutputStreamWriter(out, StandardCharsets.UTF_8).use { writer ->
        // Write UTF-8 BOM for Excel compatibility with international characters/symbols
        out.write(0xEF)
        out.write(0xBB)
        out.write(0xBF)
        writer.write(generateCsv(trades))
      }
    }
    return file
  }

  /**
   * Creates an Android ACTION_SEND Intent with FileProvider for sharing CSV to
   * Excel, Google Drive, Gmail, or local file manager.
   */
  fun createShareIntent(context: Context, trades: List<TradeEntity>): Intent {
    val file = writeCsvFile(context, trades)
    val uri: Uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/csv"
      putExtra(Intent.EXTRA_SUBJECT, "Trading Journal Export")
      putExtra(Intent.EXTRA_TEXT, "Trading Journal export with ${trades.size} recorded trades.")
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return Intent.createChooser(intent, "Export Trading Journal CSV")
  }

  /**
   * Writes CSV directly to a Storage Access Framework (SAF) Uri selected by the user.
   */
  fun writeToUri(context: Context, uri: Uri, trades: List<TradeEntity>): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri)?.use { out ->
        // Write UTF-8 BOM
        out.write(0xEF)
        out.write(0xBB)
        out.write(0xBF)
        OutputStreamWriter(out, StandardCharsets.UTF_8).use { writer ->
          writer.write(generateCsv(trades))
        }
      }
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  private fun escapeCsv(value: String): String {
    val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
    return if (containsSpecial) {
      "\"" + value.replace("\"", "\"\"") + "\""
    } else {
      value
    }
  }
}
