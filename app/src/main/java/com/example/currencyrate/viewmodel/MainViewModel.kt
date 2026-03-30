package com.example.currencyrate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.currencyrate.data.repository.CurrencyRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: CurrencyRepository) : ViewModel() {

    // Подписка на все валюты из БД
    val allCurrencies = repository.allCurrencies.asLiveData()
    
    // Подписка только на избранные
    val favoriteCurrencies = repository.favoriteCurrencies.asLiveData()

    init {
        refreshRates()
    }

    fun refreshRates() {
        viewModelScope.launch {
            repository.refreshCurrencies()
        }
    }

    fun toggleFavorite(code: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(code, isFavorite)
        }
    }
}
