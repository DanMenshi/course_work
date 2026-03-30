package com.example.currencyrate.data.repository

import com.example.currencyrate.data.local.CurrencyDao
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.data.local.HistoricalRateEntity
import com.example.currencyrate.data.remote.CbrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
                val response = api.getDailyCurrencies()
                val entities = response.valutes?.map { 
                    CurrencyEntity(
                        code = it.code,
                        name = it.name,
                        rate = it.value.replace(",", ".").toDouble() / it.nominal,
                        nominal = it.nominal
                    )
                } ?: emptyList()
                
                val listWithRub = entities.toMutableList().apply {
                    add(CurrencyEntity("RUB", "Российский рубль", 1.0, 1))
                }
                
                dao.insertAll(listWithRub)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun fetchHistory(code: String, days: Int): Flow<List<HistoricalRateEntity>> {
        withContext(Dispatchers.IO) {
            try {
                // Нам нужен внутренний ID валюты для динамики (например R01235)
                // Для простоты в этом примере, если ID нет, можно сопоставить или получить из первого запроса
                // Здесь я использую заглушку поиска ID, в реальном API ID передается в Valute
                val valuteId = getValuteId(code) 
                
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

    private fun getValuteId(code: String): String {
        return when(code) {
            "USD" -> "R01235"
            "EUR" -> "R01239"
            "CNY" -> "R01375"
            "KZT" -> "R01335"
            else -> "R01235"
        }
    }

    suspend fun toggleFavorite(code: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) { // ДОБАВИЛИ ОБЕРТКУ
            dao.updateFavoriteStatus(code, isFavorite)
        }
    }
}
