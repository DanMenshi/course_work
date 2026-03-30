package com.example.currencyrate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies ORDER BY isFavorite DESC, code ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE isFavorite = 1")
    fun getFavoriteCurrencies(): Flow<List<CurrencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(currencies: List<CurrencyEntity>) // Убрали : List<Long>

    @Query("UPDATE currencies SET isFavorite = :isFavorite WHERE code = :code")
    fun updateFavoriteStatus(code: String, isFavorite: Boolean) // Убрали : Int

    @Query("SELECT * FROM historical_rates WHERE code = :code ORDER BY date ASC")
    fun getHistoricalRates(code: String): Flow<List<HistoricalRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistoricalRates(rates: List<HistoricalRateEntity>) // Убрали : List<Long>
}