package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TradeStatus
import com.example.data.model.TradeType

class Converters {

  @TypeConverter
  fun fromTradeType(value: TradeType): String = value.name

  @TypeConverter
  fun toTradeType(value: String): TradeType = try {
    TradeType.valueOf(value)
  } catch (e: Exception) {
    TradeType.LONG
  }

  @TypeConverter
  fun fromTradeStatus(value: TradeStatus): String = value.name

  @TypeConverter
  fun toTradeStatus(value: String): TradeStatus = try {
    TradeStatus.valueOf(value)
  } catch (e: Exception) {
    TradeStatus.OPEN
  }
}
