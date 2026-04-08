package com.example.currencyrate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.data.repository.CurrencyRepository
import java.util.Locale

class ConverterViewModel(private val repository: CurrencyRepository) : ViewModel() {

    val allCurrencies = repository.allCurrencies.asLiveData()

    private val _currencyGive = MutableLiveData<CurrencyEntity>()
    val currencyGive: LiveData<CurrencyEntity> = _currencyGive

    private val _currencyReceive = MutableLiveData<CurrencyEntity>()
    val currencyReceive: LiveData<CurrencyEntity> = _currencyReceive

    private val _resultAmount = MutableLiveData<Double>()
    val resultAmount: LiveData<Double> = _resultAmount

    private val _rateInfo = MutableLiveData<String>()
    val rateInfo: LiveData<String> = _rateInfo

    private var currentInput: Double = 0.0

    fun updateGiveCurrency(entity: CurrencyEntity) {
        _currencyGive.value = entity
        calculate()
    }

    fun updateReceiveCurrency(entity: CurrencyEntity) {
        _currencyReceive.value = entity
        calculate()
    }

    fun setInitialCurrencies(currencies: List<CurrencyEntity>) {
        if (_currencyGive.value != null && _currencyReceive.value != null) return
        
        val usd = currencies.find { it.code == "USD" }
        val rub = currencies.find { it.code == "RUB" } ?: CurrencyEntity("RUB", "R00000", "Российский рубль", 1.0, 1.0, 1)

        if (_currencyGive.value == null) _currencyGive.value = usd ?: currencies.firstOrNull()
        if (_currencyReceive.value == null) _currencyReceive.value = rub
        
        calculate()
    }

    fun setCurrencyByCode(code: String, type: String) {
        val list = allCurrencies.value ?: return

        val currency = list.find { it.code == code }
            ?: if (code == "RUB") CurrencyEntity("RUB", "R00000", "Российский рубль", 1.0, 1.0, 1) else return

        if (type == "GIVE") {
            updateGiveCurrency(currency)
        } else {
            updateReceiveCurrency(currency)
        }
    }

    fun onInputChanged(input: String) {
        currentInput = input.replace(",", ".").toDoubleOrNull() ?: 0.0
        calculate()
    }

    private fun calculate() {
        val give = _currencyGive.value ?: return
        val receive = _currencyReceive.value ?: return

        val rateGive = give.rate / give.nominal
        val rateReceive = receive.rate / receive.nominal

        val result = currentInput * (rateGive / rateReceive)
        _resultAmount.value = result
        
        updateRateInfo(give, receive)
    }

    private fun updateRateInfo(give: CurrencyEntity, receive: CurrencyEntity) {
        val rateGive = give.rate / give.nominal
        val rateReceive = receive.rate / receive.nominal
        val crossRate = rateGive / rateReceive
        _rateInfo.value = String.format(Locale.US, "1 %s = %.4f %s", give.code, crossRate, receive.code)
    }

    fun swapCurrencies() {
        val give = _currencyGive.value
        val receive = _currencyReceive.value
        if (give != null && receive != null) {
            _currencyGive.value = receive
            _currencyReceive.value = give
            calculate()
        }
    }
}
