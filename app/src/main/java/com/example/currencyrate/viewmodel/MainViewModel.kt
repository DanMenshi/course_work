package com.example.currencyrate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.currencyrate.data.repository.CurrencyRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(private val repository: CurrencyRepository) : ViewModel() {

    // Подписка на все валюты из БД
    val allCurrencies = repository.allCurrencies.asLiveData()
    
    // Подписка только на избранные
    val favoriteCurrencies = repository.favoriteCurrencies.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    init {
        refreshRates()
    }

    fun refreshRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Загрузка данных..."
            try {
                repository.refreshCurrencies()
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                _syncStatus.value = "Обновлено: $currentTime"
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
