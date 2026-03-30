package com.example.currencyrate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyrate.data.local.HistoricalRateEntity
import com.example.currencyrate.data.repository.CurrencyRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailsViewModel(private val repository: CurrencyRepository) : ViewModel() {

    private val _history = MutableLiveData<List<HistoricalRateEntity>>()
    val history: LiveData<List<HistoricalRateEntity>> = _history

    private val _stats = MutableLiveData<Pair<Double, Double>>() // Min to Max
    val stats: LiveData<Pair<Double, Double>> = _stats

    fun loadHistory(code: String, days: Int) {
        viewModelScope.launch {
            repository.fetchHistory(code, days).collectLatest { list ->
                if (list.isNotEmpty()) {
                    _history.value = list
                    val min = list.minByOrNull { it.rate }?.rate ?: 0.0
                    val max = list.maxByOrNull { it.rate }?.rate ?: 0.0
                    _stats.value = min to max
                } else {
                    _history.value = emptyList()
                    _stats.value = 0.0 to 0.0
                }
            }
        }
    }
}
