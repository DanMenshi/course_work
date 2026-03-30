package com.example.currencyrate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.currencyrate.data.repository.CurrencyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainViewModel(private val repository: CurrencyRepository) : ViewModel() {

    // Подписка на все валюты из БД
    val allCurrencies = repository.allCurrencies.asLiveData()
    
    // Подписка только на избранные
    val favoriteCurrencies = repository.favoriteCurrencies.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    private val _updateProgress = MutableStateFlow(0)
    val updateProgress: StateFlow<Int> = _updateProgress

    init {
        refreshRates()
        startAutoUpdateTimer()
    }

    private fun startAutoUpdateTimer() {
        viewModelScope.launch {
            while (true) {
                for (i in 0..100) {
                    _updateProgress.emit(i)
                    delay(600) // 60 секунд / 100 шагов = 600 мс на 1%
                }
                refreshRates()
            }
        }
    }

    fun refreshRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Загрузка данных..."
            try {
                repository.refreshCurrencies()
                
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("GMT+3")
                }
                val currentTime = sdf.format(Date())
                _syncStatus.value = "Обновлено в $currentTime (МСК)"
                
                _updateProgress.emit(0)
            } catch (e: Exception) {
                _syncStatus.value = "Ошибка обновления"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(code: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(code, isFavorite)
        }
    }
}
