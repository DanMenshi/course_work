package com.example.currencyrate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    /**
     * Возвращает все валюты, отсортированные:
     * 1. Сначала избранные (isFavorite = 1)
     * 2. Затем по коду валюты алфавиту
     */
    @Query("SELECT * FROM currencies ORDER BY isFavorite DESC, code ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE isFavorite = 1 ORDER BY code ASC")
    fun getFavoriteCurrencies(): Flow<List<CurrencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currencies: List<CurrencyEntity>)

    @Query("UPDATE currencies SET isFavorite = :isFavorite WHERE code = :code")
    suspend fun updateFavoriteStatus(code: String, isFavorite: Boolean)

    @Query("SELECT * FROM historical_rates WHERE code = :code ORDER BY date ASC")
    fun getHistoricalRates(code: String): Flow<List<HistoricalRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalRates(rates: List<HistoricalRateEntity>)
}
