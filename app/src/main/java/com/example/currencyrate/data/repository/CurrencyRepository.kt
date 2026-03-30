package com.example.currencyrate.data.repository

import android.util.Log
import com.example.currencyrate.data.local.CurrencyDao
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.data.local.HistoricalRateEntity
import com.example.currencyrate.data.remote.CbrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CurrencyRepository(
    private val api: CbrApi,
    private val dao: CurrencyDao
) {
    val allCurrencies: Flow<List<CurrencyEntity>> = dao.getAllCurrencies()
    val favoriteCurrencies: Flow<List<CurrencyEntity>> = dao.getFavoriteCurrencies()

    suspend fun refreshCurrencies() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("CurrencyRepository", "Fetching daily rates from CBR...")
                val response = api.getDailyCurrencies()
                
                val currentList = try { 
                    dao.getAllCurrencies().first() 
                } catch (e: Exception) { 
                    emptyList<CurrencyEntity>() 
                }
                
                val favoritesMap = currentList.associateBy({ it.code }, { it.isFavorite })
                val isFirstRun = currentList.isEmpty()

                val entities = response.valutes?.map { valute ->
                    val shouldBeFavorite = if (isFirstRun) {
                        valute.code in listOf("USD", "EUR", "CNY")
                    } else {
                        favoritesMap[valute.code] ?: false
                    }

                    CurrencyEntity(
                        code = valute.code,
                        cbrId = valute.id,
                        name = valute.name,
                        rate = valute.value.replace(",", ".").toDouble() / valute.nominal,
                        nominal = valute.nominal,
                        isFavorite = shouldBeFavorite
                    )
                } ?: emptyList()
                
                val listToInsert = entities.toMutableList()
                
                if (listToInsert.none { it.code == "RUB" }) {
                    listToInsert.add(
                        CurrencyEntity(
                            code = "RUB", 
                            cbrId = "R00000",
                            name = "Российский рубль", 
                            rate = 1.0, 
                            nominal = 1, 
                            isFavorite = favoritesMap["RUB"] ?: false
                        )
                    )
                }
                
                dao.insertAll(listToInsert)
                Log.d("CurrencyRepository", "Successfully updated ${listToInsert.size} currencies. First run: $isFirstRun")
            } catch (e: Exception) {
                Log.e("CurrencyRepository", "Failed to refresh currencies", e)
                throw e
            }
        }
    }

    suspend fun fetchHistory(code: String, days: Int): Flow<List<HistoricalRateEntity>> {
        withContext(Dispatchers.IO) {
            try {
                val valuteId = dao.getCbrIdByCode(code) ?: "R01235"
                
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val end = Calendar.getInstance()
                val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }
                
                val response = api.getHistoricalRates(sdf.format(start.time), sdf.format(end.time), valuteId)
                
                val history = response.records?.map {
                    HistoricalRateEntity(
                        code = code,
                        date = it.date,
                        rate = it.value.replace(",", ".").toDouble() / it.nominal
                    )
                } ?: emptyList()
                
                dao.insertHistoricalRates(history)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return dao.getHistoricalRates(code)
    }

    suspend fun toggleFavorite(code: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateFavoriteStatus(code, isFavorite)
        }
    }
}
