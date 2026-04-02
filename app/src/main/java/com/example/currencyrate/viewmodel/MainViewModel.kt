package com.example.currencyrate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.currencyrate.data.repository.CurrencyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainViewModel(private val repository: CurrencyRepository) : ViewModel() {

    val allCurrencies = repository.allCurrencies.asLiveData()
    val favoriteCurrencies = repository.favoriteCurrencies.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    private val _updateProgress = MutableStateFlow(0)
    val updateProgress: StateFlow<Int> = _updateProgress

    private var timerJob: Job? = null

    init {
        refreshRates()
    }

    private fun startAutoUpdateTimer() {
        // Отменяем предыдущий таймер, если он был
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            _updateProgress.value = 0 // Мгновенно сбрасываем в ноль

            // Плавный таймер на 60 секунд (1000 шагов)
            for (i in 0..1000) {
                _updateProgress.value = i
                delay(60) // 60 мс * 1000 = 60000 мс (1 минута)
            }
            // По истечении таймера - обновляем
            refreshRates()
        }
    }

    fun refreshRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Загрузка данных..."

            // Останавливаем таймер на время загрузки
            timerJob?.cancel()
            _updateProgress.value = 0

            try {
                repository.refreshCurrencies()

                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("GMT+3")
                }
                val currentTime = sdf.format(Date())
                _syncStatus.value = "Обновлено в $currentTime (МСК)"

            } catch (e: Exception) {
                _syncStatus.value = "Ошибка обновления"
            } finally {
                _isLoading.value = false
                // Запускаем таймер заново после завершения загрузки
                startAutoUpdateTimer()
            }
        }
    }

    fun toggleFavorite(code: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(code, isFavorite)
        }
    }
}