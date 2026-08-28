package com.example.data.repository

import com.example.data.local.TradeDao
import com.example.data.model.TradeEntity
import kotlinx.coroutines.flow.Flow

class TradeRepository(private val tradeDao: TradeDao) {

  val allTrades: Flow<List<TradeEntity>> = tradeDao.getAllTrades()

  fun getTradeById(id: Long): Flow<TradeEntity?> = tradeDao.getTradeById(id)

  suspend fun insertTrade(trade: TradeEntity): Long = tradeDao.insertTrade(trade)

  suspend fun updateTrade(trade: TradeEntity) = tradeDao.updateTrade(trade)

  suspend fun deleteTrade(trade: TradeEntity) = tradeDao.deleteTrade(trade)

  suspend fun deleteTradeById(id: Long) = tradeDao.deleteTradeById(id)

  suspend fun deleteAllTrades() = tradeDao.deleteAllTrades()
}
