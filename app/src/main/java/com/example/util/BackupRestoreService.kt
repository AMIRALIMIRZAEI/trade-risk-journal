package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TradeEntity
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

data class BackupPayload(
  val version: Int,
  val exportTimestamp: Long,
  val totalCapital: Double?,
  val trades: List<TradeEntity>,
  val checklistItems: List<ChecklistItemEntity>
)

object BackupRestoreService {

  private const val BACKUP_FILE_NAME = "trading_journal_backup.json"

  /**
   * Serializes the full application database state into a clean JSON string.
   */
  fun exportToJson(
    trades: List<TradeEntity>,
    checklistItems: List<ChecklistItemEntity>,
    totalCapital: Double
  ): String {
    val root = JSONObject()
    root.put("version", 3)
    root.put("appName", "Trading Journal")
    root.put("exportTimestamp", System.currentTimeMillis())
    root.put("totalCapital", totalCapital)

    // Trades array
    val tradesArray = JSONArray()
    trades.forEach { trade ->
      val tradeObj = JSONObject().apply {
        put("id", trade.id)
        put("symbol", trade.symbol)
        put("tradeType", trade.tradeType.name)
        put("entryPrice", trade.entryPrice)
        put("takeProfit", trade.takeProfit)
        put("stopLoss", trade.stopLoss)
        put("marginAmount", trade.marginAmount)
        put("leverage", trade.leverage)
        put("totalCapital", trade.totalCapital)
        put("riskPercentage", trade.riskPercentage)
        put("status", trade.status.name)
        if (trade.exitPrice != null) put("exitPrice", trade.exitPrice)
        if (trade.realizedPnl != null) put("realizedPnl", trade.realizedPnl)
        if (trade.realizedPnlPercent != null) put("realizedPnlPercent", trade.realizedPnlPercent)
        put("notes", trade.notes)
        put("strategyTag", trade.strategyTag)
        if (trade.imageUri != null) put("imageUri", trade.imageUri)
        put("completedChecklistIds", trade.completedChecklistIds)
        put("openTimestamp", trade.openTimestamp)
        if (trade.closeTimestamp != null) put("closeTimestamp", trade.closeTimestamp)
        put("emotion", trade.emotion.ifBlank { "Calm" })
        put("lessonsLearned", trade.lessonsLearned)
      }
      tradesArray.put(tradeObj)
    }
    root.put("trades", tradesArray)

    // Checklist array
    val checklistArray = JSONArray()
    checklistItems.forEach { item ->
      val itemObj = JSONObject().apply {
        put("id", item.id)
        put("title", item.title)
        put("category", item.category)
        put("orderIndex", item.orderIndex)
      }
      checklistArray.put(itemObj)
    }
    root.put("checklistItems", checklistArray)

    return root.toString(2)
  }

  /**
   * Parses and validates a JSON backup payload.
   */
  fun parseBackupJson(jsonString: String): Result<BackupPayload> {
    return try {
      val root = JSONObject(jsonString)
      val version = root.optInt("version", 1)
      val exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis())
      val totalCapital = if (root.has("totalCapital")) root.getDouble("totalCapital") else null

      val tradesList = mutableListOf<TradeEntity>()
      if (root.has("trades")) {
        val tradesArray = root.getJSONArray("trades")
        for (i in 0 until tradesArray.length()) {
          val obj = tradesArray.getJSONObject(i)
          val tradeType = try {
            TradeType.valueOf(obj.optString("tradeType", "LONG"))
          } catch (_: Exception) {
            TradeType.LONG
          }
          val status = try {
            TradeStatus.valueOf(obj.optString("status", "OPEN"))
          } catch (_: Exception) {
            TradeStatus.OPEN
          }

          val trade = TradeEntity(
            id = obj.optLong("id", 0L),
            symbol = obj.optString("symbol", "BTC/USDT"),
            tradeType = tradeType,
            entryPrice = obj.optDouble("entryPrice", 0.0),
            takeProfit = obj.optDouble("takeProfit", 0.0),
            stopLoss = obj.optDouble("stopLoss", 0.0),
            marginAmount = obj.optDouble("marginAmount", 100.0),
            leverage = obj.optDouble("leverage", 1.0),
            totalCapital = obj.optDouble("totalCapital", 10000.0),
            riskPercentage = obj.optDouble("riskPercentage", 1.0),
            status = status,
            exitPrice = if (obj.has("exitPrice") && !obj.isNull("exitPrice")) obj.getDouble("exitPrice") else null,
            realizedPnl = if (obj.has("realizedPnl") && !obj.isNull("realizedPnl")) obj.getDouble("realizedPnl") else null,
            realizedPnlPercent = if (obj.has("realizedPnlPercent") && !obj.isNull("realizedPnlPercent")) obj.getDouble("realizedPnlPercent") else null,
            notes = obj.optString("notes", ""),
            strategyTag = obj.optString("strategyTag", "General"),
            imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null,
            completedChecklistIds = obj.optString("completedChecklistIds", ""),
            openTimestamp = obj.optLong("openTimestamp", System.currentTimeMillis()),
            closeTimestamp = if (obj.has("closeTimestamp") && !obj.isNull("closeTimestamp")) obj.getLong("closeTimestamp") else null,
            emotion = obj.optString("emotion", "Calm").ifBlank { "Calm" },
            lessonsLearned = obj.optString("lessonsLearned", "")
          )
          tradesList.add(trade)
        }
      }

      val checklistList = mutableListOf<ChecklistItemEntity>()
      if (root.has("checklistItems")) {
        val checklistArray = root.getJSONArray("checklistItems")
        for (i in 0 until checklistArray.length()) {
          val obj = checklistArray.getJSONObject(i)
          val item = ChecklistItemEntity(
            id = obj.optLong("id", 0L),
            title = obj.optString("title", "Checklist Rule"),
            category = obj.optString("category", "General"),
            orderIndex = obj.optInt("orderIndex", i)
          )
          checklistList.add(item)
        }
      }

      Result.success(
        BackupPayload(
          version = version,
          exportTimestamp = exportTimestamp,
          totalCapital = totalCapital,
          trades = tradesList,
          checklistItems = checklistList
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Writes backup JSON to cache for sharing.
   */
  fun writeBackupFile(context: Context, jsonString: String): File {
    val file = File(context.cacheDir, BACKUP_FILE_NAME)
    file.outputStream().use { out ->
      OutputStreamWriter(out, StandardCharsets.UTF_8).use { writer ->
        writer.write(jsonString)
      }
    }
    return file
  }

  /**
   * Creates an Android ACTION_SEND Intent for sharing the JSON backup file.
   */
  fun createShareBackupIntent(context: Context, jsonString: String): Intent {
    val file = writeBackupFile(context, jsonString)
    val uri: Uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "application/json"
      putExtra(Intent.EXTRA_SUBJECT, "Trading Journal Full Database Backup")
      putExtra(Intent.EXTRA_TEXT, "Trading Journal full JSON backup created on ${DateTimeUtils.formatDateTime(System.currentTimeMillis())}")
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return Intent.createChooser(intent, "Share Database Backup JSON")
  }

  /**
   * Writes JSON string to a SAF Uri.
   */
  fun writeToUri(context: Context, uri: Uri, jsonString: String): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri)?.use { out ->
        OutputStreamWriter(out, StandardCharsets.UTF_8).use { writer ->
          writer.write(jsonString)
        }
      }
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  /**
   * Reads JSON string from a SAF Uri.
   */
  fun readFromUri(context: Context, uri: Uri): String? {
    return try {
      context.contentResolver.openInputStream(uri)?.use { input ->
        BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
          reader.readText()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
