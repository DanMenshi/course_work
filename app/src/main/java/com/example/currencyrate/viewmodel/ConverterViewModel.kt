package com.example.currencyrate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.data.repository.CurrencyRepository

class ConverterViewModel(private val repository: CurrencyRepository) : ViewModel() {

    val allCurrencies = repository.allCurrencies.asLiveData()

    private val _currencyGive = MutableLiveData<CurrencyEntity>()
    val currencyGive: LiveData<CurrencyEntity> = _currencyGive

    private val _currencyReceive = MutableLiveData<CurrencyEntity>()
    val currencyReceive: LiveData<CurrencyEntity> = _currencyReceive

    private val _resultAmount = MutableLiveData<Double>()
    val resultAmount: LiveData<Double> = _resultAmount

    fun setInitialCurrencies(giveCode: String, receiveCode: String) {
        // В реальном приложении дождемся загрузки из репозитория
    }

    fun selectGiveCurrency(currency: CurrencyEntity) {
        _currencyGive.value = currency
        calculate()
    }

    fun selectReceiveCurrency(currency: CurrencyEntity) {
        _currencyReceive.value = currency
        calculate()
    }

    private var currentInput: Double = 0.0

    fun onInputChanged(input: String) {
        currentInput = input.toDoubleOrNull() ?: 0.0
        calculate()
    }

    private fun calculate() {
        val give = _currencyGive.value ?: return
        val receive = _currencyReceive.value ?: return
        
        val rate = give.rate / receive.rate
        _resultAmount.value = currentInput * rate
    }

    fun swapCurrencies() {
        val temp = _currencyGive.value
        _currencyGive.value = _currencyReceive.value
        _currencyReceive.value = temp
        calculate()
    }
}
