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
                val response = api.getDailyCurrencies()

                // Получаем текущие данные из БД, чтобы запомнить старые курсы
                val currentList = try {
                    dao.getAllCurrencies().first()
                } catch (e: Exception) {
                    emptyList<CurrencyEntity>()
                }

                val oldRatesMap = currentList.associateBy({ it.code }, { it.rate })
                val favoritesMap = currentList.associateBy({ it.code }, { it.isFavorite })
                val isFirstRun = currentList.isEmpty()

                val entities = response.valutes?.map { valute ->
                    val shouldBeFavorite = if (isFirstRun) {
                        valute.code in listOf("USD", "EUR", "CNY")
                    } else {
                        favoritesMap[valute.code] ?: false
                    }

                    val currentRate = valute.value.replace(",", ".").toDouble() / valute.nominal
                    // Если курс уже был в БД, берем его как предыдущий, иначе он равен текущему
                    val prevRate = oldRatesMap[valute.code] ?: currentRate

                    CurrencyEntity(
                        code = valute.code,
                        cbrId = valute.id,
                        name = valute.name,
                        rate = currentRate,
                        previousRate = prevRate, // Сохраняем предыдущий курс
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
                            previousRate = 1.0,
                            nominal = 1,
                            isFavorite = favoritesMap["RUB"] ?: false
                        )
                    )
                }

                dao.insertAll(listToInsert)
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

                val sdfRequest = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val end = Calendar.getInstance()
                val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }

                val response = api.getHistoricalRates(sdfRequest.format(start.time), sdfRequest.format(end.time), valuteId)

                val history = response.records?.map {
                    // Переводим дату ЦБ (dd.MM.yyyy) в формат ISO (yyyy-MM-dd) для правильной сортировки графиков
                    val parts = it.date.split(".")
                    val isoDate = if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else it.date

                    HistoricalRateEntity(
                        code = code,
                        date = isoDate,
                        rate = it.value.replace(",", ".").toDouble() / it.nominal
                    )
                } ?: emptyList()

                // Очищаем старый график этой валюты и сохраняем только новый
                dao.deleteHistoricalRates(code)
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