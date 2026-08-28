package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

  /**
   * Copies selected image Uri to private app storage to persist across app restarts and reboots.
   * Returns internal file path string.
   */
  fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
      val contentResolver = context.contentResolver
      val inputStream: InputStream? = contentResolver.openInputStream(uri)
      if (inputStream != null) {
        val fileName = "trade_chart_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.flush()
        outputStream.close()
        file.absolutePath
      } else {
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
